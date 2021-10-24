package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.PlayerEntity;

public class CommandOnDisable extends Ability
{
	public static final Codec<CommandOnDisable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.COMMAND_PROVIDER.fieldOf("command_provider").forGetter(CommandOnDisable::getCommandProvider)).apply(instance, CommandOnDisable::new));
	
	private CommandProvider commandProvider;

	public CommandOnDisable(CommandProvider commandProvider)
	{
		this.commandProvider = commandProvider;
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		if(!player.world.isRemote)
			commandProvider.executeAsPlayer(player);
	}
	
	public CommandProvider getCommandProvider()
	{
		return commandProvider;
	}
}
