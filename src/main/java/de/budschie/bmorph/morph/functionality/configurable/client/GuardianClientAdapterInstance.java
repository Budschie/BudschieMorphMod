package de.budschie.bmorph.morph.functionality.configurable.client;

import java.util.function.Consumer;
import java.util.function.Predicate;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuardianClientAdapterInstance extends GuardianClientAdapter
{
	public GuardianClientAdapterInstance(Predicate<Entity> isTracked)
	{
		super(isTracked);
	}

	@Override
	public void enableAdapter()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disableAdapter()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	// Currently a bit choppy when you move, but we'll fix that later... probably.
	@SubscribeEvent
	public void onRenderGameEvent(RenderTickEvent event)
	{
		if(event.phase == Phase.START && Minecraft.getInstance().player != null && Minecraft.getInstance().world != null && isTracked(Minecraft.getInstance().player))
		{
			PlayerEntity player = Minecraft.getInstance().player;
			
			IGuardianBeamCapability cap = player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).resolve().orElse(null);
			
			if(cap != null && cap.getAttackedEntity().isPresent())
			{				
				// Look at the entity the whole time
				Entity toLookAt = Minecraft.getInstance().world.getEntityByID(cap.getAttackedEntity().get());
								
				if(toLookAt != null)
				{
					Minecraft.getInstance().player.lookAt(EntityAnchorArgument.Type.EYES, calculateSmoothedEntityPos(event.renderTickTime, toLookAt).add(0, toLookAt.getEntity().getEyeHeight(), 0));
				}
			}
		}
	}
	

	
	// I have the feeling that I am doing something wrong and that this code exists somewhere else already...
	private Vector3d calculateSmoothedEntityPos(float renderTicks, Entity entity)
	{
		return new Vector3d(MathHelper.lerp(renderTicks, entity.prevPosX, entity.getPosX()),
				MathHelper.lerp(renderTicks, entity.prevPosY, entity.getPosY()),
				MathHelper.lerp(renderTicks, entity.prevPosZ, entity.getPosZ()));
	}
	
	// This is some really cursed code holy fuck
	public static void createInstance(Consumer<GuardianClientAdapter> setter, Predicate<Entity> isTracked)
	{
		setter.accept(new GuardianClientAdapterInstance(isTracked));
	}

	@Override
	public void playGuardianSound(PlayerEntity player)
	{
		GuardianEntity guardian = (GuardianEntity) RenderHandler.getCachedEntity(player);
		
		Minecraft.getInstance().getSoundHandler().play(new GuardianSound(guardian));
	}
}
