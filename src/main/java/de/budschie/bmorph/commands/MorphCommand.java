package de.budschie.bmorph.commands;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphItemDisabled.MorphItemDisabledPacket;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand
{
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("morph")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
							Commands.argument("entity", EntitySummonArgument.id())
							.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundTag());
							})
							.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundTag.class));
							})))
						));
		
		dispatcher.register(Commands.literal("morphplayer")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
						Commands.argument("playername", StringArgumentType.word())
						.executes(ctx -> 
						{
							List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
							
							for(ServerPlayer player : players)
							{
								// TODO: Risky
								
								
								// And risky it was 
								// - Budschie, probably half a year later
								Optional<GameProfile> gp = ServerSetup.server.getProfileCache().get(ctx.getArgument("playername", String.class));
								
								if(gp.isPresent())
									MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, gp.get())), Optional.empty(), player);
								else
									ctx.getSource().sendFailure(new TextComponent(ChatFormatting.RED + "The player " + ctx.getArgument("playername", String.class) + " doesn't exist."));
							}
							
							return 0;
						}))));
		
		dispatcher.register(Commands.literal("demorph")
				.requires(sender -> sender.hasPermission(2))
				.executes(ctx ->
				{
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})
				.then(Commands.argument("player", EntityArgument.players()).executes(ctx ->
				{
					List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
					
					for(ServerPlayer player : players)
						MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})));
		
		dispatcher.register(Commands.literal("addmorph")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
									Commands.argument("entity", EntitySummonArgument.id())
									.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundTag());
									})
									.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundTag.class));
									}))
								)
						));
		
		// This code is plain unreadable...
		dispatcher.register(Commands.literal("disable_morph_item").requires(sender -> sender.hasPermission(2))
				.then(Commands.argument("player", EntityArgument.players()).then(Commands.argument("disabled_for", IntegerArgumentType.integer(0))
						.then(Commands.literal("current_morph_item").executes(ctx -> disableMorphItems(ctx, (sp, cap) ->
						{
							if (cap.getCurrentMorphIndex().isPresent())
								return new int[] { cap.getCurrentMorphIndex().get() };
							else
								return null;
						})))
						.then(Commands.literal("everything")
								.then(Commands.literal("matching").then(Commands.literal("entity_type").then(Commands.argument("entity_type", EntitySummonArgument.id())
										.suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(ctx -> disableMorphItems(ctx, (sp, cap) ->
										{
											EntityType<?> desiredEntityType = ForgeRegistries.ENTITIES.getValue(ctx.getArgument("entity_type", ResourceLocation.class));
											
											// If the given entity type doesn't exist, return null to indicate error
											if(desiredEntityType == null)
												return null;

											// Create an dynamically resizable int array containing the indices of the found entities
											IntArrayList intArrayList = new IntArrayList();

											// Iterate over every single morph item and add those morph items to the array list that have the given entity type
											for (int i = 0; i < cap.getMorphList().getMorphArrayList().size(); i++)
											{
												MorphItem currentItem = cap.getMorphList().getMorphArrayList().get(i);

												if (currentItem.getEntityType() == desiredEntityType)
													intArrayList.add(i);
											}
											
											// Return the int array
											return intArrayList.toIntArray();
										})))))
								.executes(ctx -> disableMorphItems(ctx, (sp, cap) ->
								{
									// We could greatly optimize this (memory-wise) but we would lose a lot of
									// generalization, so I'm not gonna do it
									int[] toDisable = new int[cap.getMorphList().getMorphArrayList().size()];

									for (int i = 0; i < toDisable.length; i++)
									{
										toDisable[i] = i;
									}

									return toDisable;
								}))))));
		
		dispatcher.register(Commands.literal("morph_list_ability")
				.requires(sender -> sender.hasPermission(2))
				.executes(ctx -> listAbilities(ctx.getSource(), ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> listAbilities(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}
	
	private static int disableMorphItems(CommandContext<CommandSourceStack> ctx, BiFunction<ServerPlayer, IMorphCapability, int[]> morphItemSelectionFunction) throws CommandSyntaxException
	{
		List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
		int disableFor = ctx.getArgument("disabled_for", Integer.class);
		
		int succeeded = 0;
		
		for(ServerPlayer player : players)
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(player);
			
			if(cap != null)
			{
//				if(cap.getCurrentMorphIndex().isPresent())
//				{
//					cap.getCurrentMorph().get().disable(disableFor);
//					
//					MorphItemDisabledPacket packet = new MorphItemDisabledPacket(cap.getCurrentMorphIndex().get(), disableFor);
//					MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
//					
//					succeeded++;
//				}
				
				int[] selectedMorphs = morphItemSelectionFunction.apply(player, cap);
				
				if(selectedMorphs != null && selectedMorphs.length > 0)
				{
					for(int i : selectedMorphs)
						cap.getMorphList().getMorphArrayList().get(i).disable(disableFor);
					
					MorphItemDisabledPacket packet = new MorphItemDisabledPacket(disableFor, selectedMorphs);
					MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
					
					succeeded++;
				}
			}
		}
		
		ctx.getSource().sendSuccess(new TextComponent(MessageFormat.format("{0}/{1} players' morphs were successfully disabled.", succeeded, players.size())), false);
		
		return 0;
	}
	
	private static int listAbilities(CommandSourceStack sender, Player player)
	{
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		
		if(cap == null || !cap.getCurrentMorph().isPresent())
		{
			sender.sendFailure(new TextComponent("The given player is currently not morphed."));
		}
		else if(cap.getCurrentAbilities().size() <= 0)
		{
			sender.sendSuccess(new TextComponent(ChatFormatting.AQUA + "The given player has no abilities, but they are morphed."), true);
		}
		else
		{
			StringBuilder builder = new StringBuilder(ChatFormatting.GREEN + "The given player currently has following abilities: ").append(ChatFormatting.WHITE);
			
			for(int i = 0; i < cap.getCurrentAbilities().size(); i++)
			{
				Ability currentAbility = cap.getCurrentAbilities().get(i);
				
				builder.append(currentAbility.getResourceLocation().toString()).append('(').append(currentAbility.getConfigurableAbility().getRegistryName().toString()).append(')');
				
				if(i == (cap.getCurrentAbilities().size() - 2))
				{
					builder.append(" and ");
				}
				else if(i != (cap.getCurrentAbilities().size() - 1))
				{
					builder.append(", ");
				}
			}
			
			sender.sendSuccess(new TextComponent(builder.toString()), true);
		}
		
		return 0;
	}
	
	private static int addMorph(List<ServerPlayer> entities, ResourceLocation rs, CompoundTag nbtData)
	{
		MorphItem morphItemToAdd = MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true);
		
		for(ServerPlayer entity : entities)
		{
			IMorphCapability capability = entity.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get();
						
			if(capability.getMorphList().contains(morphItemToAdd))
				entity.sendMessage(new TextComponent(ChatFormatting.RED + "You may not add a morph to your list that is already present."), new UUID(0, 0));
			else
			{
				entity.sendMessage(new TextComponent("Added " + rs.toString() + " with its NBT data to your morph list."), new UUID(0, 0));
				
				capability.addToMorphList(morphItemToAdd);
				capability.syncMorphAcquisition(morphItemToAdd);
			}
		}
		
		return 0;
	}
	
	private static int createEntityMorph(List<ServerPlayer> entities, ResourceLocation rs, CompoundTag nbtData)
	{
		for(ServerPlayer entity : entities)
		{
			if(rs.toString().equals("bmorph:morph_entity"))
				throw new IllegalArgumentException("You may not morph yourself into the morph entity.");
			
			nbtData.putString("id", rs.toString());
			
			MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true)), Optional.empty(), entity);
		}
		
		return 0;
	}
}
