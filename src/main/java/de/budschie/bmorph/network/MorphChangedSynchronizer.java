package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.MorphChangedSynchronizer.MorphChangedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class MorphChangedSynchronizer implements ISimpleImplPacket<MorphChangedPacket>
{
	@Override
	public void encode(MorphChangedPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.getPlayerUUID());
		
		buffer.writeBoolean(packet.getMorphIndex().isPresent());
		buffer.writeBoolean(packet.getMorphItem().isPresent());
		
		packet.getMorphIndex().ifPresent(index -> buffer.writeInt(index));
		packet.getMorphItem().ifPresent(item -> buffer.writeCompoundTag(item.serialize()));
		
		buffer.writeInt(packet.getAbilities().size());
		
		for(String str : packet.getAbilities())
			buffer.writeString(str);
	}

	@Override
	public MorphChangedPacket decode(PacketBuffer buffer)
	{
		UUID playerUUID = buffer.readUniqueId();
		boolean hasIndex = buffer.readBoolean(), hasItem = buffer.readBoolean();
		
		Optional<Integer> morphIndex = Optional.empty();
		Optional<MorphItem> morphItem = Optional.empty();
		
		if(hasIndex)
			morphIndex = Optional.of(buffer.readInt());
		
		if(hasItem)
			morphItem = Optional.of(MorphHandler.deserializeMorphItem(buffer.readCompoundTag()));
		
		int amountOfAbilities = buffer.readInt();
		
		ArrayList<String> abilities = new ArrayList<>(amountOfAbilities);
		
		for(int i = 0; i < amountOfAbilities; i++)
			abilities.add(buffer.readString());
		
		return new MorphChangedPacket(playerUUID, morphIndex, morphItem, abilities);
	}

	@Override
	public void handle(MorphChangedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.deapplyAbilities(Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()));
				
				if(packet.getMorphIndex().isPresent())
					resolved.setMorph(packet.getMorphIndex().get());
				else if(packet.getMorphItem().isPresent())
					resolved.setMorph(packet.getMorphItem().get());
				else
					resolved.demorph();
				
				ArrayList<Ability> resolvedAbilities = new ArrayList<>();
				
				IForgeRegistry<Ability> registry = GameRegistry.findRegistry(Ability.class);
				
				for(String name : packet.getAbilities())
				{
					ResourceLocation resourceLocation = new ResourceLocation(name);
					resolvedAbilities.add(registry.getValue(resourceLocation));
				}
				
				resolved.setCurrentAbilities(resolvedAbilities);
				resolved.applyAbilities(Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()));
			}
		});
	}
	
	public static class MorphChangedPacket
	{
		UUID playerUUID;
		Optional<Integer> morphIndex;
		Optional<MorphItem> morphItem;
		ArrayList<String> abilities; 
		
		public MorphChangedPacket(UUID playerUUID, Optional<Integer> morphIndex, Optional<MorphItem> morphItem, ArrayList<String> abilities)
		{
			this.playerUUID = playerUUID;
			this.morphIndex = morphIndex;
			this.morphItem = morphItem;
			this.abilities = abilities;
		}
		
		public Optional<Integer> getMorphIndex()
		{
			return morphIndex;
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
	}
}
