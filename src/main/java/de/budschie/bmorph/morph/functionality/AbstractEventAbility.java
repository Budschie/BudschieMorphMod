package de.budschie.bmorph.morph.functionality;

import java.util.HashSet;
import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.configurable.PufferfishAbility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

/**
 * @deprecated The purpose of this class was to handle abilities which subscribe
 *             to events. I consider this class to be an antipattern, and using
 *             this class is really a pain in many ways, especially because Java
 *             doesn't support multi-inheritance. A prime example of where this
 *             class fails is in the class {@link PufferfishAbility}, where you
 *             can see that I did not inherit this class but the class
 *             {@link StunAbility} because I needed its functionality as well.
 *             Despite this, I needed to subscribe to events, which lead to me
 *             copying code of this class into {@link PufferfishAbility}. So, I
 *             hope you begin to undertstand why I chose to deprecate this
 *             class.
 * 
 *             There will be a replacement of this class in place. In fact, it
 *             will be directly baked into the {@link Ability} class. If you
 *             want the same behaviour that this class has, just override the
 *             method {@link Ability#isAbleToReceiveEvents()} and let it return
 *             true. This has the advantage that I don't have to copy code
 *             anymore.
 **/
@Deprecated(since = "1.18.1-3.0.0")
public abstract class AbstractEventAbility extends Ability 
{
	// TODO: Investigate whether players that die are being removed from this list on the client and server.
	protected HashSet<UUID> trackedPlayers = new HashSet<>();
	
	public AbstractEventAbility()
	{
		
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		trackedPlayers.add(player.getUUID());
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		trackedPlayers.remove(player.getUUID());
	}
	
	@Override
	public void onRegister()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onUnregister()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@Override
	public boolean isTracked(Entity entity)
	{
		return trackedPlayers.contains(entity.getUUID());
	}
}
