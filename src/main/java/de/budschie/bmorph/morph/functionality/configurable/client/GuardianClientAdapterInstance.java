package de.budschie.bmorph.morph.functionality.configurable.client;

import java.util.function.Consumer;
import java.util.function.Predicate;

import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityInstance;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
		if(event.phase == Phase.START && Minecraft.getInstance().player != null && Minecraft.getInstance().level != null && isTracked(Minecraft.getInstance().player))
		{
			Player player = Minecraft.getInstance().player;
			
			IGuardianBeamCapability cap = player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).resolve().orElse(null);
			
			if(cap != null && cap.getAttackedEntity().isPresent())
			{				
				// Look at the entity the whole time
				Entity toLookAt = Minecraft.getInstance().level.getEntity(cap.getAttackedEntity().get());
								
				if(toLookAt != null)
				{
					Minecraft.getInstance().player.lookAt(EntityAnchorArgument.Anchor.EYES, calculateSmoothedEntityPos(Minecraft.getInstance().getDeltaFrameTime(), toLookAt).add(0, toLookAt.getEyeHeight(), 0));
				}
			}
		}
	}
	

	
	// I have the feeling that I am doing something wrong and that this code exists somewhere else already...
	private Vec3 calculateSmoothedEntityPos(float renderTicks, Entity entity)
	{
		return new Vec3(Mth.lerp(renderTicks, entity.xOld, entity.getX()),
				Mth.lerp(renderTicks, entity.yOld, entity.getY()),
				Mth.lerp(renderTicks, entity.zOld, entity.getZ()));
	}
	
	// This is some really cursed code holy fuck
	public static void createInstance(Consumer<GuardianClientAdapter> setter, Predicate<Entity> isTracked)
	{
		setter.accept(new GuardianClientAdapterInstance(isTracked));
	}

	@Override
	public void playGuardianSound(Player player)
	{
		Minecraft.getInstance().getSoundManager().play(new GuardianAttackSoundInstance((Guardian) player.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get().getOrCreateCachedEntity(player)));
	}
}
