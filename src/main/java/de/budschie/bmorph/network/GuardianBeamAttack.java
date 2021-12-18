package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityInstance;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import de.budschie.bmorph.network.GuardianBeamAttack.GuardianBeamAttackPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;


public class GuardianBeamAttack extends CommonCapabilitySynchronizer<GuardianBeamAttackPacket, IGuardianBeamCapability>
{	
	public GuardianBeamAttack()
	{
		super(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP);
	}
	
	@Override
	public void encodeAdditional(GuardianBeamAttackPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.getEntity().isPresent());
		
		if(packet.getEntity().isPresent())
		{
			buffer.writeInt(packet.getEntity().get());
			buffer.writeInt(packet.getAttackProgression());
			buffer.writeInt(packet.getMaxAttackProgression());
		}
	}

	@Override
	public GuardianBeamAttackPacket decodeAdditional(FriendlyByteBuf buffer)
	{
		Optional<Integer> entity = Optional.empty();
		int attackProgression = 0;
		int maxAttackProgression = 0;
		
		if(buffer.readBoolean())
		{
			entity = Optional.of(buffer.readInt());
			attackProgression = buffer.readInt();
			maxAttackProgression = buffer.readInt();
		}
		
		return new GuardianBeamAttackPacket(entity, attackProgression, maxAttackProgression);
	}
	
	@Override
	public boolean handleCapabilitySync(GuardianBeamAttackPacket packet, Supplier<Context> ctx, Player player, IGuardianBeamCapability capabilityInterface)
	{
		capabilityInterface.setAttackedEntity(packet.getEntity());
		capabilityInterface.setAttackProgression(packet.getAttackProgression());
		capabilityInterface.setMaxAttackProgression(packet.getMaxAttackProgression());
		
		return true;
	}
	
	public static class GuardianBeamAttackPacket extends CommonCapabilitySynchronizerPacket
	{
		private Optional<Integer> entity;
		private int attackProgression;
		private int maxAttackProgression;
		
		public GuardianBeamAttackPacket(Optional<Integer> entity, int attackProgression, int maxAttackProgression)
		{
			this.entity = entity;
			this.attackProgression = attackProgression;
			this.maxAttackProgression = maxAttackProgression;
		}
		
		public Optional<Integer> getEntity()
		{
			return entity;
		}
		
		public int getAttackProgression()
		{
			return attackProgression;
		}
		
		public int getMaxAttackProgression()
		{
			return maxAttackProgression;
		}
	}
}
