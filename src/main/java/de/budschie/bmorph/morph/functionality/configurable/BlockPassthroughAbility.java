package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashSet;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.events.MotionMultiplierEvent;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyTag;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockPassthroughAbility extends AbstractEventAbility
{
	public static Codec<BlockPassthroughAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(Codec.DOUBLE.fieldOf("speed_multiplier").forGetter(BlockPassthroughAbility::getWebSpeedMultiplier),
							ModCodecs.LAZY_BLOCK_TAGS.fieldOf("applies_to").forGetter(BlockPassthroughAbility::getAppliesTo))
					.apply(instance, BlockPassthroughAbility::new));
	
	private double webSpeedMultiplier;
	private UUID webSpeedUUID;
	private AttributeModifier am;
	private LazyTag<Block> appliesTo;
	
	// TODO: This is dumb.
	private HashSet<UUID> wasInWeb = new HashSet<>();
	
	// Custom block tags are probably not loaded in yet when this is being created; this causes this ability to only be loaded when we execute "/reload".
	// => do everything lazily.
	public BlockPassthroughAbility(double webSpeedMultiplier, LazyTag<Block> appliesTo)
	{
		this.webSpeedMultiplier = webSpeedMultiplier;
		this.webSpeedUUID = UUID.randomUUID();
		this.appliesTo = appliesTo;
		
		// Löööng name
		this.am = new AttributeModifier(webSpeedUUID, "web_speed_attribute_modifier", this.webSpeedMultiplier, Operation.MULTIPLY_BASE);
	}
	
	public LazyTag<Block> getAppliesTo()
	{
		return appliesTo;
	}
	
	public double getWebSpeedMultiplier()
	{
		return webSpeedMultiplier;
	}
	
	@SubscribeEvent
	public void onPlayerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			trackedPlayers.forEach(uuid -> 
			{
				// This is not particularly efficient.
				if(!wasInWeb.contains(uuid))
				{
					Player player = ServerSetup.server.getPlayerList().getPlayer(uuid);
					player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(am);
				}
				
				wasInWeb.clear();
			});
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

			if (!attributeInstance.hasModifier(am))
				attributeInstance.addTransientModifier(am);
			
			wasInWeb.add(player.getUUID());
		}
	}
	
	// We need this since this may cause issues like permanent speed boost (until world is loaded again) otherwise
	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		super.disableAbility(player, disabledItem);
		AttributeInstance attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
		
		if(attributeInstance.hasModifier(am))
			attributeInstance.removeModifier(am);
	}
}
