package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.network.SquidBoost.SquidBoostPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class SquidBoost implements ISimpleImplPacket<SquidBoostPacket>
{
	@Override
	public void encode(SquidBoostPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(packet.getStrength());
		buffer.writeUUID(packet.getPlayer());
	}

	@Override
	public SquidBoostPacket decode(FriendlyByteBuf buffer)
	{
		return new SquidBoostPacket(buffer.readFloat(), buffer.readUUID());
	}

	@Override
	public void handle(SquidBoostPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
				Player potentialPlayer = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
				
				if(potentialPlayer != null)
				{
					potentialPlayer.setDeltaMovement(potentialPlayer.getDeltaMovement().add(potentialPlayer.getForward().multiply(packet.strength, packet.strength, packet.strength)));
					ctx.get().setPacketHandled(true);
				}
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