package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;

public class CommandOnEnable extends Ability
{
	public static final Codec<CommandOnEnable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.COMMAND_PROVIDER.fieldOf("command_provider").forGetter(CommandOnEnable::getCommandProvider)).apply(instance, CommandOnEnable::new));
	
	private CommandProvider commandProvider;

	public CommandOnEnable(CommandProvider commandProvider)
	{
		this.commandProvider = commandProvider;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		if(!player.level.isClientSide)
			commandProvider.executeAsPlayer(player);
	}
	
	public CommandProvider getCommandProvider()
	{
		return commandProvider;
	}
}
