package de.budschie.bmorph.morph.functionality;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import de.budschie.bmorph.util.IDynamicRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public abstract class Ability implements IDynamicRegistryObject
{
	private final Logger LOGGER = LogManager.getLogger();
	
	// Yay finally we have this in the Ability class
	protected HashSet<UUID> trackedPlayers = new HashSet<>();
	
	private ResourceLocation resourceLocation;
	private ConfigurableAbility<? extends Ability> configurableAbility;
	
	/**
	 * @deprecated <b>THIS METHOD IS MARKED FOR REMOVAL!</b> It may be removed in
	 *             the future. It is still being called, but it might disappear in a
	 *             few versions.<br><br>
	 * 
	 *             It will be replaced by
	 *             {@link Ability#enableAbility(Player, MorphItem, MorphItem, List)},
	 *             which has more arguments.
	 **/
	@Deprecated(since = "1.18.1-3.0.0", forRemoval = true)
	public void enableAbility(Player player, MorphItem enabledItem) { }
	
	/**
	 * If you plan on overriding this method, you will NEED to call the super method
	 * as this method actually contains some code by default
	 * 
	 * Called when an ability is added to a player.
	 * 
	 * @param player       This is the player to which this ability was added.
	 * @param enabledItem  This is the current morph item the player has.
	 * @param oldMorph     This is the previous morph item the player had. This
	 *                     value might be null.
	 * @param oldAbilities This is a list of the old abilities the player had before
	 *                     getting this ability. This list is never null.
	 **/
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		// Call old method for compat reasons
		enableAbility(player, enabledItem);
		
		// Add player to the tracked player list if we listen for events
		if(isAbleToReceiveEvents())
			trackedPlayers.add(player.getUUID());
	}
	
	/**
	 * @deprecated <b>THIS METHOD IS MARKED FOR REMOVAL!</b> It may be removed in
	 *             the future. It is still being called, but it might disappear in a
	 *             few versions.<br><br>
	 * 
	 *             It will be replaced by
	 *             {@link Ability#disableAbility(Player, MorphItem, MorphItem, List)},
	 *             which has more arguments.
	 **/
	@Deprecated(since = "1.18.1-3.0.0", forRemoval = true)
	public void disableAbility(Player player, MorphItem disabledItem) {}
	
	/**
	 * If you plan on overriding this method, you will NEED to call the super method
	 * as this method actually contains some code by default
	 * 
	 * Called when an ability is removed from a player.
	 * 
	 * @param player       This is the player from which this ability was removed.
	 * @param disabledItem This is the old morph item the player had.
	 * @param newMorph     This is the next morph item the player will have. This
	 *                     value might be null.
	 * @param newAbilities This is a list of the new abilities the player will have.
	 *                     This list is never null.
	 **/
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		// Call old method for compatibility reasons.
		disableAbility(player, disabledItem);		
	}
	
	/**
	 * This method is being invoked when references of the given player in the
	 * Ability classes should be removed. Remember that this is no replacement for
	 * {@link Ability#disableAbility(Player, MorphItem, MorphItem, List, AbilityChangeReason)},
	 * but rather an addition that is not only fired when demorphing, but also when leaving the world.
	 **/
	public void removePlayerReferences(Player playerRefToRemove) 
	{
		// Remove player from the tracked player list if we listen for events
		if(isAbleToReceiveEvents())
			trackedPlayers.remove(playerRefToRemove.getUUID());
	}
	
	/** This method is fired when an active ability is used. **/
	public void onUsedAbility(Player player, MorphItem currentMorph) {}
	
	/**
	 * If you plan on overriding this method, you will NEED to call the super method
	 * as this method actually contains some code by default
	 * 
	 * This method gets invoked once when an instance of this ability gets
	 * registered. An example use case of this would be in
	 * {@link AbstractEventAbility}. Here, this method is used to register this
	 * class to the event bus.
	 **/
	public void onRegister() 
	{
		// Add this instance to the event bus if we should be able to receive events.
		if(isAbleToReceiveEvents())
		{
			MinecraftForge.EVENT_BUS.register(this);
		}
	}
		
	/**
	 * If you plan on overriding this method, you will NEED to call the super method
	 * as this method actually contains some code by default This method is like
	 * {@link Ability#onRegister()}, except it is called when this ability is
	 * removed from the dynamic registry.
	 **/
	public void onUnregister() 
	{
		// If we were able to receive events, remove this instance from the event bus.
		if(isAbleToReceiveEvents())
		{
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}
	
	@Override
	public ResourceLocation getResourceLocation()
	{
		return resourceLocation;
	}
	
	@Override
	public void setResourceLocation(ResourceLocation resourceLocation)
	{
		this.resourceLocation = resourceLocation;
	}
	
	public ConfigurableAbility<? extends Ability> getConfigurableAbility()
	{
		return configurableAbility;
	}
	
	public void setConfigurableAbility(ConfigurableAbility<?> configurableAbility)
	{
		this.configurableAbility = configurableAbility;
	}
	
	/**
	 * This method shall return true if this Ability subscribes to events. Returning
	 * true will lead this Ability class to add itself to the event bus.
	 * 
	 * @return A boolean indicating whether this Ability is able to receive events
	 *         or not. Only set this to true if you really need it. This method
	 *         defaults to return {@code false}. <b>Contract:</b> For a given
	 *         instance of an Ability, invoking this method at any point shall
	 *         always return the same result.
	 **/
	public boolean isAbleToReceiveEvents()
	{
		return false;
	}
	
	/**
	 * Only returns {@code true} if {@link Ability#isAbleToReceiveEvents()} also
	 * returns true and the supplied entity posseses this ability.
	 **/
	public boolean isTracked(Entity entity)
	{
		return trackedPlayers.contains(entity.getUUID());
	}
	
	/** Returns {@link ResourceLocation#hashCode()}. **/
	@Override
	public int hashCode()
	{
		return resourceLocation.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Ability otherAbility)
		{
			if(otherAbility.getResourceLocation().equals(this.getResourceLocation()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Seerialize player-specific data of this ability in the given compound tag.
	 * 
	 * @param player               The player to whom this ability belongs.
	 * @param context              This context can be used to retrieve the compound
	 *                             tags to which this ability can be saved to.
	 * @param canSaveTransientData Only save transient data if this flag is set to
	 *                             {@code true} to avoid wasting unneccessary CPU
	 *                             cycles.
	 **/
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		// Always delete the object before we serialize it so that there are no left-overs of the previous serialization
		context.deleteSerializationObjectForAbility(this);
	}
	
	/** Load data for the specific player from the compound tag. **/
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		
	}
	
	/**
	 * Enum showing different reasons why
	 * {@link Ability#enableAbility(Player, MorphItem, MorphItem, List)} or
	 * {@link Ability#disableAbility(Player, MorphItem, MorphItem, List)} was
	 * called.
	 * 
	 * There are currently two different reasons:
	 * - The first one is {@link AbilityChangeReason#MORPHED}: It signalizes that the reason for a change of an ability is that morphs have changed.
	 * - The second reason is {@link AbilityChangeReason#DYNAMIC}: It indicates that the reason for the change of an ability was not the change of morphs.
	 * - The third reason is {@link AbilityChangeReason#LOGGED_OUT}: It tells the game that the player's ability have changed because the player left the game.
	 **/
	public static enum AbilityChangeReason
	{
		MORPHED, DYNAMIC, LOGGED_OUT
	}
}
