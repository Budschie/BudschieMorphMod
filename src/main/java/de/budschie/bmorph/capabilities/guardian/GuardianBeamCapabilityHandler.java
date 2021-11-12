package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

import de.budschie.bmorph.events.GuardianAbilityStatusUpdateEvent;
import de.budschie.bmorph.network.GuardianBeamAttack;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

@EventBusSubscriber
public class GuardianBeamCapabilityHandler
{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			event.player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
			{
				if(cap.getAttackedEntity().isPresent())
				{
					cap.setAttackProgression(cap.getAttackProgression() + 1);
					
					MinecraftForge.EVENT_BUS.post(new GuardianAbilityStatusUpdateEvent(event.player, cap));
				}
			});
		}
	}
	
	public static void attackServer(Player player, Entity toAttack, int maxAttackDuration)
	{
		player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.of(toAttack), maxAttackDuration);
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayer) player);
		});	
	}
	
	public static void unattackServer(Player player)
	{
		player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.empty(), 0);
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayer) player);
		});
	}
	
	public static void synchronizeWithClient(Player toSynchronize, ServerPlayer with)
	{
		toSynchronize.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			validateEntity(toSynchronize.getCommandSenderWorld(), cap);

			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> with),
					new GuardianBeamAttack.GuardianBeamAttackPacket(toSynchronize.getUUID(), cap.getAttackedEntity(), cap.getAttackProgression(), cap.getMaxAttackProgression()));
		});
	}
	
	public static void synchronizeWithClients(Player toSynchronize)
	{		
		toSynchronize.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			validateEntity(toSynchronize.getCommandSenderWorld(), cap);
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> toSynchronize),
					new GuardianBeamAttack.GuardianBeamAttackPacket(toSynchronize.getUUID(), cap.getAttackedEntity(), cap.getAttackProgression(), cap.getMaxAttackProgression()));
		});
	}
	
	private static void validateEntity(Level currentWorld, IGuardianBeamCapability cap)
	{
		if(cap.shouldRecalculateEntityId())
		{
			Entity entity = ((ServerLevel)currentWorld).getEntity(cap.getAttackedEntityServer().get());
			
			if(entity == null)
				cap.attackServer(Optional.empty(), 0);
			else
				cap.setAttackedEntity(Optional.of(entity.getId()));
		}
	}
}
