package de.budschie.bmorph.morph.functionality.codec_addition;

import java.util.function.Function;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;

public class CommandProvider
{
	private String command;
	private Selector selector;
	
	public CommandProvider(String command, Selector selector)
	{
		this.command = command;
		this.selector = selector;
	}
	
	/** Retrieves the stored command. **/
	public String getCommand()
	{
		return command;
	}
	
	/** Sets the stored command. **/
	public void setCommand(String command)
	{
		this.command = command;
	}
	
	/** Executes the given command as the given command source. **/
	public void executeAs(CommandSource commandSource)
	{
		ServerSetup.server.getCommandManager().handleCommand(commandSource, command);
	}
	
	/** Executes the stored command as the given player. **/
	public void executeAsPlayer(PlayerEntity player)
	{
		CommandSource source = this.selector.getCommandSource(player);
		
		if(source != null)
			executeAs(source.withPermissionLevel(ServerSetup.server.getOpPermissionLevel()).withFeedbackDisabled());
	}
	
	public Selector getSelector()
	{
		return selector;
	}
	
	/**
	 * This enum has three elements:
	 * <br>
	 * <br>
	 * SELF: Use this Selector if you want that the command should be executed on the player itself.
	 * <br>
	 * REVENGE_TARGET: Use this Selector if you want your command to execute on the entity that last hit the player. Note that the command might NOT execute, as there are sometimes no revenge targets.
	 * <br>
	 * LAST_ATTACKED: Use this Selector if you want your command to be executed on the entity you last attacked.
	 **/
	public static enum Selector
	{
		SELF(playerIn -> playerIn.getCommandSource()), 
		REVENGE_TARGET(playerIn -> playerIn.getRevengeTarget() == null ? null : playerIn.getRevengeTarget().getCommandSource()), 
		LAST_ATTACKED(playerIn -> playerIn.getLastAttackedEntity() == null ? null : playerIn.getLastAttackedEntity().getCommandSource());
		
		private Function<PlayerEntity, CommandSource> sourceFunction;
		
		private Selector(Function<PlayerEntity, CommandSource> sourceFunction)
		{
			this.sourceFunction = sourceFunction;
		}
		
		public CommandSource getCommandSource(PlayerEntity playerIn)
		{
			return sourceFunction.apply(playerIn);
		}
	}
}
