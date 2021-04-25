package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.network.SquidBoost.SquidBoostPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SquidBoost implements ISimpleImplPacket<SquidBoostPacket>
{
	@Override
	public void encode(SquidBoostPacket packet, PacketBuffer buffer)
	{
		buffer.writeFloat(packet.getStrength());
		buffer.writeUniqueId(packet.getPlayer());
	}

	@Override
	public SquidBoostPacket decode(PacketBuffer buffer)
	{
		return new SquidBoostPacket(buffer.readFloat(), buffer.readUniqueId());
	}

	@Override
	public void handle(SquidBoostPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			PlayerEntity potentialPlayer = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
			
			if(potentialPlayer != null)
			{
				potentialPlayer.setMotion(potentialPlayer.getMotion().add(potentialPlayer.getForward().mul(packet.strength, packet.strength, packet.strength)));
			}
		});
	}
	
	public static class SquidBoostPacket
	{
		float strength;
		UUID player;
		
		public SquidBoostPacket(float strength, UUID player)
		{
			this.strength = strength;
			this.player = player;
		}
		
		public float getStrength()
		{
			return strength;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
	}
}