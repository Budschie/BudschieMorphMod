package de.budschie.bmorph.network;

import java.util.UUID;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import de.budschie.bmorph.network.GuardianBeamAttack.GuardianBeamAttackPacket;
import de.budschie.bmorph.network.PufferfishPuff.PufferfishPuffPacket;
import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientOnlyShit
{
	public static void handlePufferfishPacketClient(PufferfishPuffPacket packet)
	{
		if(Minecraft.getInstance().level != null)
		{
			Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
	
			if (player != null)
			{
				player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
				{
					cap.setOriginalPuffTime(packet.getOriginalDuration());
					cap.setPuffTime(packet.getDuration());
				});
			}
		}
	}
	
	public static void handleGuardianPacketClient(GuardianBeamAttackPacket packet)
	{
		if(Minecraft.getInstance().level != null)
		{
			Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
	
			if (player != null)
			{
				player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).ifPresent(cap ->
				{
					cap.setAttackedEntity(packet.getEntity());
					cap.setAttackProgression(packet.getAttackProgression());
					cap.setMaxAttackProgression(packet.getMaxAttackProgression());
				});
			}
		}
	}
	
	public static void disposePlayerMorphData(UUID playerToDispose)
	{
		RenderHandler.disposePlayerMorphData(playerToDispose);
	}
}
