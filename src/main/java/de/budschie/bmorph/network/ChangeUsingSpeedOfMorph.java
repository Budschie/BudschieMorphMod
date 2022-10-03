package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.speed_of_morph_cap.IPlayerUsingSpeedOfMorph;
import de.budschie.bmorph.capabilities.speed_of_morph_cap.PlayerUsingSpeedOfMorphInstance;
import de.budschie.bmorph.network.ChangeUsingSpeedOfMorph.ChangeUsingSpeedOfMorphPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class ChangeUsingSpeedOfMorph implements ISimpleImplPacket<ChangeUsingSpeedOfMorphPacket>
{
	public static record ChangeUsingSpeedOfMorphPacket(boolean isUsingSpeedOfMorph)
	{
		
	}

	@Override
	public void encode(ChangeUsingSpeedOfMorphPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.isUsingSpeedOfMorph());
	}

	@Override
	public ChangeUsingSpeedOfMorphPacket decode(FriendlyByteBuf buffer)
	{
		return new ChangeUsingSpeedOfMorphPacket(buffer.readBoolean());
	}

	@Override
	public void handle(ChangeUsingSpeedOfMorphPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().player != null)
			{
				LazyOptional<IPlayerUsingSpeedOfMorph> usingSpeedOfMorph = Minecraft.getInstance().player.getCapability(PlayerUsingSpeedOfMorphInstance.SPEED_OF_MORPH_CAP);
				
				if(usingSpeedOfMorph.isPresent())
				{
					usingSpeedOfMorph.resolve().get().setUsingSpeedOfMorph(packet.isUsingSpeedOfMorph());
				}
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
}
