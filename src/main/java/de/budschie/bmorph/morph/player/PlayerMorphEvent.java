package de.budschie.bmorph.morph.player;

import javax.annotation.Nullable;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public abstract class PlayerMorphEvent extends Event
{
	PlayerEntity player;
	IMorphCapability morphCapability;
	MorphItem aboutToMorphTo;
	
	public PlayerMorphEvent(PlayerEntity player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
	{
		this.player = player;
		this.morphCapability = morphCapability;
		this.aboutToMorphTo = aboutToMorphTo;
	}

	public PlayerEntity getPlayer()
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
		public Server(PlayerEntity player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
		{
			super(player, morphCapability, aboutToMorphTo);
		}
		
		public static class Pre extends Server
		{

			public Pre(PlayerEntity player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
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

			public Post(PlayerEntity player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
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
		public Client(PlayerEntity player, IMorphCapability morphCapability, @Nullable MorphItem aboutToMorphTo)
		{
			super(player, morphCapability, aboutToMorphTo);
		}
		
		public static class Pre extends Client
		{

			public Pre(PlayerEntity player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
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

			public Post(PlayerEntity player, IMorphCapability morphCapability, MorphItem aboutToMorphTo)
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
