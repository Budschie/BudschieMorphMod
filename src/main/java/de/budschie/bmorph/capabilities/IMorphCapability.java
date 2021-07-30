package de.budschie.bmorph.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;

public interface IMorphCapability
{
	public Optional<MorphItem> getCurrentMorphItem();
	public Optional<Integer> getCurrentMorphIndex();
	
	public Optional<MorphItem> getCurrentMorph();
	
	public void addToMorphList(MorphItem morphItem);
	public void removeFromMorphList(int index);
	public MorphList getMorphList();
	public void setMorphList(MorphList list);
	
	public void setMorph(int index);
	public void setMorph(MorphItem morph);
	public void demorph();
	
	public void applyHealthOnPlayer(PlayerEntity player);
	
	public void syncWithClients(PlayerEntity player);
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo);
	public void syncWithConnection(PlayerEntity player, NetworkManager connection);
	
	public void syncMorphChange(PlayerEntity player);
	public void syncMorphAcquisition(PlayerEntity player, MorphItem item);
	public void syncMorphRemoval(PlayerEntity player, int index);
	
	@Nullable
	/** This list returns all currently active abilities. It may be null. **/
	public List<Ability> getCurrentAbilities();
	
	public void setCurrentAbilities(List<Ability> abilities);
	
	public void applyAbilities(PlayerEntity player);
	public void deapplyAbilities(PlayerEntity player);
	
	public void useAbility(PlayerEntity player);
	
	public boolean hasAbility(Ability ability);
	
	public boolean isDirty();
	public void cleanDirty();
}
