package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRequestFavouriteChange implements ISimpleImplPacket<MorphRequestFavouriteChangePacket>
{
	@Override
	public void encode(MorphRequestFavouriteChangePacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.shouldAdd());
		buffer.writeInt(packet.getIndexInMorphArray());
	}

	@Override
	public MorphRequestFavouriteChangePacket decode(FriendlyByteBuf buffer)
	{
		return new MorphRequestFavouriteChangePacket(buffer.readBoolean(), buffer.readInt());
	}

	@Override
	public void handle(MorphRequestFavouriteChangePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = ctx.get().getSender().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
								
				if(resolved.getMorphList().getMorphArrayList().size() < packet.getIndexInMorphArray() || packet.getIndexInMorphArray() < 0)
					System.out.println("Player " + ctx.get().getSender().getName().getString() + " with UUID " + ctx.get().getSender().getUUID() + " has tried to cause an ArrayIndexOutOfBoundsException! Shame on you!");
				else
				{
					FavouriteList favouriteList = resolved.getFavouriteList();
					
					if(packet.shouldAdd())
						favouriteList.addFavourite(packet.getIndexInMorphArray());
					else
						favouriteList.removeFavourite(packet.getIndexInMorphArray());
					
					ctx.get().setPacketHandled(true);
				}
			}
		});
	}
	
	public static class MorphRequestFavouriteChangePacket
	{
		private boolean add;
		private int indexInMorphArray;
		
		public MorphRequestFavouriteChangePacket(boolean add, int indexInMorphArray)
		{
			this.add = add;
			this.indexInMorphArray = indexInMorphArray;
		}
		
		public boolean shouldAdd()
		{
			return add;
		}
		
		public int getIndexInMorphArray()
		{
			return indexInMorphArray;
		}
	}
}
