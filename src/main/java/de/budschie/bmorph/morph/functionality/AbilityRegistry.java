package de.budschie.bmorph.morph.functionality;

import java.util.Random;
import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.entity.passive.fish.SalmonEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
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
	public static RegistryObject<Ability> MOB_ATTACK_ABILITY = ABILITY_REGISTRY.register("mob_attack", () -> new MobAttackAbility());
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
	
	// This is stupid. I should use the attributes directly instead of effects.
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

	
	public static RegistryObject<Ability> YEET_ABILITY = ABILITY_REGISTRY.register("yeet", () -> new AttackYeetAbility());
	public static RegistryObject<Ability> NO_KNOCKBACK_ABILITY = ABILITY_REGISTRY.register("no_knockback", () -> new NoKnockbackAbility());
	
	// No stun or anything like that xD perfectly balanced, as all things should be
	public static RegistryObject<Ability> LLAMA_SPIT_ABILITY = ABILITY_REGISTRY.register("llama_spit", () -> new ProjectileShootingAbility((player, direction) -> 
	{
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
		
		//return new LlamaSpitEntity(player.world, player, direction.x, direction.y, direction.z);
		LlamaSpitEntity entity = new LlamaSpitEntity(EntityType.LLAMA_SPIT, player.world);
		entity.setShooter(player);
		entity.setFire(42);
		entity.setMotion(direction.x, direction.y, direction.z);
		return entity;
	}, 0));
	
//	public static RegistryObject<Ability> SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
//	{
//		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 2, true, false, false, null));
//	}));
//	
//	public static RegistryObject<Ability> EXTREME_SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("extreme_swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
//	{
//		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 4, true, false, false, null));
//	}));
	
	public static RegistryObject<Ability> SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("swiftness", () -> new SpeedAbility(0.2F * 3));
	
	public static RegistryObject<Ability> EXTREME_SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("extreme_swiftness", () -> new SpeedAbility(0.2F * 5));
	
	public static RegistryObject<Ability> INSTAJUMP_ABILITY = ABILITY_REGISTRY.register("insta_jump", () -> new InstaJumpAbility());
	
	public static final RegistryObject<Ability> EAT_REGEN_ABILITY = ABILITY_REGISTRY.register("eat_regen", () -> new InstaRegenAbility());
	
	public static final RegistryObject<Ability> WITHER_ON_HIT_ABILITY = ABILITY_REGISTRY.register("wither_on_hit", () -> new WitherEffectOnHitAbility());

	public static final RegistryObject<Ability> MORE_DAMAGE_ABILITY = ABILITY_REGISTRY.register("more_damage", () -> new AttackAbility(event ->
	{
		event.setAmount(event.getAmount() + 3);
	}));
	
	public static final RegistryObject<Ability> NAUSEA_ON_HIT_ABILITY = ABILITY_REGISTRY.register("nausea_on_hit", () -> new AttackAbility(event -> event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.NAUSEA, 150, 12))));
	
	public static final RegistryObject<Ability> POISON_ON_HIT_ABILITY = ABILITY_REGISTRY.register("poison_on_hit", () -> new AttackAbility(event -> event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.POISON, 40, 4))));

	public static final RegistryObject<Ability> WATER_BREATHING_ABILITY = ABILITY_REGISTRY.register("water_breathing", () -> new WaterBreathingAbility());

	public static final RegistryObject<Ability> WATER_SANIC_ABILITY = ABILITY_REGISTRY.register("water_sanic", () -> new WaterSanicAbility());
	
	public static final RegistryObject<Ability> SQUID_BOOST_ABILITY = ABILITY_REGISTRY.register("squid_boost", () -> new SquidBoostAbility());

	public static final RegistryObject<Ability> WATER_DISLIKE_ABILITY = ABILITY_REGISTRY.register("water_dislike", () -> new WaterDislikeAbility());
	
	public static final RegistryObject<Ability> ENDERMAN_TELEPORT_ABILITY = ABILITY_REGISTRY.register("enderman_teleport", () -> new TeleportAbility(40));
	
	public static final RegistryObject<Ability> EGG_YEET_ABILITY = ABILITY_REGISTRY.register("egg_yeet", () -> new YeetAbility(10, player -> 
	{
		EggEntity entity = new EggEntity(player.world, player);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 7, .7f);
		return entity;
	}, 2f));
	
	public static final RegistryObject<Ability> FISH_YEET_ABILITY = ABILITY_REGISTRY.register("fish_yeet", () -> new YeetAbility(90, player -> 
	{
		AbstractFishEntity entity = null;
		
		int fish = new Random().nextInt(4);
		
		switch (fish)
		{
		case 0:
			entity = new CodEntity(EntityType.COD, player.world);
			break;

		case 2:
			entity = new SalmonEntity(EntityType.SALMON, player.world);
			break;

		case 3:
			entity = new TropicalFishEntity(EntityType.TROPICAL_FISH, player.world);
			break;

		default:
			entity = new PufferfishEntity(EntityType.PUFFERFISH, player.world);
			break;
		}
		
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PUFFER_FISH_FLOP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PUFFER_FISH_FLOP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.NEUTRAL, 10, 1);
		return entity;
	}, .25f));

	
	public static final RegistryObject<Ability> WEB_YEET_ABILITY = ABILITY_REGISTRY.register("web_yeet", () -> new YeetAbility(10, player -> 
	{
		FallingBlockEntity entity = new FallingBlockEntity(player.world, 0, 0, 0, Blocks.COBWEB.getDefaultState());
		entity.fallTime = -69420;
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 10, .7f);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
		return entity;
	}, 2.5f));
	
	public static final RegistryObject<Ability> WEB_PASSTHROUGH_ABILITY = ABILITY_REGISTRY.register("web_passthrough", () -> new WebSpeedAbility());
	
	public static final RegistryObject<Ability> COOKIE_DEATH_ABILITY = ABILITY_REGISTRY.register("cookie_death", () -> new InstaDeathOnCookieAbility());
}
