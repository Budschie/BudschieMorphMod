package de.budschie.bmorph.morph;

import java.util.function.Predicate;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class PlayerMorphItem extends MorphItem
{
	GameProfile gameProfile;
	
	public PlayerMorphItem(GameProfile gameProfile)
	{
		super("player_morph_item");
		this.gameProfile = gameProfile;
	}
	
	public PlayerMorphItem()
	{
		super("player_morph_item");
	}
	
	@Override
	public EntityType<?> getEntityType()
	{
		return EntityType.PLAYER;
	}

	@Override
	public Entity createEntity(World world)
	{		
		System.out.println(gameProfile.getId());
		
		AdvancedAbstractClientPlayerEntity entity = new AdvancedAbstractClientPlayerEntity((ClientWorld) world, gameProfile);
		
		Minecraft.getInstance().getSkinManager().loadProfileTextures(gameProfile, (type, resourceLocation, texture) -> 
		{
			System.out.println("Downloading texture with hash " + texture.getHash());
			System.out.println("URL: " + texture.getUrl());
			
			if(type == Type.CAPE)
			{
				entity.capeResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);;
			}
			else if(type == Type.SKIN)
			{
				entity.skinResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);
			}
			else if(type == Type.ELYTRA)
			{
				entity.elytraResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);
			}
		}, true);
		
		return entity;
	}

	@Override
	public void deserializeAdditional(CompoundNBT nbt)
	{
		gameProfile = new GameProfile(nbt.getUniqueId("UUID"), nbt.getString("Name"));
	}

	@Override
	public CompoundNBT serializeAdditional()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUniqueId("UUID", gameProfile.getId());
		nbt.putString("Name", gameProfile.getName());
		return nbt;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof PlayerMorphItem)
		{
			return MorphManagerHandlers.PLAYER.equalsFor(this, (PlayerMorphItem)obj);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return MorphManagerHandlers.PLAYER.hashCodeFor(this);
	}
	
	public static class AdvancedAbstractClientPlayerEntity extends AbstractClientPlayerEntity
	{
		public ResourceLocation skinResourceLocation = DefaultPlayerSkin.getDefaultSkin(getUniqueID());
		public ResourceLocation elytraResourceLocation = null;
		public ResourceLocation capeResourceLocation = null;
		
		private Predicate<PlayerModelPart> isWearing = null;
		
		public AdvancedAbstractClientPlayerEntity(ClientWorld world, GameProfile profile)
		{
			super(world, profile);
		}
		
		@Override
		public ResourceLocation getLocationSkin()
		{
			return skinResourceLocation;
		}
		
		@Override
		public boolean hasPlayerInfo()
		{
			return capeResourceLocation != null;
		}
		
		@Override
		public boolean isPlayerInfoSet()
		{
			return elytraResourceLocation != null;
		}
		
		@Override
		public boolean isWearing(PlayerModelPart part)
		{
			return this.isWearing == null ? true : isWearing.test(part);
		}
		
		public void setIsWearing(Predicate<PlayerModelPart> isWearing)
		{
			this.isWearing = isWearing;
		}
		
		@Override
		public ResourceLocation getLocationElytra()
		{
			return elytraResourceLocation;
		}
		
		@Override
		public ResourceLocation getLocationCape()
		{
			return capeResourceLocation;
		}
	}
}
