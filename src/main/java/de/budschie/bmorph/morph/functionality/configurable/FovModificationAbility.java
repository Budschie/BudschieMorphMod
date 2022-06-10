package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.configurable.client.FovModificationAbilityAdapter;
import de.budschie.bmorph.morph.functionality.configurable.client.IFovModificationAbilityAdapter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * This class contains code to modify the FOV of certain morphs. It has two
 * arguments that you will have to supply it with: "fov_multiplier" (float) and
 * "fov_add" (float). First, the multiplier will be applied to the fov, then the
 * addition. The fov_multiplier argument defaults to 1, the fov_add argument
 * defaults to 0.
 * 
 * @author budschie
 */
public class FovModificationAbility extends Ability
{
	public static final Codec<FovModificationAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group
	(
		Codec.FLOAT.optionalFieldOf("fov_multiplier", 1.0f).forGetter(FovModificationAbility::getFovMultiplier),
		Codec.FLOAT.optionalFieldOf("fov_add", 0.0f).forGetter(FovModificationAbility::getFovAdd)
	).apply(instance, FovModificationAbility::new));
	
	private float fovMultiplier;
	private float fovAdd;
	private Optional<IFovModificationAbilityAdapter> adapter;
	
	public FovModificationAbility(float fovMultiplier, float fovAdd)
	{
		this.fovMultiplier = fovMultiplier;
		this.fovAdd = fovAdd;
		
		this.adapter = Optional.empty();
		
		// This (hopefully) works to prevent classnotfound exceptions, though the compat
		// check between the interface and the implementation might be a bit
		// problematic.
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			adapter = Optional.of(new FovModificationAbilityAdapter());
			adapter.get().setAbility(this);
		});
	}
	
	// I didn't call the super method of onRegister or onUnregister because this class should not receive any events.
	@Override
	public void onRegister()
	{
		if(adapter.isPresent())
			adapter.get().register();
	}
	
	@Override
	public void onUnregister()
	{
		if(adapter.isPresent())
			adapter.get().unregister();	
	}
	
	public float getFovMultiplier()
	{
		return fovMultiplier;
	}
	
	public void setFovMultiplier(float fovMultiplier)
	{
		this.fovMultiplier = fovMultiplier;
	}
	
	public float getFovAdd()
	{
		return fovAdd;
	}
	
	public void setFovAdd(float fovAdd)
	{
		this.fovAdd = fovAdd;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return adapter.isPresent();
	}
}
