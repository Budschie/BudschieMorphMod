package de.budschie.bmorph.events;

import javax.annotation.Nullable;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public abstract class PlayerMorphEvent extends Event
{
	Player player;
	IMorphCapability morphCapability;
	MorphItem aboutToMorphTo;
	
	public PlayerMorphEvent(Player player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
	{
		this.player = player;
		this.morphCapability = morphCapability;
		this.aboutToMorphTo = aboutToMorphTo;
	}

	public Player getPlayer()
	{
		return player;
	}
	
	public IMorphCapability getMorphCapability()
	{
		return morphCapability;
	}
	
	@Nullable
	public MorphItem getAboutToMorphTo()
	{
		return aboutToMorphTo;
	}
	
	public static abstract class Server extends PlayerMorphEvent
	{
		public Server(Player player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
		{
			super(player, morphCapability, aboutToMorphTo);
		}
		
		public static class Pre extends Server
		{

			public Pre(Player player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
			{
				super(player, morphCapability, aboutToMorphTo);
			}
			
			@Override
			public boolean isCancelable()
			{
				return true;
			}
		}
		
		public static class Post extends Server
		{

			public Post(Player player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
			{
				super(player, morphCapability, aboutToMorphTo);
			}
			
			@Override
			public boolean isCancelable()
			{
				return false;
			}
		}
	}
	
	public static abstract class Client extends PlayerMorphEvent
	{
		public Client(Player player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
		{
			super(player, morphCapability, aboutToMorphTo);
		}
		
		public static class Pre extends Client
		{

			public Pre(Player player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
			{
				super(player, morphCapability, aboutToMorphTo);
			}
			
			@Override
			public boolean isCancelable()
			{
				return false;
			}
		}
		
		public static class Post extends Client
		{

			public Post(Player player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
			{
				super(player, morphCapability, aboutToMorphTo);
			}
			
			@Override
			public boolean isCancelable()
			{
				return false;
			}
		}
	}
}
