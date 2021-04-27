package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.AbilityLookupTableHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

// This is just a stupid class which goal is to unify the code for morphing
public class MorphUtil
{
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
			
			boolean isCanceled = MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Pre(player, resolved, aboutToMorphTo));
			
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
					resolved.setCurrentAbilities(AbilityLookupTableHandler.getAbilitiesFor(resolved.getCurrentMorph().get()));
				else
					resolved.setCurrentAbilities(null);
				
				resolved.applyAbilities(player);
				resolved.syncMorphChange(player);
				resolved.applyHealthOnPlayer(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, resolved, aboutToMorphTo));
			}
		}
	}
	
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
				
				IForgeRegistry<Ability> registry = GameRegistry.findRegistry(Ability.class);
				
				for(String name : abilities)
				{
					ResourceLocation resourceLocation = new ResourceLocation(name);
					resolvedAbilities.add(registry.getValue(resourceLocation));
				}
				
				resolved.setCurrentAbilities(resolvedAbilities);
				resolved.applyAbilities(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Client.Post(player, resolved, aboutToMorphTo));
			}
			else
				System.out.println("Could not synchronize data, as the morph cap is not created yet.");
		}
	}
}
