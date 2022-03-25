package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.Ability;

public class NoFlames extends Ability
{
	public static final Codec<NoFlames> CODEC = Codec.unit(NoFlames::new);
}
