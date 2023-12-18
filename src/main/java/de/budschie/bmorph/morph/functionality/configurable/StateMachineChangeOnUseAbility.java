package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineRecordedChanges;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class StateMachineChangeOnUseAbility extends Ability
{
	public static final Codec<StateMachineChangeOnUseAbility> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
				ResourceLocation.CODEC.fieldOf("state_key").forGetter(StateMachineChangeOnUseAbility::getStateKey), 
				Codec.STRING.optionalFieldOf("state_value").forGetter(StateMachineChangeOnUseAbility::getStateValue)).apply(instance, StateMachineChangeOnUseAbility::new)
	);
	
	private ResourceLocation stateKey;
	private Optional<String> stateValue;
	
	public StateMachineChangeOnUseAbility(ResourceLocation stateKey, Optional<String> stateValue)
	{
		this.stateKey = stateKey;
		this.stateValue = stateValue;
	}

	public ResourceLocation getStateKey()
	{
		return stateKey;
	}
	
	public Optional<String> getStateValue()
	{
		return stateValue;
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(player.level.isClientSide())
		{
			return;
		}
		
		MorphUtil.processCap(player, cap ->
		{
			MorphStateMachineRecordedChanges changes = null;
			
			// Strictly speaking not needed, but I'll do it anyway
			if(stateValue.isPresent())
			{
				changes = cap.createMorphStateMachineChangeRecorder().recordChange(stateKey, new MorphStateMachineEntry(Optional.of(new TickTimestamp()), stateValue)).finishRecording();
			}
			else
			{
				changes = cap.createMorphStateMachineChangeRecorder().recordChange(stateKey).finishRecording();
			}
			
			changes.applyChanges();
			cap.syncMorphStateMachineRecordedChanges(changes);
		});
	}
}
