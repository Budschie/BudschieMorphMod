package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SheepEatGrassAbility extends StunAbility
{
	public static final Codec<SheepEatGrassAbility> CODEC = RecordCodecBuilder.create(instance ->
	instance.group(SoundInstance.CODEC.fieldOf("shear_sound").forGetter(SheepEatGrassAbility::getShearSound), 
			SoundInstance.CODEC.fieldOf("consume_grass_sound").forGetter(SheepEatGrassAbility::getConsumeGrassSound),
			ModCodecs.DIRECTION_ENUM.listOf().fieldOf("valid_directions").forGetter(SheepEatGrassAbility::getValidEatingDirections),
			ModCodecs.BLOCKS.fieldOf("valid_eatable_block").forGetter(SheepEatGrassAbility::getFromBlock), 
			BlockStateProvider.CODEC.fieldOf("transform_to_block").forGetter(SheepEatGrassAbility::getToBlock),
			Codec.INT.fieldOf("stun").forGetter(SheepEatGrassAbility::getStun)).apply(instance, SheepEatGrassAbility::new));
	
	private SoundInstance shearSound;
	private SoundInstance consumeGrassSound;
	private List<Direction> validEatingDirections;
	private net.minecraft.world.level.block.Block fromBlock;
	private BlockStateProvider toBlock;
	
	public SheepEatGrassAbility(SoundInstance shearSound, SoundInstance consumeGrassSound, List<Direction> validEatingDirections,
			net.minecraft.world.level.block.Block fromBlock, BlockStateProvider toBlock, int stun)
	{
		super(stun);
		this.shearSound = shearSound;
		this.consumeGrassSound = consumeGrassSound;
		this.validEatingDirections = validEatingDirections;
		this.fromBlock = fromBlock;
		this.toBlock = toBlock;
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
				SheepCapabilityHandler.INSTANCE.setSheared(player, true);
				consumeGrassSound.playSoundAt(player);
				
				player.level.setBlockAndUpdate(result.getBlockPos(), toBlock.getState(player.getLevel().getRandom(), result.getBlockPos()));
				
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
				CompoundTag tag = new CompoundTag();
				morphCap.getCurrentMorph().get().deserializeAdditional(tag);

				// Get dye color of sheep
				currentDyeColor = DyeColor.byId(tag.getInt("Color"));
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
}
