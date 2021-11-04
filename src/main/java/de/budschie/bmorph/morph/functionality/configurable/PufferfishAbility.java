package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;

public class PufferfishAbility extends StunAbility
{
	public static final Codec<PufferfishAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					Codec.INT.fieldOf("stun").forGetter(PufferfishAbility::getStun),
					Codec.list(ModCodecs.EFFECT_INSTANCE).fieldOf("effects_on_use").forGetter(PufferfishAbility::getEffects),
					Codec.FLOAT.fieldOf("direct_damage").forGetter(PufferfishAbility::getDirectDamage),
					Codec.FLOAT.fieldOf("ability_radius").forGetter(PufferfishAbility::getRadius)).apply(instance, PufferfishAbility::new));
	
	private List<EffectInstance> effects;
	private float directDamage;
	private float radius;
	
	public PufferfishAbility(int stun, List<EffectInstance> effects, float directDamage, float radius)
	{
		super(stun);
		
		this.effects = effects;
		this.directDamage = directDamage;
		this.radius = radius;
	}
	
	public List<EffectInstance> getEffects()
	{
		return effects;
	}
	
	public float getDirectDamage()
	{
		return directDamage;
	}
	
	public float getRadius()
	{
		return radius;
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		super.onUsedAbility(player, currentMorph);
	}
}
