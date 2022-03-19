package de.budschie.bmorph.morph;

import java.util.HashMap;

import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.VisualMorphSynchronizer;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class VisualMorphDataRegistry
{
	private HashMap<ResourceLocation, VisualMorphData> visualMorphDataRegistry = new HashMap<>();
	
	// We might have to make this method synchronized
	public void addVisualMorphData(VisualMorphData visualMorphData)
	{
		if(this.visualMorphDataRegistry.containsKey(visualMorphData.getRegistryName()))
		{
			throw new IllegalArgumentException(String.format("The key %s is already registered.", visualMorphData.getRegistryName()));
		}
		
		this.visualMorphDataRegistry.put(visualMorphData.getRegistryName(), visualMorphData);
	}
	
	public VisualMorphData getDataForMorph(ResourceLocation morph)
	{
		return visualMorphDataRegistry.get(morph);
	}
	
	public VisualMorphData getDataForMorph(MorphItem morphItem)
	{
		return getDataForMorph(morphItem.getEntityType().getRegistryName());
	}
	
	public void clear()
	{
		this.visualMorphDataRegistry.clear();
	}
	
	public void syncWithClient(ServerPlayer client)
	{
		if(!visualMorphDataRegistry.isEmpty())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> client), new VisualMorphSynchronizer.VisualMorphPacket(visualMorphDataRegistry.values()));
	}
	
	public void syncWithClients()
	{
		if(!visualMorphDataRegistry.isEmpty())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new VisualMorphSynchronizer.VisualMorphPacket(visualMorphDataRegistry.values()));
	}
	
	public static class VisualMorphData
	{
		private float scale;
		private ResourceLocation registryName;
		
		public VisualMorphData(float scale)
		{
			this.scale = scale;
		}
		
		public float getScale()
		{
			return scale;
		}
		
		public void setRegistryName(ResourceLocation registryName)
		{
			this.registryName = registryName;
		}
		
		public ResourceLocation getRegistryName()
		{
			return registryName;
		}
	}
}
