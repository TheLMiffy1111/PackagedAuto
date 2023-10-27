package thelm.packagedauto.api;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public interface IMiscHelper {

	List<ItemStack> condenseStacks(Container container);

	List<ItemStack> condenseStacks(IItemHandler itemHandler);

	List<ItemStack> condenseStacks(ItemStack... stacks);

	List<ItemStack> condenseStacks(Stream<ItemStack> stacks);

	List<ItemStack> condenseStacks(Iterable<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize);

	ListTag saveAllItems(ListTag tagList, List<ItemStack> list);

	void loadAllItems(ListTag tagList, List<ItemStack> list);

	IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index);

	List<ItemStack> getRemainingItems(Container container);

	List<ItemStack> getRemainingItems(Container container, int minInclusive, int maxExclusive);

	List<ItemStack> getRemainingItems(ItemStack... stacks);

	List<ItemStack> getRemainingItems(List<ItemStack> stacks);

	ItemStack getContainerItem(ItemStack stack);

	ItemStack cloneStack(ItemStack stack, int stackSize);

	boolean isEmpty(IItemHandler itemHandler);

	CompoundTag saveRecipe(CompoundTag nbt, IPackageRecipeInfo recipe);

	IPackageRecipeInfo loadRecipe(CompoundTag nbt);

	boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate);

	boolean arePatternsDisjoint(List<IPackagePattern> patternList);

	ItemStack insertItem(IItemHandler itemHandler, ItemStack stack, boolean requireEmptySlot, boolean simulate);

	ItemStack fillVolume(BlockEntity blockEntity, Direction direction, ItemStack stack, boolean simulate);

	<T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier);

	RecipeManager getRecipeManager();
}
