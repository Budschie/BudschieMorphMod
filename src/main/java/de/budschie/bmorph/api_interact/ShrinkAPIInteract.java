package de.budschie.bmorph.api_interact;

import java.util.Optional;

import net.gigabit101.shrink.api.IShrinkProvider;
import net.gigabit101.shrink.api.ShrinkAPI;
import net.minecraft.entity.player.PlayerEntity;

public class ShrinkAPIInteract implements IShrinkAPIInteract
{
	@Override
	public float getShrinkingValue(PlayerEntity player)
	{
		Optional<IShrinkProvider> provider = player.getCapability(ShrinkAPI.SHRINK_CAPABILITY).resolve();
		
		if(provider.isPresent())
		{
			if(provider.get().isShrunk())
				return provider.get().defaultEntitySize().height / 0.2F;
		}
		
		return 1;
	}
}
