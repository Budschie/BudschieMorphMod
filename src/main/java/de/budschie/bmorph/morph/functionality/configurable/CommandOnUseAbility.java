package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.PlayerEntity;

public class CommandOnUseAbility extends StunAbility
{
	public static final Codec<CommandOnUseAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.INT.optionalFieldOf("stun_in_ticks", 10).forGetter(CommandOnUseAbility::getStun),
					ModCodecs.COMMAND_PROVIDER.fieldOf("command_provider").forGetter(CommandOnUseAbility::getCommandProvider))
			.apply(instance, CommandOnUseAbility::new));

	private CommandProvider commandProvider;

	public CommandOnUseAbility(int stun, CommandProvider commandProvider)
	{
		super(stun);
		this.commandProvider = commandProvider;
	}

	public CommandProvider getCommandProvider()
	{
		return commandProvider;
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!player.world.isRemote && !isCurrentlyStunned(player.getUniqueID()))
		{
			stun(player.getUniqueID());
			
			this.commandProvider.executeAsPlayer(player);
		}
	}
}
