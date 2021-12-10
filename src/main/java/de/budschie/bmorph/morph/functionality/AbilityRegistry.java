package de.budschie.bmorph.morph.functionality;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.functionality.configurable.AirSuffocationAbility;
import de.budschie.bmorph.morph.functionality.configurable.AttackYeetAbility;
import de.budschie.bmorph.morph.functionality.configurable.AttributeModifierAbility;
import de.budschie.bmorph.morph.functionality.configurable.BlockPassthroughAbility;
import de.budschie.bmorph.morph.functionality.configurable.Boom;
import de.budschie.bmorph.morph.functionality.configurable.BurnInSunAbility;
import de.budschie.bmorph.morph.functionality.configurable.ClimbingAbility;
import de.budschie.bmorph.morph.functionality.configurable.CommandOnDisable;
import de.budschie.bmorph.morph.functionality.configurable.CommandOnEnable;
import de.budschie.bmorph.morph.functionality.configurable.CommandOnUseAbility;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import de.budschie.bmorph.morph.functionality.configurable.DamageImmunityAbility;
import de.budschie.bmorph.morph.functionality.configurable.EffectOnAttackEntity;
import de.budschie.bmorph.morph.functionality.configurable.ElderGuardianJumpscareAbility;
import de.budschie.bmorph.morph.functionality.configurable.FlyAbility;
import de.budschie.bmorph.morph.functionality.configurable.GuardianAbility;
import de.budschie.bmorph.morph.functionality.configurable.InstaDeathOnCookieAbility;
import de.budschie.bmorph.morph.functionality.configurable.InstaJumpAbility;
import de.budschie.bmorph.morph.functionality.configurable.InstaRegenAbility;
import de.budschie.bmorph.morph.functionality.configurable.MobAttackAbility;
import de.budschie.bmorph.morph.functionality.configurable.NoKnockbackAbility;
import de.budschie.bmorph.morph.functionality.configurable.PassiveEffectAbility;
import de.budschie.bmorph.morph.functionality.configurable.PassiveTickCommandAbility;
import de.budschie.bmorph.morph.functionality.configurable.PhantomAbility;
import de.budschie.bmorph.morph.functionality.configurable.ProjectileShootingAbility;
import de.budschie.bmorph.morph.functionality.configurable.PufferfishAbility;
import de.budschie.bmorph.morph.functionality.configurable.RandomDelegatingOnUseAbility;
import de.budschie.bmorph.morph.functionality.configurable.SoundOnUseAbility;
import de.budschie.bmorph.morph.functionality.configurable.SquidBoostAbility;
import de.budschie.bmorph.morph.functionality.configurable.TeleportAbility;
import de.budschie.bmorph.morph.functionality.configurable.TransformEntityOnDeath;
import de.budschie.bmorph.morph.functionality.configurable.WalkOnPowderedSnowAbility;
import de.budschie.bmorph.morph.functionality.configurable.WaterBreathingAbility;
import de.budschie.bmorph.morph.functionality.configurable.WaterDislikeAbility;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = Bus.MOD)
public class AbilityRegistry
{	
	@SuppressWarnings("unchecked")
	public static DeferredRegister<ConfigurableAbility<? extends Ability>> ABILITY_REGISTRY = DeferredRegister.<ConfigurableAbility<? extends Ability>>create((Class<ConfigurableAbility<?>>)((Class<?>)ConfigurableAbility.class), References.MODID); 
	
	public static Supplier<IForgeRegistry<ConfigurableAbility<? extends Ability>>> REGISTRY = ABILITY_REGISTRY.makeRegistry("abilities", () -> new RegistryBuilder<ConfigurableAbility<?>>().disableSaving());
	
