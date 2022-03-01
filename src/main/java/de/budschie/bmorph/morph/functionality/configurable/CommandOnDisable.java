package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;

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
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		if(!player.level.isClientSide)
			commandProvider.executeAsPlayer(player);
	}
	
	public CommandProvider getCommandProvider()
	{
		return commandProvider;
	}
}
