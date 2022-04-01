package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IGuardianBeamCapability
{
	/** Returns a reference to the player holding this capability. **/
	Player getPlayer();
	
	/** Returns the entity that is being attacked by this guardian morph. **/
	Optional<Integer> getAttackedEntity();
	
	/** Returns the entity that is being attacked on the server. This is returns a UUID; this value should only be used for deserialization. **/
	Optional<UUID> getAttackedEntityServer();
	
	/** A number between 0 and {@code getMaxAttackProgression}, this is used to color the guardian beam. **/
	int getAttackProgression();
	
	/** This number indicates what the greatest attack progression may be. **/
	int getMaxAttackProgression();
	
	/** Setter for the entity that shall be attacked by the beam. **/
	void setAttackedEntityServer(Optional<UUID> attackedEntity);
	
	/** Setter for the entity that shell be attacked by the beam on the client. **/
	void setAttackedEntity(Optional<Integer> attackedEntity);
	
	/** Setter for the attack time. **/
	void setAttackProgression(int progression);
	
	/** Setter for the max attack progression **/
	void setMaxAttackProgression(int maxAttackProgression);
	
	/** This method resets the attack progression and sets the attacked entity. **/
	void attack(Optional<Integer> entity, int maxAttackDuration);
	
	/** This method resets the attack progression and sets the attacked entity. It should only be called on the server and sets the UUID. **/
	void attackServer(Optional<Entity> entity, int maxAttackDuration);
	
	/** Returns whether the server should resolve the UUID to an Integer or not. **/
	public default boolean shouldRecalculateEntityId()
	{
		return !getAttackedEntity().isPresent() && getAttackedEntityServer().isPresent();
	}
}
