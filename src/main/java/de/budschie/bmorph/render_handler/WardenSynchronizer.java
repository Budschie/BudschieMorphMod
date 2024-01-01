package de.budschie.bmorph.render_handler;

import java.util.Optional;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.state_machine.WardenStates;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

public class WardenSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Warden;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Warden warden = (Warden) morphEntity;
		
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		Optional<MorphStateMachineEntry> wardenStateMachine = cap.getMorphStateMachine().query(WardenStates.WARDEN_KEY);
		
		if(wardenStateMachine.isEmpty())
		{
			return;
		}
		
		if(wardenStateMachine.get().getValue().isEmpty() || wardenStateMachine.get().getTimeElapsedSinceChange().isEmpty())
		{
			return;
		}
		
		TickTimestamp timestamp = wardenStateMachine.get().getTimeElapsedSinceChange().get();
		
		Optional<AnimationState> animationState = Optional.empty();
		
		AnimationState[] controlledAnimations = {warden.diggingAnimationState, warden.emergeAnimationState, warden.sonicBoomAnimationState};
		
		warden.getEntityData().set(Warden.CLIENT_ANGER_LEVEL, 1000);
		
		switch (wardenStateMachine.get().getValue().get())
		{
		case WardenStates.DIG:
		{
			animationState = Optional.of(warden.diggingAnimationState);
			break;
		}
		case WardenStates.EMERGING:
		{
			animationState = Optional.of(warden.emergeAnimationState);
			break;
		}
		case WardenStates.SONIC_BOOM:
		{
			animationState = Optional.of(warden.sonicBoomAnimationState);
			break;
		}
		case WardenStates.UNDER_GROUND:
		{
			warden.setInvisible(true);
		}
		default:
			for(AnimationState state : controlledAnimations)
			{
				state.stop();
			}
			
			return;
		}
				
		// Rebalance this relative to the age of the entity		
		animationState.get().startIfStopped(warden.tickCount - timestamp.getTimeElapsed());
		
		switch (wardenStateMachine.get().getValue().get())
		{
		case WardenStates.EMERGING:
		case WardenStates.DIG:
			warden.clientDiggingParticles(animationState.get());
			break;
		default:
			
		}
		
		for(AnimationState state : controlledAnimations)
		{
			if(state == animationState.get())
			{
				continue;
			}
			
			state.stop();
		}
	} 
}
