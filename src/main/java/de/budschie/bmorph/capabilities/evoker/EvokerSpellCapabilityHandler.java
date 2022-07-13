package de.budschie.bmorph.capabilities.evoker;

import java.util.ArrayList;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.network.EvokerSpell.EvokerSpellPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class EvokerSpellCapabilityHandler extends CommonCapabilityHandler<IEvokerSpellCapability, EvokerSpellPacket>
{
	public static final EvokerSpellCapabilityHandler INSTANCE = new EvokerSpellCapabilityHandler();
	
	public EvokerSpellCapabilityHandler()
	{
		super(EvokerSpellCapabilityInstance.EVOKER_SPELL_CAP);
	}

	@Override
	protected EvokerSpellPacket createPacket(Player player, IEvokerSpellCapability capability)
	{
		return new EvokerSpellPacket(capability.getCastingTicksLeft());
	}
	
	public void useSpellServer(Player player, int spellDuration, int fangsTimePoint, double range)
	{
		LazyOptional<IEvokerSpellCapability> lazyCap = player.getCapability(EvokerSpellCapabilityInstance.EVOKER_SPELL_CAP);
		
		if(lazyCap.isPresent())
		{
			IEvokerSpellCapability cap = lazyCap.resolve().get();
			
			cap.setCastingTicks(spellDuration);
			cap.setRange(range);
			cap.setFangsTimePoint(fangsTimePoint);
			
			this.synchronizeWithClients(player);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			LazyOptional<IEvokerSpellCapability> lazyCap = event.player.getCapability(EvokerSpellCapabilityInstance.EVOKER_SPELL_CAP);
			
			if(lazyCap.isPresent())
			{
				IEvokerSpellCapability cap = lazyCap.resolve().get();
				
				if(cap.getCastingTicksLeft() > 0)
				{
					// If the player is about to finish their casting animation and the server ticks, evoker fangs
					// should be summoned, but *only* on the server.
					if(cap.getCastingTicksLeft() == cap.getFangsTimePoint() && !event.player.level.isClientSide())
					{
						createFangs(event.player, cap.getRange());
					}
					
					cap.setCastingTicks(cap.getCastingTicksLeft() - 1);
				}
			}
		}
	}
	
	private static void createFangs(Player player, double range)
	{
		Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
		Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).multiply(range, range, range).add(from);
		
		AABB aabb = new AABB(from, to);
		
		EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(player.level, player, from, to, aabb, entity -> entity instanceof LivingEntity);
		ClipContext context = new ClipContext(from, to, Block.VISUAL, Fluid.NONE, null);
		
		BlockHitResult blockResult = player.level.clip(context);
		
		ArrayList<Vec3> targets = new ArrayList<>();
		
		if(entityResult != null && entityResult.getEntity() != null)
		{
			targets.add(entityResult.getEntity().position());
		}
		
		if(blockResult != null && blockResult.getType() == Type.BLOCK)
		{
			targets.add(blockResult.getLocation());
		}
		
		if(targets.size() > 0)
		{
			targets.sort((pos1, pos2) -> (player.distanceToSqr(pos1) < player.distanceToSqr(pos2)) ? 1 : -1);
			performSpellCasting(player, targets.get(0));
		}
	}
	
	private static void performSpellCasting(Player player, Vec3 target)
	{
		double d0 = Math.min(target.y(), player.getY());
		double d1 = Math.max(target.y(), player.getY()) + 1.0D;
		float f = (float) Mth.atan2(target.z() - player.getZ(), target.x() - player.getX());
		if (player.distanceToSqr(target) < 9.0D)
		{
			for (int i = 0; i < 5; ++i)
			{
				float f1 = f + i * (float) Math.PI * 0.4F;
				createSpellEntity(player, player.getX() + Mth.cos(f1) * 1.5D, player.getZ() + Mth.sin(f1) * 1.5D, d0, d1, f1, 0);
			}

			for (int k = 0; k < 8; ++k)
			{
				float f2 = f + k * (float) Math.PI * 2.0F / 8.0F + 1.2566371F;
				createSpellEntity(player, player.getX() + Mth.cos(f2) * 2.5D, player.getZ() + Mth.sin(f2) * 2.5D, d0, d1, f2, 3);
			}
		} 
		else
		{
			for (int l = 0; l < 16; ++l)
			{
				double d2 = 1.25D * (l + 1);
				int j = 1 * l;
				createSpellEntity(player, player.getX() + Mth.cos(f) * d2, player.getZ() + Mth.sin(f) * d2, d0, d1, f, j);
			}
		}

	}

	private static void createSpellEntity(Player player, double p_32673_, double p_32674_, double p_32675_, double p_32676_, float p_32677_, int p_32678_)
	{
		BlockPos blockpos = new BlockPos(p_32673_, p_32676_, p_32674_);
		boolean flag = false;
		double d0 = 0.0D;

		do
		{
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = player.level.getBlockState(blockpos1);
			if (blockstate.isFaceSturdy(player.level, blockpos1, Direction.UP))
			{
				if (!player.level.isEmptyBlock(blockpos))
				{
					BlockState blockstate1 = player.level.getBlockState(blockpos);
					VoxelShape voxelshape = blockstate1.getCollisionShape(player.level, blockpos);
					if (!voxelshape.isEmpty())
					{
						d0 = voxelshape.max(Direction.Axis.Y);
					}
				}

				flag = true;
				break;
			}

			blockpos = blockpos.below();
		} 
		while (blockpos.getY() >= Mth.floor(p_32675_) - 1);

		if (flag)
		{
			player.level.addFreshEntity(new EvokerFangs(player.level, p_32673_, blockpos.getY() + d0, p_32674_, p_32677_, p_32678_, player));
		}
	}

}
