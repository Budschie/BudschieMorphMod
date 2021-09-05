package de.budschie.bmorph.morph.player;

import java.util.function.Predicate;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;

public class AdvancedAbstractClientPlayerEntity extends AbstractClientPlayerEntity
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
