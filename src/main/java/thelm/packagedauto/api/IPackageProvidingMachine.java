package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;

public interface IPackageProvidingMachine {

	ItemStack getPatternStack();

	void setPatternStack(ItemStack stack);
}
