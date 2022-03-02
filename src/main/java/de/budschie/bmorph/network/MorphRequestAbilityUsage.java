package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.network.MorphRequestAbilityUsage.MorphRequestAbilityUsagePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRequestAbilityUsage implements ISimpleImplPacket<MorphRequestAbilityUsagePacket>
{
	@Override
	public void encode(MorphRequestAbilityUsagePacket packet, FriendlyByteBuf buffer)
	{
		
	}

	@Override
	public MorphRequestAbilityUsagePacket decode(FriendlyByteBuf buffer)
	{
		return new MorphRequestAbilityUsagePacket();
	}

	@Override
	public void handle(MorphRequestAbilityUsagePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = ctx.get().getSender().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.useAbility();
				
				ctx.get().setPacketHandled(true);
			}
		});
	}
	
	public static class MorphRequestAbilityUsagePacket
	{
		public MorphRequestAbilityUsagePacket()
		{
			
		}
	}
}
