package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.events.WebEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.WebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

@Mixin(value = WebBlock.class)
public abstract class WebBlockMixin extends WebBlock
{
	public WebBlockMixin(Properties properties)
	{
		super(properties);
	}
	
	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;net/minecraft/world/World;net/minecraft/util/math/BlockPos;net/minecraft/entity/Entity)V", cancellable = true)
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, CallbackInfo info)
	{
		WebEvent webEvent = new WebEvent(new Vector3d(0.25D, (double)0.05F, 0.25D));
		MinecraftForge.EVENT_BUS.post(webEvent);
		
		if(webEvent.isDirty())
		{
			System.out.println("Cancelled and replaced by own implementation.");

			entityIn.setMotionMultiplier(state, webEvent.getNewWebSpeed());

			info.cancel();
		}
	}
}
