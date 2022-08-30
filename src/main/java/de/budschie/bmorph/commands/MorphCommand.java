package de.budschie.bmorph.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphItemDisabled.MorphItemDisabledPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
								Optional<GameProfile> gp = ctx.getSource().getServer().getProfileCache().get(ctx.getArgument("playername", String.class));
								
								if(gp.isPresent())
									MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, gp.get())), MorphReasonRegistry.MORPHED_BY_COMMAND.get(), player);
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
					
					MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_COMMAND.get(), player);
					
					return 0;
				})
				.then(Commands.argument("player", EntityArgument.players()).executes(ctx ->
				{
					List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
					
					for(ServerPlayer player : players)
						MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_COMMAND.get(), player);
					
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
				
		dispatcher.register(Commands.literal("disable_morph_item")
				.requires(sender -> sender.hasPermission(2))
				.then(Commands.argument("player", EntityArgument.players())
				.then(Commands.argument("disabled_for", IntegerArgumentType.integer(0))
				.then(Commands.literal("everything")
						.then(Commands.literal("matching")
						.then(Commands.argument("entity_type", EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
							.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
							.executes(ctx -> disableMorphItems(ctx, getMorphFilter(ctx.getArgument("entity_type", ResourceLocation.class), Optional.of(ctx.getArgument("nbt", CompoundTag.class))))))
						.executes(ctx -> disableMorphItems(ctx, getMorphFilter(ctx.getArgument("entity_type", ResourceLocation.class), Optional.empty())))))
					.executes(ctx -> disableMorphItems(ctx, (cap, consumer) -> cap.getMorphList().forEach(consumer))))
				.then(Commands.literal("current_morph_item")
						.executes(ctx -> disableMorphItems(ctx, (cap, consumer) -> cap.getCurrentMorph().ifPresent(consumer))))))
		);
		
		dispatcher.register(Commands.literal("morph_list_ability")
				.requires(sender -> sender.hasPermission(2))
				.executes(ctx -> listAbilities(ctx.getSource(), ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> listAbilities(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}
	
	private static BiConsumer<IMorphCapability, Consumer<MorphItem>> getMorphFilter(ResourceLocation entity, Optional<CompoundTag> nbtToMatch)
	{
		EntityType<?> resultingEntityType = ForgeRegistries.ENTITIES.getValue(entity);
		
		return (cap, consumer) ->
		{
			for(MorphItem morph : cap.getMorphList())
			{
				if(morph.getEntityType() == resultingEntityType &&
						(nbtToMatch.isEmpty() || (nbtToMatch.isPresent() && NbtUtils.compareNbt(nbtToMatch.get(), morph.serializeAdditional(), true))))
				{
					consumer.accept(morph);
				}
			}
		};
	}
	
	private static int disableMorphItems(CommandContext<CommandSourceStack> ctx, BiConsumer<IMorphCapability, Consumer<MorphItem>> forEachMorphItem) throws CommandSyntaxException
	{
		List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
		int disabledFor = ctx.getArgument("disabled_for", Integer.class);
		
		// Count of players where the given for each loop provided more than one result.
		int succeeded = 0;
		
		for(ServerPlayer player : players)
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(player);
			
			if(cap != null)
			{
				ArrayList<UUID> toDeactivate = new ArrayList<>();
				
				forEachMorphItem.accept(cap, morphItem ->
				{
					toDeactivate.add(morphItem.getUUID());
					morphItem.disable(disabledFor);
				});
				
				if(toDeactivate.size() > 0)
				{
					succeeded++;
				}
				
				MorphItemDisabledPacket packet = new MorphItemDisabledPacket(disabledFor, toDeactivate.toArray(size -> new UUID[size]));
				MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
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
				
				capability.addMorphItem(morphItemToAdd);
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
			
			MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true)), MorphReasonRegistry.MORPHED_BY_COMMAND.get(), entity);
		}
		
		return 0;
	}
}
