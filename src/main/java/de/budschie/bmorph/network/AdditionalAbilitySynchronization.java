package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.AdditionalAbilitySynchronization.AdditionalAbilitySynchronizationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class AdditionalAbilitySynchronization implements ISimpleImplPacket<AdditionalAbilitySynchronizationPacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(AdditionalAbilitySynchronizationPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayer());
		buffer.writeBoolean(packet.shouldAdd());
		buffer.writeInt(packet.getAbilities().length);
		
		for(String ability : packet.getAbilities())
		{
			buffer.writeUtf(ability);
		}
	}

	@Override
	public AdditionalAbilitySynchronizationPacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		boolean add = buffer.readBoolean();
		int abilityLength = buffer.readInt();
		
		String[] abilities = new String[abilityLength];
		
		for(int i = 0; i < abilityLength; i++)
			abilities[i] = buffer.readUtf();
		
		return new AdditionalAbilitySynchronizationPacket(player, add, abilities);
	}

	@Override
	public void handle(AdditionalAbilitySynchronizationPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
				Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
				
				if(player == null)
				{
					LOGGER.info(MessageFormat.format("The player {0} was not found when trying to synchronize abilities.", packet.getPlayer().toString()));
				}
				else
				{
					ArrayList<Ability> resolvedAbilities = new ArrayList<>();
					
					for(String rawAbility : packet.getAbilities())
					{
						Ability retrievedAbility = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(new ResourceLocation(rawAbility));
						
						if(retrievedAbility == null)
						{
							LOGGER.info(MessageFormat.format("The ability {0} could not be resolved; it doesn't exist on the client. Please report this as a bug.", rawAbility));
						}
						else
							resolvedAbilities.add(retrievedAbility);
					}
					
					MorphUtil.processCap(player, cap ->
					{
						for(Ability ability : resolvedAbilities)
						{
							if(packet.add)
							{
								cap.applyAbility(ability);
							}
							else
							{
								cap.deapplyAbility(ability);
							}
						}
						
						ctx.get().setPacketHandled(true);
					});
				}
			}
		});
	}
	
	public static class AdditionalAbilitySynchronizationPacket
	{
		private UUID player;
		private boolean add;
		private String[] abilities;
		
		public AdditionalAbilitySynchronizationPacket(UUID player, boolean add, Ability...abilities)
		{
			this.player = player;
			this.add = add;
			
			this.abilities = new String[abilities.length];
			
			// Convert ability to string
			for(int i = 0; i < abilities.length; i++)
				this.abilities[i] = abilities[i].getResourceLocation().toString();			
		}
		
		public AdditionalAbilitySynchronizationPacket(UUID player, boolean add, String...abilities)
		{
			this.player = player;
			this.add = add;
			this.abilities = abilities;
		}
		
		public boolean shouldAdd()
		{
			return add;
		}
		
		public String[] getAbilities()
		{
			return abilities;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
	}
}