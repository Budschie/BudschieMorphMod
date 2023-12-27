package de.budschie.bmorph.commands;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineRecordedChanges;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StateMachineCommand
{
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
	{
		dispatcher
				.register(
						Commands.literal("state_machine").requires(sender -> sender.hasPermission(2))
								.then(Commands.argument("player", EntityArgument.players())
										.then(Commands.literal("set").then(Commands.argument("state_key", ResourceLocationArgument.id())
												.then(Commands.argument("state_value", StringArgumentType.word())
														.executes(ctx -> performCommand(ctx.getSource(), ctx.getArgument("state_key", ResourceLocation.class),
																Optional.of(ctx.getArgument("state_value", String.class)))))
												.executes(
														ctx -> performCommand(ctx.getSource(), ctx.getArgument("state_key", ResourceLocation.class), Optional.empty()))))
										.then(Commands.literal("query")
												.then(Commands.argument("state_key", ResourceLocationArgument.id())
														.executes(ctx -> queryCommand(ctx.getSource(), ctx.getArgument("player", EntitySelector.class),
																Optional.of(ctx.getArgument("state_key", ResourceLocation.class)))))
												.executes(ctx -> queryCommand(ctx.getSource(), ctx.getArgument("player", EntitySelector.class), Optional.empty())))));
	}
	
	private static int performCommand(CommandSourceStack source, ResourceLocation key, Optional<String> value)
	{
		ServerPlayer player = source.getPlayer();
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		MorphStateMachineRecordedChanges recorder = cap.createMorphStateMachineChangeRecorder().recordChange(key, new MorphStateMachineEntry(Optional.of(new TickTimestamp()), value)).finishRecording();
		cap.syncMorphStateMachineRecordedChanges(recorder);
		recorder.applyChanges();
		
		return 1;
	}
	
	private static Component getComponentForState(ResourceLocation key, Optional<MorphStateMachineEntry> state)
	{
		if(state.isEmpty())
		{
			return Component.literal(key + ": (not initialized)");
		}
		
		String value = "(not present)";
		String time = "(not present)";
		
		if(state.get().getValue().isPresent())
		{
			value = state.get().getValue().get();
		}
		
		if(state.get().getTimeElapsedSinceChange().isPresent())
		{
			time = Integer.valueOf(state.get().getTimeElapsedSinceChange().get().getTimeElapsed()).toString();
		}
		
		return Component.literal(MessageFormat.format("{0}: Value of \"{1}\", Timestamp of \"{2}\"", key.toString(), value, time));
	}
	
	private static Component getComponentForStateAndPlayer(ServerPlayer player, ResourceLocation stateKey)
	{
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		Optional<MorphStateMachineEntry> entry = cap.getMorphStateMachine().query(stateKey);
		
		return Component.literal("Player ")
				.append(player.getDisplayName())
				.append(" has following data for the key ")
				.append(stateKey.toString()).append(":\n")
				.append(getComponentForState(stateKey, entry));
	}
	
	private static Component getComponentForPlayer(ServerPlayer player)
	{
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		MutableComponent component = Component.literal("Player ")
				.append(player.getDisplayName())
				.append(" has following data in their state machine:");
		
		for(Map.Entry<ResourceLocation, MorphStateMachineEntry> entry : cap.getMorphStateMachine().getStates().entrySet())
		{
			component.append("\n\n").append(getComponentForState(entry.getKey(), Optional.ofNullable(entry.getValue())));
		}
		
		return component;
	}
	
	private static int queryCommand(CommandSourceStack source, EntitySelector selector, Optional<ResourceLocation> stateKey) throws CommandSyntaxException
	{
		Function<ServerPlayer, Component> consumer = null;
		
		MutableComponent component = Component.empty();
		
		if(stateKey.isPresent())
		{
			consumer = (player) -> getComponentForStateAndPlayer(player, stateKey.get());
		}
		else
		{
			consumer = (player) -> getComponentForPlayer(player);
		}
		
		for(ServerPlayer player : selector.findPlayers(source))
		{
			component.append(consumer.apply(player));
		}
		
		source.sendSystemMessage(component);
		
		return 1;
	}
}
