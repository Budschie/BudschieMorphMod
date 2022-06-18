package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.events.MotionMultiplierEvent;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyTag;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.morph.functionality.configurable.client.BlockPassthroughAbilityAdapter;
import de.budschie.bmorph.morph.functionality.configurable.client.IBlockPassthroughAbilityAdapter;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class BlockPassthroughAbility extends Ability
{
	public static final Codec<BlockPassthroughAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(Codec.DOUBLE.fieldOf("speed_multiplier").forGetter(BlockPassthroughAbility::getWebSpeedMultiplier),
							ModCodecs.LAZY_BLOCK_TAGS.fieldOf("applies_to").forGetter(BlockPassthroughAbility::getAppliesTo))
					.apply(instance, BlockPassthroughAbility::new));
	
	private double webSpeedMultiplier;
	private LazyOptional<AttributeModifier> am;
	private LazyTag<Block> appliesTo;
	private Optional<IBlockPassthroughAbilityAdapter> adapter;
	
	// TODO: This is dumb.
	private HashSet<UUID> wasInWeb = new HashSet<>();
	
	// Custom block tags are probably not loaded in yet when this is being created; this causes this ability to only be loaded when we execute "/reload".
	// => do everything lazily.
	public BlockPassthroughAbility(double webSpeedMultiplier, LazyTag<Block> appliesTo)
	{
		this.webSpeedMultiplier = webSpeedMultiplier;
		this.appliesTo = appliesTo;
		
		// Löööng name
		this.am = LazyOptional.of(() -> new AttributeModifier(UUID.nameUUIDFromBytes((this.getResourceLocation().toString() + "_web_speed_attribute_modifier").getBytes()), "web_speed_attribute_modifier", this.webSpeedMultiplier, Operation.MULTIPLY_BASE));
		
		this.adapter = Optional.empty();
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			this.adapter = Optional.of(new BlockPassthroughAbilityAdapter());
			this.adapter.get().setAbilty(this);
		});
	}
	
	public LazyTag<Block> getAppliesTo()
	{
		return appliesTo;
	}
	
	public double getWebSpeedMultiplier()
	{
		return webSpeedMultiplier;
	}
	
	@Override
	public void onRegister()
	{
		super.onRegister();
		
		if(this.adapter.isPresent())
			this.adapter.get().register();
	}
	
	@Override
	public void onUnregister()
	{
		super.onUnregister();
		
		if(this.adapter.isPresent())
			this.adapter.get().unregister();
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			trackedPlayers.forEach(uuid -> 
			{
				// This is not particularly efficient.
				if(!wasInWeb.contains(uuid))
				{
					Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
					player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(am.resolve().get());
				}				
			});
			
			wasInWeb.clear();
		}
	}
	
	@SubscribeEvent
	public void onMotionMultiplierEvent(MotionMultiplierEvent event)
	{
		if(isTracked(event.getEntity()) && appliesTo.test(event.getBlockState().getBlock()))
		{
			event.setCanceled(true);
			
			Player player = (Player) event.getEntity();

			AttributeInstance attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);

			if (!attributeInstance.hasModifier(am.resolve().get()))
				attributeInstance.addTransientModifier(am.resolve().get());
			
			wasInWeb.add(player.getUUID());
		}
	}
	
	public boolean isInWeb(Player player)
	{
		return player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(am.resolve().get());
	}
	
	public float getSpeedMultiplier()
	{
		return (float) am.resolve().get().getAmount();
	}
	
	// We need this since this may cause issues like permanent speed boost (until world is loaded again) otherwise
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		AttributeInstance attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
		
		if(attributeInstance.hasModifier(am.resolve().get()))
			attributeInstance.removeModifier(am.resolve().get());
		
		wasInWeb.remove(player.getUUID());
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
