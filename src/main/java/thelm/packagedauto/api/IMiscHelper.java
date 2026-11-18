package thelm.packagedauto.api;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface IMiscHelper {

	List<ItemStack> condenseStacks(Container container);

	List<ItemStack> condenseStacks(ResourceHandler<ItemResource> itemHandler);

	List<ItemStack> condenseStacks(ItemStack... stacks);

	List<ItemStack> condenseStacks(Stream<ItemStack> stacks);

	List<ItemStack> condenseStacks(Iterable<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks);

	List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize);

	void serializeItemList(List<ItemStack> list, ValueOutputList output);

	void serializeItemList(List<ItemStack> list, String indexKey, ValueOutputList output);

	void deserializeItemList(List<ItemStack> list, ValueInputList input);

	void deserializeItemList(List<ItemStack> list, String indexKey, ValueInputList input);

	IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index);

	List<ItemStack> getRemainingItems(Container container);

	List<ItemStack> getRemainingItems(Container container, int minInclusive, int maxExclusive);

	List<ItemStack> getRemainingItems(ItemStack... stacks);

	List<ItemStack> getRemainingItems(List<ItemStack> stacks);

	ItemStack getContainerItem(ItemStack stack);

	ItemStack cloneStack(ItemStack stack, int stackSize);

	boolean isPackage(ItemStack stack);

	boolean isPackage(ItemResource resource);

	boolean isEmpty(ResourceHandler<ItemResource> itemHandler);

	ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack);

	ItemStack tryMakeVolumePackage(Object volumeStack);

	void serializeRecipeList(List<IPackageRecipeInfo> recipes, ValueOutputList output);

	List<IPackageRecipeInfo> deserializeRecipeList(ValueInputList input);

	boolean recipeEquals(IPackageRecipeInfo recipeA, Object recipeInternalA, IPackageRecipeInfo recipeB, Object recipeInternalB);

	int recipeHashCode(IPackageRecipeInfo recipe, Object recipeInternal);

	boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate);

	boolean arePatternsDisjoint(List<IPackagePattern> patternList);

	int insertItem(ResourceHandler<ItemResource> itemHandler, ItemStack stack, boolean requireEmptySlot, TransactionContext transaction);

	int insertVolume(Level level, BlockPos pos, Direction direction, ItemStack stack, TransactionContext transaction);

	Runnable conditionalRunnable(BooleanSupplier conditionSupplier, Supplier<Runnable> trueRunnable, Supplier<Runnable> falseRunnable);

	<T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier);

	RecipeMap getRecipeMap();

	RegistryAccess getRegistryAccess();
}
