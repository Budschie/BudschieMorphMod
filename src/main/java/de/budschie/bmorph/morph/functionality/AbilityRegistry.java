package de.budschie.bmorph.morph.functionality;

import java.util.Random;
import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.potion.Effect;
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
	
	public static RegistryObject<Ability> GHAST_SHOOTING_ABILITY = ABILITY_REGISTRY.register("fire_ghast", () -> new ProjectileShootingAbility((player, direction) -> 
	{
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
		
		return new FireballEntity(player.world, player, direction.x, direction.y, direction.z);
	}, 50));

	
	public static RegistryObject<Ability> NO_FIRE_DAMAGE_ABILITY = ABILITY_REGISTRY.register("no_fire_damage", () -> new NoFireDamageAbility());
	public static RegistryObject<Ability> NO_FALL_DAMAGE_ABILITY = ABILITY_REGISTRY.register("no_fall_damage", () -> new NoFallDamageAbility());

	public static RegistryObject<Ability> BOOM = ABILITY_REGISTRY.register("boom", () -> new Boom());
	
	public static RegistryObject<Ability> NIGHT_VISION_ABILITY = ABILITY_REGISTRY.register("night_vision", () -> new NightVisionAbility());
	
	public static RegistryObject<Ability> SLOWNESS_ABILITY = ABILITY_REGISTRY.register("slowness", () -> 
	{
		return new PassiveTickAbility(10, (player, morph) ->
		{
			player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50, 1, true, false, false, null));
		});
	});

	
//	public static RegistryObject<Ability> CLIMBING_ABILITY = ABILITY_REGISTRY.register("climbing", () -> new PassiveTickAbility(0, (player, morph) ->
//	{
//		if(player.collidedHorizontally)
//			player.setMotion(player.getMotion().add(0, 20f, 0));
//	}));
	
	public static RegistryObject<Ability> CLIMBING_ABILITY = ABILITY_REGISTRY.register("climbing", () -> new ClimbingAbility());
	
	public static RegistryObject<Ability> SLOWFALL_ABILITY = ABILITY_REGISTRY.register("slowfall", () -> new PassiveTickAbility(10, (player, morph) ->
	{
		player.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 20, 2, true, false, false, null));
	}));

	public static RegistryObject<Ability> JUMPBOOST_ABILITY = ABILITY_REGISTRY.register("jumpboost", () -> new PassiveTickAbility(10, (player, morph) ->
	{
		player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 20, 2, true, false, false, null));
	}));

	
	public static RegistryObject<Ability> YEET_ABILITY = ABILITY_REGISTRY.register("yeet", () -> new YeetAbility());
	public static RegistryObject<Ability> NO_KNOCKBACK_ABILITY = ABILITY_REGISTRY.register("no_knockback", () -> new NoKnockbackAbility());
	
	public static RegistryObject<Ability> LLAMA_SPIT_ABILITY = ABILITY_REGISTRY.register("llama_spit", () -> new ProjectileShootingAbility((player, direction) -> 
	{
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
		
		//return new LlamaSpitEntity(player.world, player, direction.x, direction.y, direction.z);
		LlamaSpitEntity entity = new LlamaSpitEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), direction.x, direction.y, direction.z);
		entity.setShooter(player);
		entity.setFire(42);
		return entity;
	}, 3));
	
	public static RegistryObject<Ability> SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
	{
		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 2, true, false, false, null));
	}));
	
	public static RegistryObject<Ability> EXTREME_SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("extreme_swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
	{
		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 4, true, false, false, null));
	}));
	
	public static RegistryObject<Ability> INSTAJUMP_ABILITY = ABILITY_REGISTRY.register("insta_jump", () -> new InstaJumpAbility());
	
	public static final RegistryObject<Ability> EAT_REGEN_ABILITY = ABILITY_REGISTRY.register("eat_regen", () -> new InstaRegenAbility());
	
	public static final RegistryObject<Ability> WITHER_ON_HIT_ABILITY = ABILITY_REGISTRY.register("wither_on_hit", () -> new WitherEffectOnHitAbility());
}
