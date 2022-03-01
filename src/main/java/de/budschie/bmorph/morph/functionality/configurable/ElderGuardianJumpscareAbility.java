package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.PassiveTickAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class ElderGuardianJumpscareAbility extends PassiveTickAbility
{
	public static final Codec<ElderGuardianJumpscareAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.INT.fieldOf("frequency").forGetter(ElderGuardianJumpscareAbility::getUpdateDuration),
					Codec.list(ModCodecs.EFFECT_INSTANCE).fieldOf("effect_instances").forGetter(ElderGuardianJumpscareAbility::getEffectInstances),
					Codec.DOUBLE.fieldOf("distance").forGetter(ElderGuardianJumpscareAbility::getDistance))
			.apply(instance, ElderGuardianJumpscareAbility::new));
	
	private List<MobEffectInstance> effectInstances;
	
	private double distance;
	
	public ElderGuardianJumpscareAbility(int updateDuration, List<MobEffectInstance> effectInstances, double distance)
	{
		super(updateDuration, (player, cap) ->
		{			
            if(!player.level.isClientSide)
            {
            	double distanceSquared = distance * distance;
            	
            	player.getCommandSenderWorld().players().stream()
            	.filter(playerToFilter -> playerToFilter.position().distanceToSqr(player.position()) < distanceSquared)
            	.forEach(playerToJumpscare ->
            	{
            		if(playerToJumpscare != player)
            		{
	            		((ServerPlayer)playerToJumpscare).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0F));
	            		effectInstances.forEach(effect -> playerToJumpscare.addEffect(new MobEffectInstance(effect)));
            		}
            	});
            }
		});
		
		this.distance = distance;
		this.effectInstances = effectInstances;
	}
	
	public List<MobEffectInstance> getEffectInstances()
	{
		return effectInstances;
	}
	
	public double getDistance()
	{
		return distance;
	}
}
