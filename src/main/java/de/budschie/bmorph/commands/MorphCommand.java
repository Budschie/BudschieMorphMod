package de.budschie.bmorph.commands;

import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand
{
	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("morph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(Commands.argument("entity", EntitySummonArgument.entitySummon())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(ctx -> 
						{
							return createEntityMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), new CompoundNBT());
						})
						.then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
						.executes(ctx -> 
						{
							return createEntityMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundNBT.class));
						}))
						));
		
		dispatcher.register(Commands.literal("morphplayer")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(Commands.argument("playername", StringArgumentType.word())
						.executes(ctx -> 
						{
							ServerPlayerEntity player = ctx.getSource().asPlayer();
							
							MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, ServerSetup.server.getPlayerProfileCache().getGameProfileForUsername(ctx.getArgument("playername", String.class)))), Optional.empty(), player);
							
							return 0;
						})));
		
		dispatcher.register(Commands.literal("demorph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.executes(ctx ->
				{
					ServerPlayerEntity player = ctx.getSource().asPlayer();
					
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				}));
		
		dispatcher.register(Commands.literal("addmorph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(Commands.argument("entity", EntitySummonArgument.entitySummon())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(ctx -> 
						{
							return addMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), new CompoundNBT());
						})
						.then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
						.executes(ctx -> 
						{
							return addMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundNBT.class));
						}))
						));
	}
	
	private static int addMorph(ServerPlayerEntity entity, ResourceLocation rs, CompoundNBT nbtData)
	{
		IMorphCapability capability = entity.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get();
		
		MorphItem morphItemToAdd = MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true);
		
		if(capability.getMorphList().contains(morphItemToAdd))
			entity.sendMessage(new StringTextComponent(TextFormatting.RED + "You may not add a morph to your list that is already present."), new UUID(0, 0));
		else
		{
			entity.sendMessage(new StringTextComponent("Added " + rs.toString() + " with its NBT data to your morph list."), new UUID(0, 0));
			
			capability.addToMorphList(morphItemToAdd);
			capability.syncMorphAcquisition(entity, morphItemToAdd);
		}
		
		return 0;
	}
	
	private static int createEntityMorph(ServerPlayerEntity entity, ResourceLocation rs, CompoundNBT nbtData)
	{
		if(rs.toString().equals("bmorph:morph_entity"))
			throw new IllegalArgumentException("You may not morph yourself into the morph entity.");
		
		nbtData.putString("id", rs.toString());
		
		MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true)), Optional.empty(), entity);
		
		return 0;
	}
}
