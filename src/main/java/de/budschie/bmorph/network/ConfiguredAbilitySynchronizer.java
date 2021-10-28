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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ConfiguredAbilitySynchronizer implements ISimpleImplPacket<ConfiguredAbilityPacket>
{
	@Override
	public void encode(ConfiguredAbilityPacket packet, PacketBuffer buffer)
	{
		for(Ability ability : packet.getAbilities())
		{
			Optional<CompoundNBT> serialized = ability.getConfigurableAbility().serializeNBTIAmTooDumbForJava(ability);
			
			if(serialized.isPresent())
			{
				buffer.writeString(ability.getResourceLocation().toString());
				buffer.writeString(ability.getConfigurableAbility().getRegistryName().toString());
				buffer.writeCompoundTag(serialized.get());
			}
		}
		
		buffer.writeString("");
	}

	@Override
	public ConfiguredAbilityPacket decode(PacketBuffer buffer)
	{
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		
		readAbilityLoop:
		while(true)
		{
			String abilityName = buffer.readString();
			
			if(abilityName.isEmpty())
				break readAbilityLoop;
			else
			{
				String configurableAbilityName = buffer.readString();
				CompoundNBT nbt = buffer.readCompoundTag();
				
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
			packet.getAbilities().forEach(ability -> BMorphMod.DYNAMIC_ABILITY_REGISTRY.registerAbility(ability));
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
