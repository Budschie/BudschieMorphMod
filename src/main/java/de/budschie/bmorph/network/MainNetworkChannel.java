package de.budschie.bmorph.network;

import java.util.Optional;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.network.AbilityGroupSync.AbilityGroupSyncPacket;
import de.budschie.bmorph.network.AdditionalAbilitySynchronization.AdditionalAbilitySynchronizationPacket;
import de.budschie.bmorph.network.ChangeUsingSpeedOfMorph.ChangeUsingSpeedOfMorphPacket;
import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket;
import de.budschie.bmorph.network.DataTransformerSynchronizer.DataTransfomerSynchronizerPacket;
import de.budschie.bmorph.network.DeleteOrDropMorph.DeleteOrDropMorphPacket;
import de.budschie.bmorph.network.EntityMovementChanged.EntityMovementChangedPacket;
import de.budschie.bmorph.network.EvokerSpell.EvokerSpellPacket;
import de.budschie.bmorph.network.Flight.FlightPacket;
import de.budschie.bmorph.network.GlideStatusChange.GlideStatusChangePacket;
import de.budschie.bmorph.network.GuardianBeamAttack.GuardianBeamAttackPacket;
import de.budschie.bmorph.network.MorphAddedSynchronizer.MorphAddedPacket;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer.MorphPacket;
import de.budschie.bmorph.network.MorphChangedSynchronizer.MorphChangedPacket;
import de.budschie.bmorph.network.MorphItemDisabled.MorphItemDisabledPacket;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import de.budschie.bmorph.network.MorphRequestAbilityUsage.MorphRequestAbilityUsagePacket;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import de.budschie.bmorph.network.MorphSheepSheared.MorphSheepShearedPacket;
import de.budschie.bmorph.network.ParrotDanceSync.ParrotDanceSyncPacket;
import de.budschie.bmorph.network.ProxyEntityEvent.ProxyEntityEventPacket;
import de.budschie.bmorph.network.PufferfishPuff.PufferfishPuffPacket;
import de.budschie.bmorph.network.SquidBoost.SquidBoostPacket;
import de.budschie.bmorph.network.VisualMorphSynchronizer.VisualMorphPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MainNetworkChannel
{
	public static final String PROTOCOL_VERSION = "6";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(References.MODID, "main"), 
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	private static int id = 0;
		
	public static void registerMainNetworkChannels()
	{
//		INSTANCE.registerMessage(id++, MorphCapabilityFullSynchronizer.MorphPacket.class, MorphCapabilityFullSynchronizer::encode,
//				MorphCapabilityFullSynchronizer::decode, MorphCapabilityFullSynchronizer::handle);
		
		registerSimpleImplPacket(MorphPacket.class, new MorphCapabilityFullSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(MorphAddedPacket.class, new MorphAddedSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(MorphRemovedPacket.class, new MorphRemovedSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(MorphChangedPacket.class, new MorphChangedSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(RequestMorphIndexChangePacket.class, new MorphRequestMorphIndexChange(), NetworkDirection.PLAY_TO_SERVER);
		registerSimpleImplPacket(MorphRequestAbilityUsagePacket.class, new MorphRequestAbilityUsage(), NetworkDirection.PLAY_TO_SERVER);
		registerSimpleImplPacket(MorphRequestFavouriteChangePacket.class, new MorphRequestFavouriteChange(), NetworkDirection.PLAY_TO_SERVER);
		registerSimpleImplPacket(SquidBoostPacket.class, new SquidBoost(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(ConfiguredAbilityPacket.class, new ConfiguredAbilitySynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(PufferfishPuffPacket.class, new PufferfishPuff(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(GuardianBeamAttackPacket.class, new GuardianBeamAttack(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(DeleteOrDropMorphPacket.class, new DeleteOrDropMorph(), NetworkDirection.PLAY_TO_SERVER);
		registerSimpleImplPacket(VisualMorphPacket.class, new VisualMorphSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(GlideStatusChangePacket.class, new GlideStatusChange(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(AdditionalAbilitySynchronizationPacket.class, new AdditionalAbilitySynchronization(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(ParrotDanceSyncPacket.class, new ParrotDanceSync(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(MorphSheepShearedPacket.class, new MorphSheepSheared(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(DataTransfomerSynchronizerPacket.class, new DataTransformerSynchronizer(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(FlightPacket.class, new Flight(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(MorphItemDisabledPacket.class, new MorphItemDisabled(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(AbilityGroupSyncPacket.class, new AbilityGroupSync(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(EvokerSpellPacket.class, new EvokerSpell(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(ProxyEntityEventPacket.class, new ProxyEntityEvent(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(EntityMovementChangedPacket.class, new EntityMovementChanged(), NetworkDirection.PLAY_TO_CLIENT);
		registerSimpleImplPacket(ChangeUsingSpeedOfMorphPacket.class, new ChangeUsingSpeedOfMorph(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static <T> void registerSimpleImplPacket(Class<T> packetClass, ISimpleImplPacket<T> packet, NetworkDirection netDir)
	{
		INSTANCE.registerMessage(id++, packetClass, packet::encode, packet::decode, packet::handle, Optional.of(netDir));
	}
}
