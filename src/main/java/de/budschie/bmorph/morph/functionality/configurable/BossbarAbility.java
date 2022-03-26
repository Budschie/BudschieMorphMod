package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.bossbar.BossbarCapabilityInstance;
import de.budschie.bmorph.events.Events;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.player.Player;

public class BossbarAbility extends Ability
{
	public static final Codec<BossbarAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.BOSSBAR_COLOR_ENUM.fieldOf("bossbar_color").forGetter(BossbarAbility::getBossbarColor),
			ModCodecs.BOSSBAR_OVERLAY_ENUM.fieldOf("bossbar_overlay").forGetter(BossbarAbility::getBossbarOverlay),
			Codec.BOOL.optionalFieldOf("darken_world", false).forGetter(BossbarAbility::isDarkenWorld),
			Codec.BOOL.optionalFieldOf("create_world_fog", false).forGetter(BossbarAbility::isCreateWorldFog),
			Codec.BOOL.optionalFieldOf("play_boss_music", false).forGetter(BossbarAbility::isPlayBossMusic)
			).apply(instance, BossbarAbility::new));
	
	private BossBarColor bossbarColor;
	private BossBarOverlay bossbarOverlay;
	private boolean darkenWorld;
	private boolean createWorldFog;
	private boolean playBossMusic;
	
	public BossbarAbility(BossBarColor bossbarColor, BossBarOverlay bossbarOverlay, boolean darkenWorld, boolean createWorldFog, boolean playBossMusic)
	{
		this.bossbarColor = bossbarColor;
		this.bossbarOverlay = bossbarOverlay;
		this.darkenWorld = darkenWorld;
		this.createWorldFog = createWorldFog;
		this.playBossMusic = playBossMusic;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		if(!player.level.isClientSide())
		{
			ServerBossEvent bossbar = (ServerBossEvent) new ServerBossEvent(player.getDisplayName(), bossbarColor, bossbarOverlay).setCreateWorldFog(createWorldFog)
					.setDarkenScreen(darkenWorld).setPlayBossMusic(playBossMusic);
			
			player.getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(cap -> cap.setBossbar(bossbar));
			Events.showBossbarToEveryoneTrackingPlayer(player);
		}
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		if(!player.level.isClientSide())
			player.getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(cap -> cap.clearBossbar());
	}

	public BossBarColor getBossbarColor()
	{
		return bossbarColor;
	}

	public BossBarOverlay getBossbarOverlay()
	{
		return bossbarOverlay;
	}

	public boolean isDarkenWorld()
	{
		return darkenWorld;
	}

	public boolean isCreateWorldFog()
	{
		return createWorldFog;
	}

	public boolean isPlayBossMusic()
	{
		return playBossMusic;
	}
}
