package de.budschie.bmorph.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * This is a class to streamline the (de-)serialization of particle
 * "clouds"(like the particles summoned by the /particle command) on the server.
 **/
public class ParticleCloudInstance
{
	public static final Codec<ParticleCloudInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.PARTICLE_TYPE.fieldOf("particle_type").forGetter(ParticleCloudInstance::getParticleType),
			ModCodecs.VECTOR_3D.optionalFieldOf("offset", new Vec3(0, 0, 0)).forGetter(ParticleCloudInstance::getOffset),
			ModCodecs.VECTOR_3D.optionalFieldOf("delta", new Vec3(1, 1, 1)).forGetter(ParticleCloudInstance::getDelta),
			Codec.DOUBLE.optionalFieldOf("speed", 1.0d).forGetter(ParticleCloudInstance::getSpeed),
			Codec.INT.optionalFieldOf("count", 100).forGetter(ParticleCloudInstance::getCount))
			.apply(instance, ParticleCloudInstance::new));
	
	private ParticleType<? extends ParticleOptions> type;
	
	private Vec3 offset;
	
	private Vec3 delta;
	
	private double speed;
	
	private int count;
	
	public ParticleCloudInstance(ParticleType<?> type, Vec3 offset, Vec3 delta, double speed, int count)
	{
		this.type = type;
		
		this.offset = offset;
		this.delta = delta;
		
		this.speed = speed;
		this.count = count;
	}
	
	public ParticleCloudInstance(ParticleType<?> type, double offsetX, double offsetY, double offsetZ, double deltaX, double deltaY, double deltaZ, double speed, int count)
	{
		this(type, new Vec3(offsetX, offsetY, offsetZ), new Vec3(deltaX, deltaY, deltaZ), speed, count);
	}
	
	public ParticleType<? extends ParticleOptions> getParticleType()
	{
		return type;
	}
	
	public Vec3 getOffset()
	{
		return offset;
	}
	
	public Vec3 getDelta()
	{
		return delta;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	
	public int getCount()
	{
		return count;
	}
	
	private ParticleOptions getParticleOrThrow()
	{
		return (type.codec().decode(JsonOps.INSTANCE, new JsonObject()).getOrThrow(false, (a) -> {})).getFirst();
	}
	
	public void placeParticleCloudOnClient(Vec3 position)
	{
		Vec3 finalPos = position.add(offset);
		
		// Note: The implementation is a bit hacky, but it will work for now.
		ClientboundLevelParticlesPacket clpp = new ClientboundLevelParticlesPacket(getParticleOrThrow(), false, finalPos.x, finalPos.y, finalPos.z, (float)delta.x, (float)delta.y, (float)delta.z, (float)speed, count);
		
		// Now comes the hacky part yaaayS
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			Minecraft.getInstance().getConnection().handleParticleEvent(clpp);
		});
	}
	
	public void placeParticleCloudOnServer(ServerLevel serverLevel, Vec3 position)
	{
		Vec3 finalPos = position.add(offset);
		
		// This may not be right
		// I don't like the creation of the JsonObject but f it
		serverLevel.sendParticles(getParticleOrThrow(), finalPos.x, finalPos.y, finalPos.z, count, delta.x, delta.y, delta.z, speed);
	}
}
