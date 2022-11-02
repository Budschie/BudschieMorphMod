package de.budschie.bmorph.morph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.events.PlayerMorphEvent;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.render_handler.EntitySynchronizerRegistry;
import de.budschie.bmorph.render_handler.IEntitySynchronizer;
import de.budschie.bmorph.render_handler.RenderHandler;
import de.budschie.bmorph.render_handler.animations.ScaleAnimation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

// This is just a stupid class which goal is to unify the code for morphing
public class MorphUtil
{
	private static Logger LOGGER = LogManager.getLogger();
	
	// TODO: Remove deprecated stuff in the future
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public static void morphToServer(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, Player player)
	{
		morphToServer(morphItem, morphIndex, morphIndex.isPresent() ? MorphReasonRegistry.MORPHED_BY_UI.get() : MorphReasonRegistry.MORPHED_BY_COMMAND.get(), player, false);
	}
	
	public static void morphToServer(Optional<MorphItem> morphItem, MorphReason reason, Player player)
	{
		morphToServer(morphItem, Optional.empty(), reason, player, false);
	}
	
	public static void processCap(Player player, Consumer<IMorphCapability> capConsumer)
	{
		LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			capConsumer.accept(resolved);
		}
	}
	
	public static IMorphCapability getCapOrNull(Player playerEntity)
	{
		if(playerEntity == null)
			return null;
		
		LazyOptional<IMorphCapability> cap = playerEntity.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
			return cap.resolve().get();
		
		return null;
	}
	
	public static void morphToServer(Optional<MorphItem> morphItem, MorphReason reason, Player player, boolean force)
	{
		morphToServer(morphItem, Optional.empty(), reason, player, force);
	}
	
	/** Method to invoke a morph operation on the server. This will be synced to every player on the server.  **/
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public static void morphToServer(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, Player player, boolean force)
	{
		morphToServer(morphItem, morphIndex, morphIndex.isPresent() ? MorphReasonRegistry.MORPHED_BY_UI.get() : MorphReasonRegistry.MORPHED_BY_COMMAND.get(), player, force);
	}
	
	private static void morphToServer(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, MorphReason reason, Player player, boolean force)
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
			
			boolean isCanceled = MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(player, resolved, aboutToMorphTo, reason));
			
			if(!(isCanceled && !force))
			{
				List<Ability> newAbilities = null;
				List<Ability> oldAbilities = resolved.getCurrentAbilities();
				MorphItem oldMorphItem = resolved.getCurrentMorph().orElse(null);
				
				if(aboutToMorphTo != null)
					newAbilities = aboutToMorphTo.getAbilities();
				
				resolved.deapplyAbilities(aboutToMorphTo, newAbilities == null ? Arrays.asList() : newAbilities);
				
				if(morphIndex.isPresent())
					resolved.setMorph(morphIndex.get(), reason);
				else if(morphItem.isPresent())
					resolved.setMorph(morphItem.get(), reason);
				else
					resolved.demorph(reason);
				
				//resolved.getCurrentMorph().ifPresentOrElse(morph -> resolved.setCurrentAbilities(AbilityLookupTableHandler.getAbilitiesFor(morph)), () -> resolved.setCurrentAbilities(new ArrayList<>()));
				
				resolved.setCurrentAbilities(newAbilities);
				
				resolved.applyAbilities(oldMorphItem, oldAbilities == null ? Arrays.asList() : oldAbilities);
				resolved.syncMorphChange();
				resolved.applyHealthOnPlayer();
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, resolved, aboutToMorphTo, reason));
			}
		}
	}
	
	public static void morphToClient(Optional<MorphItem> morphItem, MorphReason reason, ArrayList<String> abilities, Player player)
	{
		morphToClient(morphItem, Optional.empty(), reason, abilities, player);
	}
	
	private static void morphToClient(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, MorphReason reason, ArrayList<String> abilities, Player player)
	{
		if(player != null)
		{
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				MorphItem aboutToMorphTo = null;
				
				List<Ability> oldAbilities = resolved.getCurrentAbilities();
				MorphItem oldMorphItem = resolved.getCurrentMorph().orElse(null);
				
				if(morphItem.isPresent())
					aboutToMorphTo = morphItem.get();
				else if(morphIndex.isPresent())
					aboutToMorphTo = resolved.getMorphList().getMorphArrayList().get(morphIndex.get());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Pre(player, resolved, aboutToMorphTo, reason));
				
				IRenderDataCapability renderDataCap = player.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get();
				
				Entity cachedEntityOld = renderDataCap.getOrCreateCachedEntity(player);
				ArrayList<IEntitySynchronizer> synchronizersOld = EntitySynchronizerRegistry.getSynchronizersForEntity(cachedEntityOld);
				
				// If we don't have any cached entity => we are demorphed, just use no synchronizer and the player itself as an entity
				if(cachedEntityOld == null)
				{
					cachedEntityOld = player;
					synchronizersOld.clear();
				}
				
				ArrayList<Ability> resolvedAbilities = new ArrayList<>();
				
				// IForgeRegistry<Ability> registry = GameRegistry.findRegistry(Ability.class);
				
				for(String name : abilities)
				{					
					ResourceLocation resourceLocation = new ResourceLocation(name);
					Ability foundAbility = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(resourceLocation);
					
					if(foundAbility == null)
					{
						LOGGER.warn(MessageFormat.format("The ability {0} is not present on the client. Ignoring this entry.", resourceLocation));
					}
					else
						resolvedAbilities.add(foundAbility);
				}
				
				resolved.deapplyAbilities(aboutToMorphTo, resolvedAbilities);
				
				if(morphIndex.isPresent())
					resolved.setMorph(morphIndex.get(), reason);
				else if(morphItem.isPresent())
					resolved.setMorph(morphItem.get(), reason);
				else
					resolved.demorph(reason);
					
				MorphItem javaSucks = aboutToMorphTo;
				
				resolved.setCurrentAbilities(resolvedAbilities);
				
				// Create entity right before we apply the abilities
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RenderHandler.onBuildNewEntity(player, resolved, javaSucks));
				
				resolved.applyAbilities(oldMorphItem, oldAbilities == null ? Arrays.asList() : oldAbilities);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Post(player, resolved, aboutToMorphTo, reason));
				
				Entity cachedEntityNew = renderDataCap.getOrCreateCachedEntity(player);
				ArrayList<IEntitySynchronizer> synchronizersNew = EntitySynchronizerRegistry.getSynchronizersForEntity(cachedEntityNew);
				
				// If we don't have any cached entity => we are demorphed, just use no synchronizer and the player itself as an entity
				if(cachedEntityNew == null)
				{
					cachedEntityNew = player;
					synchronizersNew.clear();
				}
				
				renderDataCap.setAnimation(Optional.of(new ScaleAnimation(player, cachedEntityOld, synchronizersOld, cachedEntityNew, synchronizersNew, 20)));
			}
			else
				LOGGER.warn("Could not synchronize data, as the morph cap is not created yet.");
		}
	}
	
	/** This method is used to invoke functions required for handling a sync on the server on the client side. **/
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public static void morphToClient(Optional<MorphItem> morphItem, Optional<Integer> morphIndex, ArrayList<String> abilities, Player player)
	{
		morphToClient(morphItem, morphIndex, morphIndex.isPresent() ? MorphReasonRegistry.MORPHED_BY_UI.get() : MorphReasonRegistry.MORPHED_BY_COMMAND.get(), abilities, player);
	}
}
