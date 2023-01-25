package thelm.packagedauto.api;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

public interface IMiscHelper {

	List<ItemStack> condenseStacks(IInventory inventory);

	List<ItemStack> condenseStacks(ItemStack... stacks);

	List<ItemStack> condenseStacks(Stream<ItemStack> stacks);

	List<ItemStack> condenseStacks(Iterable<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize);

	NBTTagList saveAllItems(NBTTagList tagList, List<ItemStack> list);

	void loadAllItems(NBTTagList tagList, List<ItemStack> list);

	IPackagePattern getPatternHelper(IPackageRecipeInfo recipeInfo, int index);

	List<ItemStack> getRemainingItems(IInventory inventory);

	List<ItemStack> getRemainingItems(IInventory inventory, int minInclusive, int maxExclusive);

	List<ItemStack> getRemainingItems(ItemStack... stacks);

	List<ItemStack> getRemainingItems(List<ItemStack> stacks);

	ItemStack getContainerItem(ItemStack stack);

	ItemStack cloneStack(ItemStack stack, int stackSize);

	boolean isEmpty(IInventory inventory, ForgeDirection side);

	NBTTagCompound writeRecipeToNBT(NBTTagCompound nbt, IPackageRecipeInfo recipe);

	IPackageRecipeInfo readRecipeFromNBT(NBTTagCompound nbt);

	boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate);

	boolean arePatternsDisjoint(List<IPackagePattern> patternList);

	<T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier);

	int[] getSlots(IInventory inv, ForgeDirection side);

	ItemStack insertItem(IInventory inv, int slot, ForgeDirection side, ItemStack stack, boolean simulate);

	ItemStack extractItem(IInventory inv, int slot, ForgeDirection side, int amount, boolean simulate);

	<T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests);
}
