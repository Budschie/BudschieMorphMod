package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphChangedSynchronizer.MorphChangedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphChangedSynchronizer implements ISimpleImplPacket<MorphChangedPacket>
{
	@Override
	public void encode(MorphChangedPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayerUUID());
		buffer.writeResourceLocation(packet.getReason());
		buffer.writeBoolean(packet.getMorphItem().isPresent());
		packet.getMorphItem().ifPresent(item -> buffer.writeNbt(item.serialize()));
		buffer.writeInt(packet.getAbilities().size());
		
		for(String str : packet.getAbilities())
			buffer.writeUtf(str);
	}

	@Override
	public MorphChangedPacket decode(FriendlyByteBuf buffer)
	{
		UUID playerUUID = buffer.readUUID();
		ResourceLocation reason = buffer.readResourceLocation();
		boolean hasItem = buffer.readBoolean();
		
		Optional<MorphItem> morphItem = Optional.empty();
		
		if(hasItem)
			morphItem = Optional.of(MorphHandler.deserializeMorphItem(buffer.readNbt()));
		
		int amountOfAbilities = buffer.readInt();
		
		ArrayList<String> abilities = new ArrayList<>(amountOfAbilities);
		
		for(int i = 0; i < amountOfAbilities; i++)
			abilities.add(buffer.readUtf());
		
		return new MorphChangedPacket(playerUUID, morphItem, reason, abilities);
	}

	@Override
	public void handle(MorphChangedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			MorphReason reason = MorphReasonRegistry.REGISTRY.get().getValue(packet.getReason());
			
			if(Minecraft.getInstance().level != null)
			{
				MorphUtil.morphToClient(packet.getMorphItem(), reason == null ? MorphReasonRegistry.MORPHED_BY_COMMAND.get() : reason, packet.getAbilities(), Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayerUUID()));
				ctx.get().setPacketHandled(true);
			}
		});
	}
	
	public static class MorphChangedPacket
	{
		UUID playerUUID;
		Optional<MorphItem> morphItem;
		ResourceLocation reason;
		ArrayList<String> abilities; 
		
		public MorphChangedPacket(UUID playerUUID, Optional<MorphItem> morphItem, ResourceLocation reason, ArrayList<String> abilities)
		{
			this.playerUUID = playerUUID;
			this.morphItem = morphItem;
			this.reason = reason;
			this.abilities = abilities;
		}
		
		public Optional<MorphItem> getMorphItem()
		{
			return morphItem;
		}
		
		public ArrayList<String> getAbilities()
		{
			return abilities;
		}
		
		public UUID getPlayerUUID()
		{
			return playerUUID;
		}
		
		public ResourceLocation getReason()
		{
			return reason;
		}
	}
}
