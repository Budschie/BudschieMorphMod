package de.budschie.bmorph.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.GameRenderer;

public class UiUtils
{
//    bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
//    bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
//    bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
//    bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();

	
	public static void drawColoredRectangle(Matrix4f matrix, float x, float y, float x2, float y2, float r, float g, float b, float a)
	{
		float minX = Math.min(x, x2);
		float maxX = Math.max(x, x2);
		float minY = Math.min(y, y2);
		float maxY = Math.max(y, y2);

		
		BufferBuilder vertexUploader = Tesselator.getInstance().getBuilder();
		
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		
		vertexUploader.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
	      
		vertexUploader.vertex(matrix, minX, minY, 0).color(r, g, b, a).endVertex();
		vertexUploader.vertex(matrix, minX, maxY, 0).color(r, g, b, a).endVertex();
		vertexUploader.vertex(matrix, maxX, maxY, 0).color(r, g, b, a).endVertex();
		vertexUploader.vertex(matrix, maxX, minY, 0).color(r, g, b, a).endVertex();
		
		vertexUploader.end();
		BufferUploader.end(vertexUploader);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
