package de.budschie.bmorph.capabilities.bossbar;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstanceSerializable;
import de.budschie.bmorph.main.References;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class BossbarCapabilityInstance extends CommonCapabilityInstanceSerializable<IBossbarCapability>
{
	public static final ResourceLocation BOSSBAR_CAP_ID = new ResourceLocation(References.MODID, "bossbar_cap");
	public static final Capability<IBossbarCapability> BOSSBAR_CAP = CapabilityManager.get(new CapabilityToken<>(){});
		
	public BossbarCapabilityInstance(ServerPlayer player)
	{
		super(BOSSBAR_CAP_ID, BOSSBAR_CAP, () -> new BossbarCapability(player));
	}
	
	@SubscribeEvent
	public static void onAttachingCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if(!event.getObject().level.isClientSide() && event.getObject() instanceof ServerPlayer player)
		{
			event.addCapability(BOSSBAR_CAP_ID, new BossbarCapabilityInstance(player));
		}
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, IBossbarCapability instance)
	{
//		if(tag.contains("bossbar", Tag.TAG_COMPOUND))
//		{
//			CompoundTag bossbarCompound = tag.getCompound("bossbar");
//			
//			boolean shouldPlayBossMusic = bossbarCompound.getBoolean("boss_music");
//			boolean shouldDarkenScreen = bossbarCompound.getBoolean("darken_screen");
//			boolean shouldCreateWorldFog = bossbarCompound.getBoolean("create_world_fog");
//			
//			BossBarColor color = BossBarColor.byName(bossbarCompound.getString("bossbar_color"));
//			BossBarOverlay overlay = BossBarOverlay.byName(bossbarCompound.getString("bossbar_overlay"));
//			
//			ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(instance.getPlayer().getName(), color, overlay).setCreateWorldFog(shouldCreateWorldFog)
//					.setDarkenScreen(shouldDarkenScreen).setPlayBossMusic(shouldPlayBossMusic);
//			
//			instance.setBossbar(bossEvent);
//		}
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IBossbarCapability instance)
	{
//		if(instance.getBossbar().isPresent())
//		{
//			ServerBossEvent bossbar = instance.getBossbar().get();
//			
//			CompoundTag bossbarCompound = new CompoundTag();
//			
//			bossbarCompound.putBoolean("boss_music", bossbar.shouldPlayBossMusic());
//			bossbarCompound.putBoolean("darken_screen", bossbar.shouldDarkenScreen());
//			bossbarCompound.putBoolean("create_world_fog", bossbar.shouldCreateWorldFog());
//			
//			bossbarCompound.putString("bossbar_color", bossbar.getColor().getName());
//			bossbarCompound.putString("bossbar_overlay", bossbar.getOverlay().getName());
//			
//			tag.put("bossbar", bossbarCompound);
//		}
	}
}
