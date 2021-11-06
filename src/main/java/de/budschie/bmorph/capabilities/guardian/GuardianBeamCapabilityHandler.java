package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

import de.budschie.bmorph.events.GuardianAbilityStatusUpdateEvent;
import de.budschie.bmorph.network.GuardianBeamAttack;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

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
	
	public static void attackServer(PlayerEntity player, Entity toAttack, int maxAttackDuration)
	{
		player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.of(toAttack), maxAttackDuration);
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayerEntity) player);
		});	
	}
	
	public static void unattackServer(PlayerEntity player)
	{
		player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			cap.attackServer(Optional.empty(), 0);
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayerEntity) player);
		});
	}
	
	public static void synchronizeWithClient(PlayerEntity toSynchronize, ServerPlayerEntity with)
	{
		toSynchronize.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			validateEntity(toSynchronize.getEntityWorld(), cap);

			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> with),
					new GuardianBeamAttack.GuardianBeamAttackPacket(toSynchronize.getUniqueID(), cap.getAttackedEntity(), cap.getAttackProgression(), cap.getMaxAttackProgression()));
		});
	}
	
	public static void synchronizeWithClients(PlayerEntity toSynchronize)
	{		
		toSynchronize.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			validateEntity(toSynchronize.getEntityWorld(), cap);
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> toSynchronize),
					new GuardianBeamAttack.GuardianBeamAttackPacket(toSynchronize.getUniqueID(), cap.getAttackedEntity(), cap.getAttackProgression(), cap.getMaxAttackProgression()));
		});
	}
	
	private static void validateEntity(World currentWorld, IGuardianBeamCapability cap)
	{
		if(cap.shouldRecalculateEntityId())
		{
			Entity entity = ((ServerWorld)currentWorld).getEntityByUuid(cap.getAttackedEntityServer().get());
			
			if(entity == null)
				cap.attackServer(Optional.empty(), 0);
			else
				cap.setAttackedEntity(Optional.of(entity.getEntityId()));
		}
	}
}
