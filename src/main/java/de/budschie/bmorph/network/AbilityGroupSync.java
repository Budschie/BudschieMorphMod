package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.AbilityGroupSync.AbilityGroupSyncPacket;
import de.budschie.bmorph.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

public class AbilityGroupSync implements ISimpleImplPacket<AbilityGroupSyncPacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(AbilityGroupSyncPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getAbilityGroups().size());
		
		Iterator<AbilityGroup> abilityGroupiterator = packet.getAbilityGroups().iterator();
		
		for(int i = 0; i < packet.getAbilityGroups().size(); i++)
		{
			AbilityGroup group = abilityGroupiterator.next();
			
			buffer.writeUtf(group.getResourceLocation().toString());
			buffer.writeInt(group.getAbilities().size());
			
			Iterator<Ability> abilityIterator = group.getAbilities().iterator();
			
			for(int j = 0; j < group.getAbilities().size(); j++)
			{
				buffer.writeUtf(abilityIterator.next().getResourceLocation().toString());
			}
		}
	}

	@Override
	public AbilityGroupSyncPacket decode(FriendlyByteBuf buffer)
	{
		ArrayList<Pair<ResourceLocation, ? extends Collection<ResourceLocation>>> receivedData = new ArrayList<>();
		
		int abilityGroupLength = buffer.readInt();
		
		for(int i = 0; i < abilityGroupLength; i++)
		{
			ResourceLocation rlGroup = new ResourceLocation(buffer.readUtf());
			
			int abilityListLength = buffer.readInt();
			ArrayList<ResourceLocation> rlAbilityList = new ArrayList<>();
			
			for(int j = 0; j < abilityListLength; j++)
			{
				rlAbilityList.add(new ResourceLocation(buffer.readUtf()));
			}
			
			receivedData.add(new Pair<>(rlGroup, rlAbilityList));
		}
		
		return AbilityGroupSyncPacket.clientPacket(receivedData);
	}

	@Override
	public void handle(AbilityGroupSyncPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			BMorphMod.ABILITY_GROUPS.setFillRegistry(() ->
			{				
				for(Pair<ResourceLocation, ? extends Collection<ResourceLocation>> groupTupel : packet.getToResolve())
				{
					AbilityGroup group = new AbilityGroup(groupTupel.getA());
					
					// This is bad code design but whatever
					resolveAbilities:
					for(ResourceLocation abilityRl : groupTupel.getB())
					{
						Ability resolved = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(abilityRl);
						
						if(resolved == null)
						{
							LOGGER.warn(MessageFormat.format("Failed to resolve ability {0} of ability group {1} as said ability doesn't exist. This is an error with the mod, please report it to the mod author!", abilityRl.toString(), groupTupel.getA().toString()));
							continue resolveAbilities;
						}
						
						group.addAbility(resolved);
					}
					
					BMorphMod.ABILITY_GROUPS.registerEntry(group);
				}
			});
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class AbilityGroupSyncPacket
	{
		private Collection<AbilityGroup> abilityGroups;
		private Collection<Pair<ResourceLocation, ? extends Collection<ResourceLocation>>> toResolve;
		
		private AbilityGroupSyncPacket()
		{
			
		}
		
		public static AbilityGroupSyncPacket serverPacket(Collection<AbilityGroup> abilityGroups)
		{
			AbilityGroupSyncPacket packet = new AbilityGroupSyncPacket();
			packet.abilityGroups = abilityGroups;
			return packet;
		}
		
		public static AbilityGroupSyncPacket clientPacket(Collection<Pair<ResourceLocation, ? extends Collection<ResourceLocation>>> toResolve)
		{
			AbilityGroupSyncPacket packet = new AbilityGroupSyncPacket();
			packet.toResolve = toResolve;
			return packet;
		}
		
		public Collection<AbilityGroup> getAbilityGroups()
		{
			return abilityGroups;
		}
		
		public Collection<Pair<ResourceLocation, ? extends Collection<ResourceLocation>>> getToResolve()
		{
			return toResolve;
		}
	}
}