	public static RegistryObject<ConfigurableAbility<Boom>> BOOM = ABILITY_REGISTRY.register("boom", () -> new ConfigurableAbility<>(Boom.CODEC));
	public static RegistryObject<ConfigurableAbility<AttackYeetAbility>> ATTACK_YEET = ABILITY_REGISTRY.register("yeet", () -> new ConfigurableAbility<>(AttackYeetAbility.CODEC));
	public static RegistryObject<ConfigurableAbility<ClimbingAbility>> CLIMBING_ABILITY = ABILITY_REGISTRY.register("climbing", () -> new ConfigurableAbility<>(ClimbingAbility.CODEC));
	public static RegistryObject<ConfigurableAbility<FlyAbility>> FLYING_ABILITY = ABILITY_REGISTRY.register("flying", () -> new ConfigurableAbility<>(FlyAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<PassiveTickCommandAbility>> PASSIVE_TICK_COMMAND_ABILITY = ABILITY_REGISTRY.register("ticking_command", () -> new ConfigurableAbility<>(PassiveTickCommandAbility.CODEC));
	public static RegistryObject<ConfigurableAbility<CommandOnEnable>> COMMAND_ON_ENABLE_ABILITY = ABILITY_REGISTRY.register("command_on_enable", () -> new ConfigurableAbility<>(CommandOnEnable.CODEC));
	public static RegistryObject<ConfigurableAbility<CommandOnDisable>> COMMAND_ON_DISABLE_ABILITY = ABILITY_REGISTRY.register("command_on_disable", () -> new ConfigurableAbility<>(CommandOnDisable.CODEC));
	public static RegistryObject<ConfigurableAbility<CommandOnUseAbility>> COMMAND_ON_USE_ABILITY = ABILITY_REGISTRY.register("command_on_use", () -> new ConfigurableAbility<>(CommandOnUseAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<AttributeModifierAbility>> ATTRIBUTE_MODIFIER_ABILITY = ABILITY_REGISTRY.register("attribute_modifier", () -> new ConfigurableAbility<>(AttributeModifierAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<DamageImmunityAbility>> DAMAGE_IMMUNITY_ABILITY = ABILITY_REGISTRY.register("damage_immunity", () -> new ConfigurableAbility<>(DamageImmunityAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<EffectOnAttackEntity>> EFFECT_ON_ATTACK_ABILITY = ABILITY_REGISTRY.register("effect_on_attack", () -> new ConfigurableAbility<>(EffectOnAttackEntity.CODEC));

	public static RegistryObject<ConfigurableAbility<InstaDeathOnCookieAbility>> COOKIE_DEATH = ABILITY_REGISTRY.register("cookie_death", () -> new ConfigurableAbility<>(InstaDeathOnCookieAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<InstaJumpAbility>> INSTA_JUMP_ABILITY = ABILITY_REGISTRY.register("insta_jump", () -> new ConfigurableAbility<>(InstaJumpAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<InstaRegenAbility>> INSTA_REGEN_ABILITY = ABILITY_REGISTRY.register("insta_regen", () -> new ConfigurableAbility<>(InstaRegenAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<MobAttackAbility>> MOB_ATTACK_ABILITY = ABILITY_REGISTRY.register("mob_attack", () -> new ConfigurableAbility<>(MobAttackAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<NoKnockbackAbility>> NO_KNOCKBACK_ABILITY = ABILITY_REGISTRY.register("no_knockback", () -> new ConfigurableAbility<>(NoKnockbackAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<PassiveEffectAbility>> PASSIVE_EFFECT_ABILITY = ABILITY_REGISTRY.register("passive_effect", () -> new ConfigurableAbility<>(PassiveEffectAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<ProjectileShootingAbility>> PROJECTILE_SHOOTING_ABILITY = ABILITY_REGISTRY.register("shoot_projectile", () -> new ConfigurableAbility<>(ProjectileShootingAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<SquidBoostAbility>> SQUID_BOOST_ABILITY = ABILITY_REGISTRY.register("squid_boost", () -> new ConfigurableAbility<>(SquidBoostAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<TeleportAbility>> TELEPORT_ABILITY = ABILITY_REGISTRY.register("teleport", () -> new ConfigurableAbility<>(TeleportAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<WaterBreathingAbility>> WATER_BREATHING_ABILITY = ABILITY_REGISTRY.register("water_breathing", () -> new ConfigurableAbility<>(WaterBreathingAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<WaterDislikeAbility>> WATER_DISLIKE_ABILITY = ABILITY_REGISTRY.register("water_dislike", () -> new ConfigurableAbility<>(WaterDislikeAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<BlockPassthroughAbility>> BLOCK_PASSTHROUGH_ABILITY = ABILITY_REGISTRY.register("block_passthrough", () -> new ConfigurableAbility<>(BlockPassthroughAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<SoundOnUseAbility>> SOUND_ON_USE_ABILITY = ABILITY_REGISTRY.register("sound_on_use", () -> new ConfigurableAbility<>(SoundOnUseAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<PufferfishAbility>> PUFFERFISH_ABILITY = ABILITY_REGISTRY.register("pufferfish", () -> new ConfigurableAbility<>(PufferfishAbility.CODEC));
	public static RegistryObject<ConfigurableAbility<GuardianAbility>> GUARDIAN_ABILITY = ABILITY_REGISTRY.register("guardian_laser", () -> new ConfigurableAbility<>(GuardianAbility.CODEC));
	public static RegistryObject<ConfigurableAbility<ElderGuardianJumpscareAbility>> ELDER_GUARDIAN_JUMPSCARE_ABILITY = ABILITY_REGISTRY.register("elder_guardian_jumpscare", () -> new ConfigurableAbility<>(ElderGuardianJumpscareAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<RandomDelegatingOnUseAbility>> RANDOM_DELEGATING_ON_USE_ABILITY = ABILITY_REGISTRY.register("random_delegation_on_use", () -> new ConfigurableAbility<>(RandomDelegatingOnUseAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<WalkOnPowderedSnowAbility>> WALK_ON_POWDERED_SNOW = ABILITY_REGISTRY.register("walk_on_powder_snow", () -> new ConfigurableAbility<>(WalkOnPowderedSnowAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<BurnInSunAbility>> BURN_IN_SUN_ABILITY = ABILITY_REGISTRY.register("burn_in_sun", () -> new ConfigurableAbility<>(BurnInSunAbility.CODEC));

	public static RegistryObject<ConfigurableAbility<TransformEntityOnDeath>> TRANSFORM_ENTITY_ON_DEATH_ABILITY = ABILITY_REGISTRY.register("transform_on_kill", () -> new ConfigurableAbility<>(TransformEntityOnDeath.CODEC));

	public static RegistryObject<ConfigurableAbility<AirSuffocationAbility>> SUFFOCATE_ON_LAND = ABILITY_REGISTRY.register("suffocate_on_land", () -> new ConfigurableAbility<>(AirSuffocationAbility.CODEC));
	
	public static RegistryObject<ConfigurableAbility<PhantomAbility>> PHANTOM_FLIGHT = ABILITY_REGISTRY.register("phantom_glide", () -> new ConfigurableAbility<>(PhantomAbility.CODEC));

//	public static RegistryObject<Ability> FLY_ABILITY = ABILITY_REGISTRY.register("flying", () -> new FlyAbility());
//	public static RegistryObject<Ability> MOB_ATTACK_ABILITY = ABILITY_REGISTRY.register("mob_attack", () -> new MobAttackAbility());
//	public static RegistryObject<Ability> FIRE_SHOOTING_ABILITY = ABILITY_REGISTRY.register("fire_blaze", () -> new ProjectileShootingAbility((player, direction) -> 
//	{
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
//		
//		return new SmallFireballEntity(player.world, player, direction.x, direction.y, direction.z);
//	}, 20));
//	
//	public static RegistryObject<Ability> GHAST_SHOOTING_ABILITY = ABILITY_REGISTRY.register("fire_ghast", () -> new ProjectileShootingAbility((player, direction) -> 
//	{
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
//		
//		return new FireballEntity(player.world, player, direction.x, direction.y, direction.z);
//	}, 50));
//
//	
//	public static RegistryObject<Ability> NO_FIRE_DAMAGE_ABILITY = ABILITY_REGISTRY.register("no_fire_damage", () -> new NoFireDamageAbility());
//	public static RegistryObject<Ability> NO_FALL_DAMAGE_ABILITY = ABILITY_REGISTRY.register("no_fall_damage", () -> new NoFallDamageAbility());
//
//	public static RegistryObject<Ability> BOOM = ABILITY_REGISTRY.register("boom", () -> new Boom());
//	
//	public static RegistryObject<Ability> NIGHT_VISION_ABILITY = ABILITY_REGISTRY.register("night_vision", () -> new NightVisionAbility());
//	
//	// This is stupid. I should use the attributes directly instead of effects.
//	public static RegistryObject<Ability> SLOWNESS_ABILITY = ABILITY_REGISTRY.register("slowness", () -> 
//	{
//		return new PassiveTickAbility(10, (player, morph) ->
//		{
//			player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50, 1, true, false, false, null));
//		});
//	});
//
//	
////	public static RegistryObject<Ability> CLIMBING_ABILITY = ABILITY_REGISTRY.register("climbing", () -> new PassiveTickAbility(0, (player, morph) ->
////	{
////		if(player.collidedHorizontally)
////			player.setMotion(player.getMotion().add(0, 20f, 0));
////	}));
//	
//	public static RegistryObject<Ability> CLIMBING_ABILITY = ABILITY_REGISTRY.register("climbing", () -> new ClimbingAbility());
//	
//	public static RegistryObject<Ability> SLOWFALL_ABILITY = ABILITY_REGISTRY.register("slowfall", () -> new PassiveTickAbility(10, (player, morph) ->
//	{
//		player.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 20, 2, true, false, false, null));
//	}));
//
//	public static RegistryObject<Ability> JUMPBOOST_ABILITY = ABILITY_REGISTRY.register("jumpboost", () -> new PassiveTickAbility(10, (player, morph) ->
//	{
//		player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 20, 2, true, false, false, null));
//	}));
//
//	
//	public static RegistryObject<Ability> YEET_ABILITY = ABILITY_REGISTRY.register("yeet", () -> new AttackYeetAbility());
//	public static RegistryObject<Ability> NO_KNOCKBACK_ABILITY = ABILITY_REGISTRY.register("no_knockback", () -> new NoKnockbackAbility());
//	
//	// No stun or anything like that xD perfectly balanced, as all things should be
//	public static RegistryObject<Ability> LLAMA_SPIT_ABILITY = ABILITY_REGISTRY.register("llama_spit", () -> new ProjectileShootingAbility((player, direction) -> 
//	{
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.HOSTILE, 10, ((new Random(System.currentTimeMillis()).nextFloat() - .2f) / 2f + 1));
//		
//		//return new LlamaSpitEntity(player.world, player, direction.x, direction.y, direction.z);
//		LlamaSpitEntity entity = new LlamaSpitEntity(EntityType.LLAMA_SPIT, player.world);
//		entity.setShooter(player);
//		entity.setFire(42);
//		entity.setMotion(direction.x, direction.y, direction.z);
//		return entity;
//	}, 0));
//	
////	public static RegistryObject<Ability> SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
////	{
////		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 2, true, false, false, null));
////	}));
////	
////	public static RegistryObject<Ability> EXTREME_SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("extreme_swiftness", () -> new PassiveTickAbility(10, (player, morph) ->
////	{
////		player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 4, true, false, false, null));
////	}));
//	
//	public static RegistryObject<Ability> SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("swiftness", () -> new SpeedAbility(0.2F * 3));
//	
//	public static RegistryObject<Ability> EXTREME_SWIFTNESS_ABILITY = ABILITY_REGISTRY.register("extreme_swiftness", () -> new SpeedAbility(0.2F * 5));
//	
//	public static RegistryObject<Ability> INSTAJUMP_ABILITY = ABILITY_REGISTRY.register("insta_jump", () -> new InstaJumpAbility());
//	
//	public static final RegistryObject<Ability> EAT_REGEN_ABILITY = ABILITY_REGISTRY.register("eat_regen", () -> new InstaRegenAbility());
//	
//	public static final RegistryObject<Ability> WITHER_ON_HIT_ABILITY = ABILITY_REGISTRY.register("wither_on_hit", () -> new WitherEffectOnHitAbility());
//
//	public static final RegistryObject<Ability> MORE_DAMAGE_ABILITY = ABILITY_REGISTRY.register("more_damage", () -> new AttackAbility(event ->
//	{
//		event.setAmount(event.getAmount() + 12);
//	}));
//	
//	public static final RegistryObject<Ability> NAUSEA_ON_HIT_ABILITY = ABILITY_REGISTRY.register("nausea_on_hit", () -> new AttackAbility(event -> event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.NAUSEA, 150, 12))));
//	
//	public static final RegistryObject<Ability> POISON_ON_HIT_ABILITY = ABILITY_REGISTRY.register("poison_on_hit", () -> new AttackAbility(event -> event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.POISON, 40, 4))));
//
//	public static final RegistryObject<Ability> WATER_BREATHING_ABILITY = ABILITY_REGISTRY.register("water_breathing", () -> new WaterBreathingAbility());
//
//	public static final RegistryObject<Ability> WATER_SANIC_ABILITY = ABILITY_REGISTRY.register("water_sanic", () -> new WaterSanicAbility());
//	
//	public static final RegistryObject<Ability> SQUID_BOOST_ABILITY = ABILITY_REGISTRY.register("squid_boost", () -> new SquidBoostAbility());
//
//	public static final RegistryObject<Ability> WATER_DISLIKE_ABILITY = ABILITY_REGISTRY.register("water_dislike", () -> new WaterDislikeAbility());
//	
//	public static final RegistryObject<Ability> ENDERMAN_TELEPORT_ABILITY = ABILITY_REGISTRY.register("enderman_teleport", () -> new TeleportAbility(40));
//	
//	public static final RegistryObject<Ability> EGG_YEET_ABILITY = ABILITY_REGISTRY.register("egg_yeet", () -> new YeetAbility(10, player -> 
//	{
//		EggEntity entity = new EggEntity(player.world, player);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 7, .7f);
//		return entity;
//	}, 2f));
//	
//	public static final RegistryObject<Ability> FISH_YEET_ABILITY = ABILITY_REGISTRY.register("fish_yeet", () -> new YeetAbility(90, player -> 
//	{
//		AbstractFishEntity entity = null;
//		
//		int fish = new Random().nextInt(4);
//		
//		switch (fish)
//		{
//		case 0:
//			entity = new CodEntity(EntityType.COD, player.world);
//			break;
//
//		case 2:
//			entity = new SalmonEntity(EntityType.SALMON, player.world);
//			break;
//
//		case 3:
//			entity = new TropicalFishEntity(EntityType.TROPICAL_FISH, player.world);
//			break;
//
//		default:
//			entity = new PufferfishEntity(EntityType.PUFFERFISH, player.world);
//			break;
//		}
//		
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PUFFER_FISH_FLOP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PUFFER_FISH_FLOP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.NEUTRAL, 10, 1);
//		return entity;
//	}, .25f));
//
//	
//	public static final RegistryObject<Ability> WEB_YEET_ABILITY = ABILITY_REGISTRY.register("web_yeet", () -> new YeetAbility(10, player -> 
//	{
//		FallingBlockEntity entity = new FallingBlockEntity(player.world, 0, 0, 0, Blocks.COBWEB.getDefaultState());
//		entity.fallTime = -69420;
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.NEUTRAL, 10, .7f);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
//		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 10, new Random().nextFloat() * 0.9f + 0.1f);
//		return entity;
//	}, 2.5f));
//	
//	public static final RegistryObject<Ability> WEB_PASSTHROUGH_ABILITY = ABILITY_REGISTRY.register("web_passthrough", () -> new WebSpeedAbility());
//	
//	public static final RegistryObject<Ability> COOKIE_DEATH_ABILITY = ABILITY_REGISTRY.register("cookie_death", () -> new InstaDeathOnCookieAbility());
}
