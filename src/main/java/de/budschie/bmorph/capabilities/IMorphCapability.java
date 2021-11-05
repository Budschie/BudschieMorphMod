package de.budschie.bmorph.capabilities;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;

public interface IMorphCapability
{
	/** Returns an optional with the current morph item. This optional will be empty, except when you are morphed with the /morph command. **/
	public Optional<MorphItem> getCurrentMorphItem();
	/** Returns an integer representing the current morph index of the morph item you are currently morphed in. Note that this does not apply to the /morph command. **/
	public Optional<Integer> getCurrentMorphIndex();
	
	/** This sort of combines {@link IMorphCapability#getCurrentMorphIndex()} and {@link IMorphCapability#getCurrentMorphItem()} by checking if one of these methods doesn't return an empty Optional, and returns the result. **/
	public Optional<MorphItem> getCurrentMorph();
	
	/** This method adds the given morph item to the morph list. **/
	public void addToMorphList(MorphItem morphItem);
	/** This method removes the given index from the morph list. **/
	public void removeFromMorphList(int index);
	/** This method returns the morph list as an object. **/
	public MorphList getMorphList();
	/** This is a setter for the morph list. **/
	public void setMorphList(MorphList list);
	
	/** This sets the morph index, and its value can be retrieved by invoking {@link IMorphCapability#getCurrentMorphIndex()}. **/
	public void setMorph(int index);
	/** This sets the morph item, and its value can be retrieved by invoking {@link IMorphCapability#getCurrentMorphItem()}. **/
	public void setMorph(MorphItem morph);
	
	/** The purpose of this method is to clear the Optionals holding the current morph data. 
	 * After calling this method, {@link IMorphCapability#getCurrentMorphItem()}, {@link IMorphCapability#getCurrentMorphIndex()} and {@link IMorphCapability#getCurrentMorph()} 
	 * will return an empty optional. 
	**/
	public void demorph();
	
	public void applyHealthOnPlayer(PlayerEntity player);
	
	/**
	 * By calling this method, you sync the capability data with every player.
	 * This method shall not be called if you intent to try to synchronize a morph
	 * change across every client. Use
	 * {@link IMorphCapability#syncMorphChange(PlayerEntity)} to do this.
	 **/
	@Deprecated
	public void syncWithClients(PlayerEntity player);
	
	/** This method is used to synchronize this capability with a specific target. **/
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo);
	
	/** This method is much like the method described above, just with an network manager as a target instead of a player as a target. **/
	public void syncWithConnection(PlayerEntity player, NetworkManager connection);
	
	/** This method synchronizes a morph change to all players. **/
	public void syncMorphChange(PlayerEntity player);
	/** This method synchronizes the acquisition of a morph to all players. **/
	public void syncMorphAcquisition(PlayerEntity player, MorphItem item);
	/** This method synchronizes the removal of a morph to all players. **/
	public void syncMorphRemoval(PlayerEntity player, int index);
	
	/** Returns the value of the flag mentioned in {@link IMorphCapability#setMobAttack(boolean)}. **/
	public boolean shouldMobsAttack();
	
	/** This method is a flag that indicates whether the mob attack ability is present or not. Note that this value defaults to {@code false}. **/
	public void setMobAttack(boolean value);
	
	@Nullable
	/** This list returns all currently active abilities. It may be null. **/
	public List<Ability> getCurrentAbilities();
	
	/** This is simply a setter for all current abilities. **/
	public void setCurrentAbilities(List<Ability> abilities);
	
	/** This applies abilities, meaning that we iterate over the list of abilities and call the apply method on them. **/
	public void applyAbilities(PlayerEntity player);
	/** This method deapplies all abilities by once again iterating over every old ability and deapplying it. **/
	public void deapplyAbilities(PlayerEntity player);
	
	/** This will iterate over every ability and signal them that the button to use an ability has been pressed. **/
	public void useAbility(PlayerEntity player);
	
	// Aggro timestamps are measured in ints. Aggro timestamp => not saved, aggro duration => saved (indicates how long mobs will be aggro)
	public int getLastAggroTimestamp();
	public void setLastAggroTimestamp(int timestamp);
	public int getLastAggroDuration();
	public void setLastAggroDuration(int aggroDuration);
	
	/** This method is a getter for the morph favourite list. **/
	public FavouriteList getFavouriteList();
	public void setFavouriteList(FavouriteList favouriteList);
}
