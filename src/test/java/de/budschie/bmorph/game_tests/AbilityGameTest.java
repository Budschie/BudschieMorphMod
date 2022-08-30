package de.budschie.bmorph.game_tests;

import java.util.Optional;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(value = References.MODID)
public class AbilityGameTest
{	
	// Note: We need structure
	/** Tests all abilities by enabling all of them, using all of them and finally disabling them all. Remember that this is just a crude test. **/
	@GameTest(timeoutTicks = 500, template = "almost_empty")
	public static void testGame(final GameTestHelper test)
	{		
		test.runAtTickTime(0, () ->
		{
			try
			{
				for (Player player : test.getLevel().players())
					MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_GAMETEST.get(), player);
			} 
			catch (Exception ex)
			{
				test.fail("Testing all abilities failed whilst demorphing everybody: " + ex.getMessage());
			}
		});

		test.runAtTickTime(1, () ->
		{
			try
			{
				for (Player player : test.getLevel().players())
					MorphUtil.morphToServer(Optional.of(new FallbackMorphItem(EntityType.PIG)), MorphReasonRegistry.MORPHED_BY_GAMETEST.get(), player);
			} 
			catch (Exception ex)
			{
				test.fail("Testing all abilities failed whilst morphing everyone to an entity: " + ex.getMessage());
			}

		});

		test.runAtTickTime(2, () ->
		{
			try
			{
				for (Player player : test.getLevel().players())
				{
					IMorphCapability cap = MorphUtil.getCapOrNull(player);

					for (Ability ability : BMorphMod.DYNAMIC_ABILITY_REGISTRY.values())
					{
						cap.applyAbility(ability);
					}
				}
			} 
			catch (Exception ex)
			{
				test.fail("Testing all abilities whilst enabling everyone's abilities: " + ex.getMessage());
			}

		});

		test.runAtTickTime(3, () ->
		{
			try
			{
				for (Player player : test.getLevel().players())
				{
					IMorphCapability cap = MorphUtil.getCapOrNull(player);

					cap.useAbility();
				}
			} 
			catch (Exception ex)
			{
				test.fail("Testing all abilities whilst using everyone's abilities: " + ex.getMessage());
			}

		});

		test.runAtTickTime(400, () ->
		{
			try
			{
				for (Player player : test.getLevel().players())
				{
					MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_GAMETEST.get(), player);
				}
			} 
			catch (Exception ex)
			{
				test.fail("Testing all abilities failed whilst demorphing every player: " + ex.getMessage());
			}
			
			test.succeed();
		});
	}
}
