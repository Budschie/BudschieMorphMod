package de.budschie.bmorph.morph;

public class VanillaFallbackMorphData
{
	public static void intialiseFallbackData()
	{
//		MorphManagerHandlers.FALLBACK.addDataHandler(EntityType.VILLAGER, new SpecialDataHandler(
//				(item, toCompare) ->
//				{
//					CompoundNBT firstNbt = item.serialize();
//					CompoundNBT secondNbt = toCompare.serialize();
//				}, 
//				(type, nbt) ->
//				{
//					return type.getRegistryName().toString().hashCode();
//				}, 
//				(nbt) ->
//				{
//					CompoundNBT newNbt = new CompoundNBT();
//					
//					return newNbt;
//				}));
		
//		MorphManagerHandlers.FALLBACK.addDataHandler(EntityType.VILLAGER, new SpecialDataHandler(
//				(item, toCompare) ->
//				{
//					CompoundNBT firstNbt = item.serialize();
//					CompoundNBT secondNbt = toCompare.serialize();
//					
//					CompoundNBT firstVillagerData = firstNbt.getCompound("VillagerData");
//					CompoundNBT secondVillagerData = secondNbt.getCompound("VillagerData");
//					
//					return firstNbt.getInt("Age") == secondNbt.getInt("Age") && firstVillagerData.getString("profession").equals(secondVillagerData.getString("profession")) && firstVillagerData.getString("type").equals(secondVillagerData.getString("type"));
//				}, 
//				(type, nbt) ->
//				{
//					return type.getRegistryName().toString().hashCode() ^ nbt.getInt("Age") ^ nbt.getCompound("VillagerData").getString("type").hashCode() ^ nbt.getCompound("VillagerData").getString("profession").hashCode();
//				}, 
//				(nbt) ->
//				{
//					CompoundNBT newNbt = new CompoundNBT();
//					CompoundNBT villagerData = new CompoundNBT();
//					
//					newNbt.putInt("Age", nbt.getInt("Age") >= 0 ? 0 : -1);
//					villagerData.putString("type", nbt.getCompound("VillagerData").getString("type"));
//					villagerData.putString("profession", nbt.getCompound("VillagerData").getString("profession"));
//					newNbt.put("VillagerData", villagerData);
//					
//					return newNbt;
//				}));
	}
}
