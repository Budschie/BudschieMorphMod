package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.configurable.NoFlames;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public class FireImmunityMixin
{
	@Inject(method = "fireImmune()Z", at = @At("HEAD"), cancellable = true)
	private void fireImmuneInject(CallbackInfoReturnable<Boolean> callback)
	{
		Entity entity = (Entity)((Object)this);
		
		if(entity instanceof Player player)
		{
			MorphUtil.processCap(player, cap ->
			{
				boolean markedNoFlames = false;
				
				if(cap.getCurrentAbilities() != null)
				{
					markCheck:
					for(Ability ability : cap.getCurrentAbilities())
					{
						if(ability instanceof NoFlames)
						{
							markedNoFlames = true;
							break markCheck;
						}
					}
				}
				
				if(markedNoFlames)
					callback.setReturnValue(true);
			});
		}
	}
}
