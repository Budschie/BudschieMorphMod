package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.stand_on_fluid.StandOnFluidInstance;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

/** Best name ever. **/
public class JesusAbility extends Ability
{
	public static Codec<JesusAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.FLUIDS.listOf().optionalFieldOf("allowed_fluids", Arrays.asList()).forGetter(JesusAbility::getAllowedFluids)
			).apply(instance, JesusAbility::new));
	
	private List<Fluid> allowedFluids;
	
	public JesusAbility(List<Fluid> allowedFluids)
	{
		this.allowedFluids = allowedFluids;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		player.getCapability(StandOnFluidInstance.STAND_ON_FLUID_CAP).ifPresent(cap ->
		{
			this.allowedFluids.forEach(fluid -> cap.addAllowedFluid(fluid));
		});
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		player.getCapability(StandOnFluidInstance.STAND_ON_FLUID_CAP).ifPresent(cap ->
		{
			this.allowedFluids.forEach(fluid -> cap.removeAllowedFluid(fluid));
		});
	}
	
	public List<Fluid> getAllowedFluids()
	{
		return allowedFluids;
	}
}
