package de.budschie.bmorph.morph.functionality.codec_addition;

import java.util.function.Function;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

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
	public void executeAs(CommandSourceStack commandSource)
	{
		commandSource.getServer().getCommands().performCommand(commandSource, command);
	}
	
	/** Executes the stored command as the given player. **/
	public void executeAsPlayer(Player player)
	{
		CommandSourceStack source = this.selector.getCommandSource(player);
		
		if(source != null)
			executeAs(source.withPermission(source.getServer().getOperatorUserPermissionLevel()).withSuppressedOutput());
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
		SELF(playerIn -> playerIn.createCommandSourceStack()), 
		REVENGE_TARGET(playerIn -> playerIn.getLastDamageSource() == null ? null : (playerIn.getLastDamageSource().getEntity() == null ? null : playerIn.getLastDamageSource().getEntity().createCommandSourceStack())), 
		LAST_ATTACKED(playerIn -> playerIn.getLastHurtMob() == null ? null : playerIn.getLastHurtMob().createCommandSourceStack());
		
		private Function<Player, CommandSourceStack> sourceFunction;
		
		private Selector(Function<Player, CommandSourceStack> sourceFunction)
		{
			this.sourceFunction = sourceFunction;
		}
		
		public CommandSourceStack getCommandSource(Player playerIn)
		{
			return sourceFunction.apply(playerIn);
		}
	}
}
