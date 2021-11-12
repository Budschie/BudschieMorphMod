package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.network.GuardianBeamAttack.GuardianBeamAttackPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class GuardianBeamAttack implements ISimpleImplPacket<GuardianBeamAttackPacket>
{

	@Override
	public void encode(GuardianBeamAttackPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayer());
		buffer.writeBoolean(packet.getEntity().isPresent());
		
		if(packet.getEntity().isPresent())
		{
			buffer.writeInt(packet.getEntity().get());
			buffer.writeInt(packet.getAttackProgression());
			buffer.writeInt(packet.getMaxAttackProgression());
		}
	}

	@Override
	public GuardianBeamAttackPacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		Optional<Integer> entity = Optional.empty();
		int attackProgression = 0;
		int maxAttackProgression = 0;
		
		if(buffer.readBoolean())
		{
			entity = Optional.of(buffer.readInt());
			attackProgression = buffer.readInt();
			maxAttackProgression = buffer.readInt();
		}
		
		return new GuardianBeamAttackPacket(player, entity, attackProgression, maxAttackProgression);
	}

	@Override
	public void handle(GuardianBeamAttackPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnlyShit.handleGuardianPacketClient(packet));
		});
	}
	
	public static class GuardianBeamAttackPacket
	{
		private UUID player;
		private Optional<Integer> entity;
		private int attackProgression;
		private int maxAttackProgression;
		
		public GuardianBeamAttackPacket(UUID player, Optional<Integer> entity, int attackProgression, int maxAttackProgression)
		{
			this.player = player;
			this.entity = entity;
			this.attackProgression = attackProgression;
			this.maxAttackProgression = maxAttackProgression;
		}
		
		public UUID getPlayer()
		{
			return player;
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
