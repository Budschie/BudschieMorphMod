package de.budschie.bmorph.capabilities;

import java.util.Optional;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface IMorphCapability
{
	public Optional<MorphItem> getCurrentMorph();
	public void setCurrentMorph(Optional<MorphItem> morph);
	
	public void addToMorphList(MorphItem morphItem);
	public void removeFromMorphList(MorphItem morphItem);
	public MorphList getMorphList();
	public void setMorphList(MorphList list);
	
	public void applyHealthOnPlayer(PlayerEntity player);
	
	public void syncWithClients(PlayerEntity player);
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo);
	
	public boolean isDirty();
	public void cleanDirty();
}
