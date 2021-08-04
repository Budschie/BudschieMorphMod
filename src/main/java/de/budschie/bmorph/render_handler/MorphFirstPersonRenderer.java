package de.budschie.bmorph.render_handler;

import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

/** Super shitty shit **/
public class MorphFirstPersonRenderer extends FirstPersonRenderer
{
	Optional<BipedRenderer<? super MobEntity, ?>> model = Optional.empty();
	
	public MorphFirstPersonRenderer(Minecraft mcIn)
	{
		super(mcIn);
	}
	
	@Override
	public void renderArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, HandSide side)
	{
		if(model.isPresent())
		{
	      matrixStackIn.push();
	      float f = side == HandSide.RIGHT ? 1.0F : -1.0F;
	      matrixStackIn.rotate(Vector3f.YP.rotationDegrees(92.0F));
	      matrixStackIn.rotate(Vector3f.XP.rotationDegrees(45.0F));
	      matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f * -41.0F));
	      matrixStackIn.translate((double)(f * 0.3F), (double)-1.1F, (double)0.45F);
	      
			model.get().entityModel.bipedLeftArm.render(matrixStackIn,
					bufferIn.getBuffer(RenderType.getEntityCutout(model.get().getEntityTexture(
							(MobEntity) RenderHandler.cachedEntities.get(Minecraft.getInstance().player.getUniqueID())))),
					combinedLightIn, combinedLightIn);

	      matrixStackIn.pop();
		}
	}
	
	public void setModel(Optional<BipedRenderer<? super MobEntity, ?>> model)
	{
		this.model = model;
	}
}
