package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.events.GuardianAbilityStatusUpdateEvent;
import de.budschie.bmorph.network.GuardianBeamAttack.GuardianBeamAttackPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class GuardianBeamCapabilityHandler extends CommonCapabilityHandler<IGuardianBeamCapability, GuardianBeamAttackPacket>
{
	public static final GuardianBeamCapabilityHandler INSTANCE = new GuardianBeamCapabilityHandler();
	
	public GuardianBeamCapabilityHandler()
	{
		super(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			event.player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).ifPresent(cap ->
			{
				if(cap.getAttackedEntity().isPresent())
				{
					cap.setAttackProgression(cap.getAttackProgression() + 1);
					
					MinecraftForge.EVENT_BUS.post(new GuardianAbilityStatusUpdateEvent(event.player, cap, false));
				}
			});
		}
	}
	
	public void attackServer(Player player, Entity toAttack, int maxAttackDuration)
	{
		player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.of(toAttack), maxAttackDuration);
			
			synchronizeWithClients(player);
		});	
	}
	
	public void unattackServer(Player player)
	{
		player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.empty(), 0);
			
			synchronizeWithClients(player);
		});
	}
	
	private static void validateEntity(Player player, IGuardianBeamCapability cap)
	{
		if(cap.shouldRecalculateEntityId())
		{
			Entity entity = ((ServerLevel)player.level).getEntity(cap.getAttackedEntityServer().get());
			
			if(entity == null)
			{
				cap.attackServer(Optional.empty(), 0);
				MinecraftForge.EVENT_BUS.post(new GuardianAbilityStatusUpdateEvent(player, cap, true));
			}
			else
				cap.setAttackedEntity(Optional.of(entity.getId()));
		}
	}

	@Override
	protected GuardianBeamAttackPacket createPacket(Player player, IGuardianBeamCapability capability)
	{
		validateEntity(player, capability);
		
		return new GuardianBeamAttackPacket(capability.getAttackedEntity(), capability.getAttackProgression(), capability.getMaxAttackProgression());
	}
}
