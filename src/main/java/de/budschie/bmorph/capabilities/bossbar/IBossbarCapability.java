package de.budschie.bmorph.capabilities.bossbar;

import java.util.Optional;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;

public interface IBossbarCapability
{
	ServerPlayer getPlayer();
	void setBossbar(ServerBossEvent bossbar);
	void clearBossbar();
	Optional<ServerBossEvent> getBossbar();
}
