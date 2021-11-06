package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.events.Events;
import de.budschie.bmorph.events.PlayerMorphEvent;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

// This is just a stupid class which goal is to unify the code for morphing
public class MorphUtil
{
	private static Logger LOGGER = LogManager.getLogger();
	
	public static void morphToServer(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, PlayerEntity player)
	{
		morphToServer(morphItem, morphIndex, player, false);
	}
	
	public static void processCap(PlayerEntity player, Consumer<IMorphCapability> capConsumer)
	{
		LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			capConsumer.accept(resolved);
		}
	}
	
	public static IMorphCapability getCapOrNull(PlayerEntity playerEntity)
	{
		LazyOptional<IMorphCapability> cap = playerEntity.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
			return cap.resolve().get();
		
		return null;
	}
	
	/** Method to invoke a morph operation on the server. This will be synced to every player on the server. TODO: Only sync change to players that are being tracked.  **/
	public static void morphToServer(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, PlayerEntity player, boolean force)
	{
		LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			
			MorphItem aboutToMorphTo = null;
			
			if(morphItem.isPresent())
				aboutToMorphTo = morphItem.get();
			else if(morphIndex.isPresent())
				aboutToMorphTo = resolved.getMorphList().getMorphArrayList().get(morphIndex.get());
			
			boolean isCanceled = MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(player, resolved, aboutToMorphTo));
			
			if(!(isCanceled && !force))
			{
				resolved.deapplyAbilities(player);
				
				if(morphIndex.isPresent())
					resolved.setMorph(morphIndex.get());
				else if(morphItem.isPresent())
					resolved.setMorph(morphItem.get());
				else
					resolved.demorph();
				
				//resolved.getCurrentMorph().ifPresentOrElse(morph -> resolved.setCurrentAbilities(AbilityLookupTableHandler.getAbilitiesFor(morph)), () -> resolved.setCurrentAbilities(new ArrayList<>()));
				if(resolved.getCurrentMorph().isPresent())
					resolved.setCurrentAbilities(Events.MORPH_ABILITY_MANAGER.getAbilitiesFor(resolved.getCurrentMorph().get()));
				else
					resolved.setCurrentAbilities(null);
				
				resolved.applyAbilities(player);
				resolved.syncMorphChange(player);
				resolved.applyHealthOnPlayer(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, resolved, aboutToMorphTo));
			}
		}
	}
	
	/** This method is used to invoke functions required for handling a sync on the server on the client side. **/
	public static void morphToClient(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, ArrayList<String> abilities, PlayerEntity player)
	{
		if(player != null)
		{
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				MorphItem aboutToMorphTo = null;
				
				if(morphItem.isPresent())
					aboutToMorphTo = morphItem.get();
				else if(morphIndex.isPresent())
					aboutToMorphTo = resolved.getMorphList().getMorphArrayList().get(morphIndex.get());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Pre(player, resolved, aboutToMorphTo));
				
				resolved.deapplyAbilities(player);
				
				if(morphIndex.isPresent())
					resolved.setMorph(morphIndex.get());
				else if(morphItem.isPresent())
					resolved.setMorph(morphItem.get());
				else
					resolved.demorph();
				
				ArrayList<Ability> resolvedAbilities = new ArrayList<>();
				
				// IForgeRegistry<Ability> registry = GameRegistry.findRegistry(Ability.class);
				
				for(String name : abilities)
				{					
					ResourceLocation resourceLocation = new ResourceLocation(name);
					Ability foundAbility = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getAbility(resourceLocation);
					
					if(foundAbility == null)
					{
						LOGGER.warn("The ability %s is not present on the client. Ignoring this entry.", resourceLocation);
					}
					else
						resolvedAbilities.add(foundAbility);
				}
				
				MorphItem javaSucks = aboutToMorphTo;
				
				resolved.setCurrentAbilities(resolvedAbilities);
				
				// Create entity right before we apply the abilities
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RenderHandler.onBuildNewEntity(player, resolved, javaSucks));
				
				resolved.applyAbilities(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Post(player, resolved, aboutToMorphTo));
			}
			else
				System.out.println("Could not synchronize data, as the morph cap is not created yet.");
		}
	}
}
