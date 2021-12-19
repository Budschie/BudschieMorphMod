package de.budschie.bmorph.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class EntityUtil
{
	private static LazyOptional<Method> readAdditionalSaveDataField = LazyOptional.of(() -> 
	{
		Method method = ObfuscationReflectionHelper.findMethod(Entity.class, "m_7378_", CompoundTag.class);
		method.setAccessible(true);
		return method;
	});
	private static LazyOptional<Method> addAdditionalSaveDataField = LazyOptional.of(() ->
	{
		Method method = ObfuscationReflectionHelper.findMethod(Entity.class, "m_7380_", CompoundTag.class);
		method.setAccessible(true);
		return method;
	});
	
	public static void readAdditionalSaveData(Entity forEntity, CompoundTag readFrom)
	{
		try
		{
			readAdditionalSaveDataField.resolve().get().invoke(forEntity, readFrom);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addAdditionalSaveData(Entity forEntity, CompoundTag addTo)
	{
		try
		{
			addAdditionalSaveDataField.resolve().get().invoke(forEntity, addTo);
		} 
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void copyLocationAndRotation(Entity from, Entity to)
	{
		to.copyPosition(from);
		
		to.setXRot(from.getXRot());
		to.setYRot(from.getYRot());
		
		to.xOld = from.xOld;
		to.yOld = from.yOld;
		to.zOld = from.zOld;
		
		to.xRotO = from.xRotO;
		to.yRotO = from.yRotO;
	}
}
