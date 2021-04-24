package de.budschie.bmorph.morph.functionality;

import java.util.Random;
import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = Bus.MOD)
public class AbilityRegistry
{	
	public static DeferredRegister<Ability> ABILITY_REGISTRY = DeferredRegister.create(Ability.class, References.MODID); 
	public static Supplier<IForgeRegistry<Ability>> REGISTRY = ABILITY_REGISTRY.makeRegistry("abilities", () -> new RegistryBuilder<>());
	
	public static RegistryObject<Ability> FLY_ABILITY = ABILITY_REGISTRY.register("flying", () -> new FlyAbility());
	public static RegistryObject<Ability> MOB_ATTACK_ABILITY = ABILITY_REGISTRY.register("mob_ignore", () -> new MobAttackAbility());
	public static RegistryObject<Ability> FIRE_SHOOTING_ABILITY = ABILITY_REGISTRY.register("fire_blaze", () -> new ProjectileShootingAbility((player, direction) -> 
	{
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
		
		return new SmallFireballEntity(player.world, player, direction.x, direction.y, direction.z);
	}, 20));
	
	public static RegistryObject<Ability> NO_FIRE_DAMAGE_ABILITY = ABILITY_REGISTRY.register("no_fire_damage", () -> new NoFireDamageAbility());
	public static RegistryObject<Ability> BOOM = ABILITY_REGISTRY.register("boom", () -> new Boom());
	
	public static RegistryObject<Ability> NIGHT_VISION_ABILITY = ABILITY_REGISTRY.register("night_vision", () -> new PassiveTickAbility(10, (player, morph) ->
	{
		player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 50, 10, true, false, false, null));
	}));
}
