package de.budschie.bmorph.morph;

import java.util.ArrayList;

import javax.annotation.Nullable;

import de.budschie.bmorph.morph.fallback.FallbackMorphManager;
import de.budschie.bmorph.morph.player.PlayerMorphManager;
import net.minecraft.world.entity.Entity;

public class MorphManagerHandlers
{
	public static final PlayerMorphManager PLAYER = new PlayerMorphManager();
	public static final FallbackMorphManager FALLBACK = new FallbackMorphManager();
	
	private static ArrayList<IMorphManager<?, ?>> morphManagers = new ArrayList<>();
	
	public static void registerDefaultManagers()
	{
		registerMorphManager(FALLBACK);
		registerMorphManager(PLAYER);
	}
	
	public static void registerMorphManager(IMorphManager<?, ?> manager)
	{
		morphManagers.add(manager);
	}
	
	@Nullable
	/** This method returns null if a morph item could not be created from a dead entity. **/
	public static MorphItem createMorphFromDeadEntity(Entity killedEntity)
	{
		for(int i = morphManagers.size() - 1; i >= 0; i--)
		{
			IMorphManager<?, ?> manager = morphManagers.get(i);
			
			if(manager.doesManagerApplyTo(killedEntity.getType()))
			{
				return manager.createMorphFromEntity(killedEntity);
			}
		}
		
		return null;
	}
}
