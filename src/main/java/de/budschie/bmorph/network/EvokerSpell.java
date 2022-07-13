package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.capabilities.evoker.EvokerSpellCapabilityInstance;
import de.budschie.bmorph.capabilities.evoker.IEvokerSpellCapability;
import de.budschie.bmorph.network.EvokerSpell.EvokerSpellPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class EvokerSpell extends CommonCapabilitySynchronizer<EvokerSpellPacket, IEvokerSpellCapability>
{
	public EvokerSpell()
	{
		super(EvokerSpellCapabilityInstance.EVOKER_SPELL_CAP);
	}

	@Override
	public void encodeAdditional(EvokerSpellPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getSpellTicksLeft());
	}

	@Override
	public EvokerSpellPacket decodeAdditional(FriendlyByteBuf buffer)
	{
		return new EvokerSpellPacket(buffer.readInt());
	}

	@Override
	public boolean handleCapabilitySync(EvokerSpellPacket packet, Supplier<Context> ctx, Player player, IEvokerSpellCapability capabilityInterface)
	{
		capabilityInterface.setCastingTicks(packet.getSpellTicksLeft());
		
		return true;
	}
	
	public static class EvokerSpellPacket extends CommonCapabilitySynchronizerPacket
	{
		private int spellTicksLeft;
		
		public EvokerSpellPacket(int spellTicksLeft)
		{
			this.spellTicksLeft = spellTicksLeft;
		}

		public int getSpellTicksLeft()
		{
			return spellTicksLeft;
		}

		public void setSpellTicksLeft(int spellTicksLeft)
		{
			this.spellTicksLeft = spellTicksLeft;
		}
	}
}
