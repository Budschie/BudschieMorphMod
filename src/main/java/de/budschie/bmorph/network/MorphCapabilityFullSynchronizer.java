package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer.MorphPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class MorphCapabilityFullSynchronizer implements ISimpleImplPacket<MorphPacket>
{
	@Override
	public void encode(MorphPacket packet, PacketBuffer buffer)
	{		
		buffer.writeUniqueId(packet.player);
		packet.morphList.serializePacket(buffer);
		buffer.writeBoolean(packet.entityData.isPresent());
		buffer.writeBoolean(packet.entityIndex.isPresent());
		packet.getEntityData().ifPresent(data -> buffer.writeCompoundTag(data.serialize()));
		packet.getEntityIndex().ifPresent(data -> buffer.writeInt(data));
		
		buffer.writeInt(packet.getAbilities().size());
		
		for(String str : packet.getAbilities())
			buffer.writeString(str);
	}
	
	@Override
	public MorphPacket decode(PacketBuffer buffer)
	{
		UUID playerUUID = buffer.readUniqueId();
		
		// Hmmm yeah the floor is made out of floor
		MorphList morphList = new MorphList();
		morphList.deserializePacket(buffer);
		
		Optional<MorphItem> toMorph = Optional.empty();
		Optional<Integer> entityIndex = Optional.empty();
		
		boolean hasMorph = buffer.readBoolean(), hasIndex = buffer.readBoolean();
		
		if(hasMorph)
			toMorph = Optional.of(MorphHandler.deserializeMorphItem(buffer.readCompoundTag()));
		
		if(hasIndex)
			entityIndex = Optional.of(buffer.readInt());
		
		int amountOfAbilities = buffer.readInt();
		
		ArrayList<String> abilities = new ArrayList<>(amountOfAbilities);
		
		for(int i = 0; i < amountOfAbilities; i++)
			abilities.add(buffer.readString());
		
		return new MorphPacket(toMorph, entityIndex, morphList, abilities, playerUUID);
	}
	
	@Override
	public void handle(MorphPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().world.getPlayerByUuid(message.getPlayer()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				cap.resolve().get().deapplyAbilities(Minecraft.getInstance().world.getPlayerByUuid(message.getPlayer()));
				
				if(message.entityData.isPresent())
					cap.resolve().get().setMorph(message.entityData.get());
				else if(message.entityIndex.isPresent())
					cap.resolve().get().setMorph(message.entityIndex.get());
				else
					cap.resolve().get().demorph();
				
				cap.resolve().get().setMorphList(message.morphList);
				
				ArrayList<Ability> resolvedAbilities = new ArrayList<>();
				
				IForgeRegistry<Ability> registry = GameRegistry.findRegistry(Ability.class);
				
				for(String name : message.getAbilities())
				{
					ResourceLocation resourceLocation = new ResourceLocation(name);
					resolvedAbilities.add(registry.getValue(resourceLocation));
				}
				
				cap.resolve().get().setCurrentAbilities(resolvedAbilities);
				cap.resolve().get().applyAbilities(Minecraft.getInstance().world.getPlayerByUuid(message.getPlayer()));
			}
		});
	}
	
	public static class MorphPacket
	{
		private Optional<MorphItem> entityData;
		private Optional<Integer> entityIndex;
		private MorphList morphList;
		private ArrayList<String> abilities;
		private UUID player;
		
		public MorphPacket(Optional<MorphItem> entityData, Optional<Integer> entityIndex, MorphList morphList, ArrayList<String> abilities, UUID player)
		{
			this.entityData = entityData;
			this.player = player;
			this.morphList = morphList;
			this.entityIndex = entityIndex;
			this.abilities = abilities;
		}
		
		public ArrayList<String> getAbilities()
		{
			return abilities;
		}
		
		public Optional<MorphItem> getEntityData()
		{
			return entityData;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public MorphList getMorphList()
		{
			return morphList;
		}
		
		public Optional<Integer> getEntityIndex()
		{
			return entityIndex;
		}
	}
}
