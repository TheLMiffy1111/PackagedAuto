package thelm.packagedauto.api;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

public interface IMiscHelper {

	List<ItemStack> condenseStacks(IInventory inventory);

	List<ItemStack> condenseStacks(IItemHandler itemHandler);

	List<ItemStack> condenseStacks(ItemStack... stacks);

	List<ItemStack> condenseStacks(Stream<ItemStack> stacks);

	List<ItemStack> condenseStacks(Iterable<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize);

	ListNBT saveAllItems(ListNBT tagList, List<ItemStack> list);

	ListNBT saveAllItems(ListNBT tagList, List<ItemStack> list, String indexKey);

	void loadAllItems(ListNBT tagList, List<ItemStack> list);

	void loadAllItems(ListNBT tagList, List<ItemStack> list, String indexKey);

	CompoundNBT saveItemWithLargeCount(CompoundNBT nbt, ItemStack stack);

	ItemStack loadItemWithLargeCount(CompoundNBT nbt);

	void writeItemWithLargeCount(PacketBuffer buf, ItemStack stack);

	ItemStack readItemWithLargeCount(PacketBuffer buf);

	IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index);

	List<ItemStack> getRemainingItems(IInventory inventory);

	List<ItemStack> getRemainingItems(IInventory inventory, int minInclusive, int maxExclusive);

	List<ItemStack> getRemainingItems(ItemStack... stacks);

	List<ItemStack> getRemainingItems(List<ItemStack> stacks);

	ItemStack getContainerItem(ItemStack stack);

	ItemStack cloneStack(ItemStack stack, int stackSize);

	boolean isEmpty(IItemHandler itemHandler);

	CompoundNBT writeRecipe(CompoundNBT nbt, IPackageRecipeInfo recipe);

	IPackageRecipeInfo readRecipe(CompoundNBT nbt);

	ListNBT writeRecipeList(ListNBT tagList, List<IPackageRecipeInfo> recipes);

	List<IPackageRecipeInfo> readRecipeList(ListNBT tagList);

	boolean recipeEquals(IPackageRecipeInfo recipeA, Object recipeInternalA, IPackageRecipeInfo recipeB, Object recipeInternalB);

	int recipeHashCode(IPackageRecipeInfo recipe, Object recipeInternal);

	boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate);

	boolean arePatternsDisjoint(List<IPackagePattern> patternList);

	ItemStack insertItem(IItemHandler itemHandler, ItemStack stack, boolean requireEmptySlot, boolean simulate);

	Runnable conditionalRunnable(BooleanSupplier conditionSupplier, Supplier<Runnable> trueRunnable, Supplier<Runnable> falseRunnable);

	<T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier);

	RecipeManager getRecipeManager();
}
