package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.sheep.SheepCapabilityHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SheepEatGrassAbility extends StunAbility
{
	public static final Codec<SheepEatGrassAbility> CODEC = RecordCodecBuilder.create(instance ->
	instance.group(
			SoundInstance.CODEC.fieldOf("shear_sound").forGetter(SheepEatGrassAbility::getShearSound), 
			SoundInstance.CODEC.fieldOf("consume_grass_sound").forGetter(SheepEatGrassAbility::getConsumeGrassSound),
			ModCodecs.DIRECTION_ENUM.listOf().fieldOf("valid_directions").forGetter(SheepEatGrassAbility::getValidEatingDirections),
			ModCodecs.BLOCKS.fieldOf("valid_eatable_block").forGetter(SheepEatGrassAbility::getFromBlock), 
			BlockStateProvider.CODEC.fieldOf("transform_to_block").forGetter(SheepEatGrassAbility::getToBlock),
			Codec.INT.fieldOf("stun").forGetter(SheepEatGrassAbility::getStun),
			Codec.INT.fieldOf("gain_food_level").forGetter(SheepEatGrassAbility::getGainFoodLevel),
			Codec.FLOAT.fieldOf("gain_saturation").forGetter(SheepEatGrassAbility::getGainSaturation))
	.apply(instance, SheepEatGrassAbility::new));

	private HashSet<UUID> trackedPlayers = new HashSet<>();
	
	private SoundInstance shearSound;
	private SoundInstance consumeGrassSound;
	private List<Direction> validEatingDirections;
	private net.minecraft.world.level.block.Block fromBlock;
	private BlockStateProvider toBlock;
	private int gainFoodLevel;
	private float gainSaturation;
	
	public SheepEatGrassAbility(SoundInstance shearSound, SoundInstance consumeGrassSound, List<Direction> validEatingDirections,
			net.minecraft.world.level.block.Block fromBlock, BlockStateProvider toBlock, int stun, int gainFoodLevel, float gainSaturation)
	{
		super(stun);
		this.shearSound = shearSound;
		this.consumeGrassSound = consumeGrassSound;
		this.validEatingDirections = validEatingDirections;
		this.fromBlock = fromBlock;
		this.toBlock = toBlock;
		
		this.gainFoodLevel = gainFoodLevel;
		this.gainSaturation = gainSaturation;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		trackedPlayers.add(player.getUUID());
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		trackedPlayers.remove(player.getUUID());
	}
	
	@Override
	public void onRegister()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onUnregister()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	public boolean isTracked(Entity entity)
	{
		return trackedPlayers.contains(entity.getUUID());
	}
	
	@SubscribeEvent
	public void onShearedPlayer(EntityInteract event)
	{
		if(event.getItemStack().getItem() instanceof ShearsItem && event.getTarget() instanceof Player && isTracked(event.getTarget()) && !SheepCapabilityHandler.INSTANCE.isSheared((Player) event.getTarget()))
		{
			event.setCancellationResult(InteractionResult.SUCCESS);
			
			if(!event.getWorld().isClientSide())
			{
				Player target = (Player) event.getTarget();
				
				shear(target);
				
				event.getItemStack().hurtAndBreak(1, event.getPlayer(), (player) ->
				{
					player.broadcastBreakEvent(event.getHand());
				});
			}
		}
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(player.isCrouching() && !SheepCapabilityHandler.INSTANCE.isSheared(player))
		{
			shear(player);
		}
		else if(player.getFoodData().needsFood() && !isCurrentlyStunned(player.getUUID()))
		{
			// Ray trace to find block
			Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
			Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).multiply(4, 4, 4).add(from);
			
			ClipContext context = new ClipContext(from, to, Block.VISUAL, Fluid.NONE, null);
			
			BlockHitResult result = player.level.clip(context);
			
			// Check if we are hitting a specific block from the right direction
			if (result != null && result.getType() == Type.BLOCK && validEatingDirections.contains(result.getDirection())
					&& player.level.getBlockState(result.getBlockPos()).getBlock() == fromBlock)
			{
				// Set sheared to true, play sound and do a block update.
				SheepCapabilityHandler.INSTANCE.setSheared(player, false);
				consumeGrassSound.playSoundAt(player);
				
				player.level.setBlockAndUpdate(result.getBlockPos(), toBlock.getState(player.getLevel().getRandom(), result.getBlockPos()));
				
				player.getFoodData().eat(gainFoodLevel, gainSaturation);
				
				stun(player.getUUID());
			}
		}
	}
	
	private void shear(Player playerToBeSheared)
	{
		DyeColor currentDyeColor = DyeColor.WHITE;
		shearSound.playSoundAt(playerToBeSheared);
		
		IMorphCapability morphCap = MorphUtil.getCapOrNull(playerToBeSheared);
		
		if(morphCap != null)
		{
			if(morphCap.getCurrentMorph().isPresent())
			{
				// Get dye color of sheep
				currentDyeColor = DyeColor.byId(morphCap.getCurrentMorph().get().serializeAdditional().getByte("Color"));
			}
		}
		
		int i = 1 + playerToBeSheared.getRandom().nextInt(3);

		for (int j = 0; j < i; ++j)
		{
			ItemEntity itementity = playerToBeSheared.spawnAtLocation(Sheep.ITEM_BY_DYE.get(currentDyeColor), 1);
			
			if (itementity != null)
			{
				itementity.setDeltaMovement(itementity.getDeltaMovement().add(((playerToBeSheared.getRandom().nextFloat() - playerToBeSheared.getRandom().nextFloat()) * 0.1),
						playerToBeSheared.getRandom().nextDouble() * 0.05D, ((playerToBeSheared.getRandom().nextDouble() - playerToBeSheared.getRandom().nextDouble()) * 0.1)));
			}
		}
		
		SheepCapabilityHandler.INSTANCE.setSheared(playerToBeSheared, true);
	}

	public SoundInstance getShearSound()
	{
		return shearSound;
	}

	public SoundInstance getConsumeGrassSound()
	{
		return consumeGrassSound;
	}

	public List<Direction> getValidEatingDirections()
	{
		return validEatingDirections;
	}

	public net.minecraft.world.level.block.Block getFromBlock()
	{
		return fromBlock;
	}

	public BlockStateProvider getToBlock()
	{
		return toBlock;
	}
	
	public int getGainFoodLevel()
	{
		return gainFoodLevel;
	}
	
	public float getGainSaturation()
	{
		return gainSaturation;
	}
}
