package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.evoker.EvokerSpellCapabilityInstance;
import de.budschie.bmorph.capabilities.evoker.IEvokerSpellCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.SpellcasterIllager.IllagerSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class EvokerSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Evoker;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		LazyOptional<IEvokerSpellCapability> optionalCap = player.getCapability(EvokerSpellCapabilityInstance.EVOKER_SPELL_CAP);
		
		if(optionalCap.isPresent())
		{
			IEvokerSpellCapability cap = optionalCap.resolve().get();
			
			Evoker casted = (Evoker) morphEntity;
			
			if(cap.isCasting())
			{
				casted.setIsCastingSpell(IllagerSpell.FANGS);
			}
			else
			{
				casted.setIsCastingSpell(IllagerSpell.NONE);
			}
		}
	}
}
