package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.PassiveTickAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;

public class PassiveTickCommandAbility extends PassiveTickAbility
{
	public static final Codec<PassiveTickCommandAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(
							Codec.INT.fieldOf("ticks_per_command").forGetter(PassiveTickCommandAbility::getUpdateDuration),
							ModCodecs.COMMAND_PROVIDER.fieldOf("command_provider").forGetter(PassiveTickCommandAbility::getCommandProvider))
					.apply(instance, PassiveTickCommandAbility::new));

	private CommandProvider commandProvider;
	
	// Nice
	public PassiveTickCommandAbility(int ticksPerCommand, CommandProvider commandProvider)
	{
		super(ticksPerCommand, (player, cap) -> commandProvider.executeAsPlayer(player));
	}

	public CommandProvider getCommandProvider()
	{
		return commandProvider;
	}
}
