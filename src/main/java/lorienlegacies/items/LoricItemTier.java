package lorienlegacies.items;

import java.util.function.Supplier;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;

public enum LoricItemTier implements IItemTier
{
	
	// Harvest level, durability, efficiency, damage, enchanability, repair item
	LORICMETAL(3, 3072, 2.5f, 5.5f, 3, () -> { return Ingredient.fromItems(ModItems.loralite.get()); });

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final LazyValue<Ingredient> repairMaterial;

	
	private LoricItemTier(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn, int enchantabilityIn, Supplier<Ingredient> repairMaterialIn)
	{
	      this.harvestLevel = harvestLevelIn;
	      this.maxUses = maxUsesIn;
	      this.efficiency = efficiencyIn;
	      this.attackDamage = attackDamageIn;
	      this.enchantability = enchantabilityIn;
	      this.repairMaterial = new LazyValue<>(repairMaterialIn);
	  }
	
	@Override
	public int getMaxUses() { return this.maxUses; }

	@Override
	public float getEfficiency() { return this.efficiency; }

	@Override
	public float getAttackDamage() { return this.attackDamage; }

	@Override
	public int getHarvestLevel() { return this.harvestLevel; }

	@Override
	public int getEnchantability() { return this.enchantability; }

	@Override
	public Ingredient getRepairMaterial() { return this.repairMaterial.getValue(); }
}
