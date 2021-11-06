package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.PassiveTickAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.potion.EffectInstance;

public class ElderGuardianJumpscareAbility extends PassiveTickAbility
{
	public static final Codec<ElderGuardianJumpscareAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.INT.fieldOf("frequency").forGetter(ElderGuardianJumpscareAbility::getUpdateDuration),
					Codec.list(ModCodecs.EFFECT_INSTANCE).fieldOf("effect_instances").forGetter(ElderGuardianJumpscareAbility::getEffectInstances),
					Codec.DOUBLE.fieldOf("distance").forGetter(ElderGuardianJumpscareAbility::getDistance))
			.apply(instance, ElderGuardianJumpscareAbility::new));
	
	private List<EffectInstance> effectInstances;
	
	private double distance;
	
	public ElderGuardianJumpscareAbility(int updateDuration, List<EffectInstance> effectInstances, double distance)
	{
		super(updateDuration, (player, cap) ->
		{			
            if(!player.getEntityWorld().isRemote)
            {
            	double distanceSquared = distance * distance;
            	
            	player.getEntityWorld().getPlayers().stream()
            	.filter(playerToFilter -> playerToFilter.getPositionVec().squareDistanceTo(player.getPositionVec()) < distanceSquared)
            	.forEach(playerToJumpscare ->
            	{
            		if(playerToJumpscare != player)
            		{
	            		((ServerPlayerEntity)playerToJumpscare).connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.CURSE_PLAYER_ELDER_GUARDIAN, 1.0F));
	            		effectInstances.forEach(effect -> playerToJumpscare.addPotionEffect(new EffectInstance(effect)));
            		}
            	});
            }
		});
		
		this.distance = distance;
		this.effectInstances = effectInstances;
	}
	
	public List<EffectInstance> getEffectInstances()
	{
		return effectInstances;
	}
	
	public double getDistance()
	{
		return distance;
	}
}
