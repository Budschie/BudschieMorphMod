package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

public class ConfiguredAbilitySynchronizer implements ISimpleImplPacket<ConfiguredAbilityPacket>
{
	@Override
	public void encode(ConfiguredAbilityPacket packet, FriendlyByteBuf buffer)
	{
		for(Ability ability : packet.getAbilities())
		{
			Optional<CompoundTag> serialized = ability.getConfigurableAbility().serializeNBTIAmTooDumbForJava(ability);
			
			if(serialized.isPresent())
			{
				buffer.writeUtf(ability.getResourceLocation().toString());
				buffer.writeUtf(ability.getConfigurableAbility().getRegistryName().toString());
				buffer.writeNbt(serialized.get());
			}
		}
		
		buffer.writeUtf("");
	}

	@Override
	public ConfiguredAbilityPacket decode(FriendlyByteBuf buffer)
	{
		ArrayList<Ability> abilities = new ArrayList<>();
		
		readAbilityLoop:
		while(true)
		{
			String abilityName = buffer.readUtf();
			
			if(abilityName.isEmpty())
				break readAbilityLoop;
			else
			{
				String configurableAbilityName = buffer.readUtf();
				CompoundTag nbt = buffer.readNbt();
				
				ConfigurableAbility<?> configurableAbility = AbilityRegistry.REGISTRY.get().getValue(new ResourceLocation(configurableAbilityName));
				
				Optional<Ability> parsedAbility = configurableAbility.deserializeNBT(nbt);
				
				if(parsedAbility.isPresent())
				{
					parsedAbility.get().setResourceLocation(new ResourceLocation(abilityName));
					abilities.add(parsedAbility.get());
				}
			}
		}
		
		return new ConfiguredAbilityPacket(abilities);
	}

	@Override
	public void handle(ConfiguredAbilityPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			BMorphMod.DYNAMIC_ABILITY_REGISTRY.unregisterAll();
			packet.getAbilities().forEach(ability -> BMorphMod.DYNAMIC_ABILITY_REGISTRY.registerEntry(ability));
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class ConfiguredAbilityPacket
	{
		private Collection<Ability> abilities;
		
		public ConfiguredAbilityPacket(Collection<Ability> abilities)
		{
			this.abilities = abilities;
		}
		
		public Collection<Ability> getAbilities()
		{
			return abilities;
		}
	}
}
