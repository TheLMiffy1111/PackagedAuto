package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.DataComponentUtil;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.VolumePackageItem;

public class MiscHelper implements IMiscHelper {

	public static final MiscHelper INSTANCE = new MiscHelper();

	private static final Cache<CompoundTag, IPackageRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private static MinecraftServer server;

	private MiscHelper() {}

	@Override
	public List<ItemStack> condenseStacks(Container container) {
		List<ItemStack> stacks = new ArrayList<>(container.getContainerSize());
		for(int i = 0; i < container.getContainerSize(); ++i) {
			stacks.add(container.getItem(i));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(IItemHandler itemHandler) {
		List<ItemStack> stacks = new ArrayList<>(itemHandler.getSlots());
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			stacks.add(itemHandler.getStackInSlot(i));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(ItemStack... stacks) {
		return condenseStacks(List.of(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(Stream<ItemStack> stacks) {
		return condenseStacks(stacks.toList());
	}

	@Override
	public List<ItemStack> condenseStacks(Iterable<ItemStack> stacks) {
		return condenseStacks(stacks instanceof List<?> ? (List<ItemStack>)stacks : Lists.newArrayList(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		return condenseStacks(stacks, false);
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize) {
		Object2IntLinkedOpenCustomHashMap<Pair<Item, DataComponentPatch>> map = new Object2IntLinkedOpenCustomHashMap<>(new Hash.Strategy<>() {
			@Override
			public int hashCode(Pair<Item, DataComponentPatch> o) {
				return Objects.hash(Item.getId(o.getLeft()), o.getRight());
			}
			@Override
			public boolean equals(Pair<Item, DataComponentPatch> a, Pair<Item, DataComponentPatch> b) {
				return a.equals(b);
			}
		});
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Pair<Item, DataComponentPatch> pair = Pair.of(stack.getItem(), stack.getComponentsPatch());
			if(!map.containsKey(pair)) {
				map.put(pair, 0);
			}
			map.addTo(pair, stack.getCount());
		}
		List<ItemStack> list = new ArrayList<>();
		for(Object2IntMap.Entry<Pair<Item, DataComponentPatch>> entry : map.object2IntEntrySet()) {
			Pair<Item, DataComponentPatch> pair = entry.getKey();
			int count = entry.getIntValue();
			Item item = pair.getLeft();
			DataComponentPatch patch = pair.getRight();
			if(ignoreStackSize) {
				ItemStack toAdd = new ItemStack(item, count);
				toAdd.applyComponents(patch);
				list.add(toAdd);
			}
			else {
				while(count > 0) {
					ItemStack toAdd = new ItemStack(item, 1);
					toAdd.applyComponents(patch);
					int limit = item.getMaxStackSize(toAdd);
					toAdd.setCount(Math.min(count, limit));
					list.add(toAdd);
					count -= limit;
				}
			}
		}
		map.clear();
		return list;
	}

	@Override
	public ListTag saveAllItems(ListTag tagList, List<ItemStack> list, HolderLookup.Provider registries) {
		return saveAllItems(tagList, list, "Index", registries);
	}

	@Override
	public ListTag saveAllItems(ListTag tagList, List<ItemStack> list, String indexKey, HolderLookup.Provider registries) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack.isEmpty();
			if(!empty || i == list.size()-1) {
				if(empty) {
					stack = ItemStack.EMPTY;
				}
				CompoundTag nbt = new CompoundTag();
				nbt.putByte(indexKey, (byte)i);
				saveItemWithLargeCount(nbt, stack, registries);
				tagList.add(nbt);
			}
		}
		return tagList;
	}

	@Override
	public void loadAllItems(ListTag tagList, List<ItemStack> list, HolderLookup.Provider registries) {
		loadAllItems(tagList, list, "Index", registries);
	}

	@Override
	public void loadAllItems(ListTag tagList, List<ItemStack> list, String indexKey, HolderLookup.Provider registries) {
		list.clear();
		try {
			for(int i = 0; i < tagList.size(); ++i) {
				CompoundTag nbt = tagList.getCompound(i);
				int j = nbt.getByte(indexKey) & 255;
				while(j >= list.size()) {
					list.add(ItemStack.EMPTY);
				}
				if(j >= 0)  {
					ItemStack stack = loadItemWithLargeCount(nbt, registries);
					list.set(j, stack.isEmpty() ? ItemStack.EMPTY : stack);
				}
			}
		}
		catch(UnsupportedOperationException | IndexOutOfBoundsException e) {}
	}

	public static final Codec<ItemStack> LARGE_ITEM_CODEC = RecordCodecBuilder.create(instance->instance.group(
			ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item").forGetter(ItemStack::getItemHolder),
			ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)).
			apply(instance, ItemStack::new));

	@Override
	public CompoundTag saveItemWithLargeCount(CompoundTag nbt, ItemStack stack, HolderLookup.Provider registries) {
		if(!stack.isEmpty()) {
			nbt.merge((CompoundTag)DataComponentUtil.wrapEncodingExceptions(stack, LARGE_ITEM_CODEC, registries));
		}
		return nbt;
	}

	@Override
	public ItemStack loadItemWithLargeCount(CompoundTag nbt, HolderLookup.Provider registries) {
		return LARGE_ITEM_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(ItemStack.EMPTY);
	}

	@Override
	public IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index) {
		return new PackagePattern(recipeInfo, index);
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container) {
		return getRemainingItems(IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(List.of(stacks));
	}

	@Override
	public List<ItemStack> getRemainingItems(List<ItemStack> stacks) {
		NonNullList<ItemStack> ret = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
		for(int i = 0; i < ret.size(); i++) {
			ret.set(i, getContainerItem(stacks.get(i)));
		}
		return ret;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(stack.getItem().hasCraftingRemainingItem(stack)) {
			stack = stack.getItem().getCraftingRemainingItem(stack);
			if(!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > stack.getMaxDamage()) {
				return ItemStack.EMPTY;
			}
			return stack;
		}
		else {
			if(stack.getCount() > 1) {
				stack = stack.copy();
				stack.setCount(stack.getCount() - 1);
				return stack;
			}
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack cloneStack(ItemStack stack, int stackSize) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack retStack = stack.copy();
		retStack.setCount(stackSize);
		return retStack;
	}

	@Override
	public boolean isPackage(ItemStack stack) {
		return stack.has(PackagedAutoDataComponents.RECIPE) && stack.has(PackagedAutoDataComponents.PACKAGE_INDEX);
	}

	@Override
	public boolean isEmpty(IItemHandler itemHandler) {
		if(itemHandler == null || itemHandler.getSlots() == 0) {
			return false;
		}
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			if(!itemHandler.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack) {
		return VolumePackageItem.makeVolumePackage(volumeStack);
	}

	@Override
	public ItemStack tryMakeVolumePackage(Object volumeStack) {
		return VolumePackageItem.tryMakeVolumePackage(volumeStack);
	}

	@Override
	public CompoundTag saveRecipe(CompoundTag nbt, IPackageRecipeInfo recipe, HolderLookup.Provider registries) {
		return nbt.merge((CompoundTag)IPackageRecipeInfo.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), recipe).result().orElse(new CompoundTag()));
	}

	@Override
	public IPackageRecipeInfo loadRecipe(CompoundTag nbt, HolderLookup.Provider registries) {
		IPackageRecipeInfo recipe = RECIPE_CACHE.getIfPresent(nbt);
		if(recipe != null) {
			return recipe;
		}
		recipe = IPackageRecipeInfo.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(null);
		if(recipe != null) {
			RECIPE_CACHE.put(nbt, recipe);
		}
		return recipe;
	}

	@Override
	public ListTag saveRecipeList(ListTag tagList, List<IPackageRecipeInfo> recipes, HolderLookup.Provider registries) {
		for(IPackageRecipeInfo recipe : recipes) {
			tagList.add(saveRecipe(new CompoundTag(), recipe, registries));
		}
		return tagList;
	}

	@Override
	public List<IPackageRecipeInfo> loadRecipeList(ListTag tagList, HolderLookup.Provider registries) {
		List<IPackageRecipeInfo> recipes = new ArrayList<>(tagList.size());
		for(int i = 0; i < tagList.size(); ++i) {
			IPackageRecipeInfo recipe = loadRecipe(tagList.getCompound(i), registries);
			if(recipe != null) {
				recipes.add(recipe);
			}
		}
		return recipes;
	}

	@Override
	public boolean recipeEquals(IPackageRecipeInfo recipeA, Object recipeInternalA, IPackageRecipeInfo recipeB, Object recipeInternalB) {
		if(!Objects.equals(recipeInternalA, recipeInternalB)) {
			return false;
		}
		List<ItemStack> inputsA = recipeA.getInputs();
		List<ItemStack> inputsB = recipeB.getInputs();
		if(inputsA.size() != inputsB.size()) {
			return false;
		}
		List<ItemStack> outputsA = recipeA.getOutputs();
		List<ItemStack> outputsB = recipeB.getOutputs();
		if(outputsA.size() != outputsB.size()) {
			return false;
		}
		for(int i = 0; i < inputsA.size(); ++i) {
			if(!ItemStack.matches(inputsA.get(i), inputsB.get(i))) {
				return false;
			}
		}
		for(int i = 0; i < outputsA.size(); ++i) {
			if(!ItemStack.matches(outputsA.get(i), outputsB.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int recipeHashCode(IPackageRecipeInfo recipe, Object recipeInternal) {
		List<ItemStack> inputs = recipe.getInputs();
		List<ItemStack> outputs = recipe.getOutputs();
		Function<ItemStack, Object[]> decompose = stack->new Object[] {
				stack.getItem(), stack.getCount(), stack.getComponentsPatch(),
		};
		Object[] toHash = {
				recipeInternal, inputs.stream().map(decompose).toArray(), outputs.stream().map(decompose).toArray(),
		};
		return Arrays.deepHashCode(toHash);
	}

	//Modified from Forestry
	@Override
	public boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.getCount() <= offer.getCount() && req.is(offer.getItem()) &&
						(req.isComponentsPatchEmpty() || ItemStack.isSameItemSameComponents(req, offer))) {
					continue f;
				}
			}
			return false;
		}
		if(simulate) {
			return true;
		}
		for(ItemStack req : condensedRequired) {
			int count = req.getCount();
			for(ItemStack offer : offered) {
				if(!offer.isEmpty()) {
					if(req.is(offer.getItem()) && (req.isComponentsPatchEmpty() || ItemStack.isSameItemSameComponents(req, offer))) {
						int toRemove = Math.min(count, offer.getCount());
						offer.shrink(toRemove);
						count -= toRemove;
						if(count == 0) {
							continue;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean arePatternsDisjoint(List<IPackagePattern> patternList) {
		ObjectRBTreeSet<Pair<Item, DataComponentPatch>> set = new ObjectRBTreeSet<>(
				Comparator.comparing(pair->Pair.of(BuiltInRegistries.ITEM.getKey(pair.getLeft()), ""+pair.getRight())));
		for(IPackagePattern pattern : patternList) {
			List<ItemStack> condensedInputs = condenseStacks(pattern.getInputs(), true);
			for(ItemStack stack : condensedInputs) {
				Pair<Item, DataComponentPatch> toAdd = Pair.of(stack.getItem(), stack.getComponentsPatch());
				if(set.contains(toAdd)) {
					return false;
				}
				set.add(toAdd);
			}
		}
		set.clear();
		return true;
	}

	@Override
	public ItemStack insertItem(IItemHandler itemHandler, ItemStack stack, boolean requireEmptySlot, boolean simulate) {
		if(itemHandler == null || stack.isEmpty()) {
			return stack;
		}
		if(!requireEmptySlot) {
			return ItemHandlerHelper.insertItem(itemHandler, stack, simulate);
		}
		for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
			if(itemHandler.getStackInSlot(slot).isEmpty()) {
				stack = itemHandler.insertItem(slot, stack, simulate);
				if(stack.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack fillVolume(Level level, BlockPos pos, Direction direction, ItemStack stack, boolean simulate) {
		if(stack.isEmpty()) {
			return stack;
		}
		if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
				stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, direction)) {
			stack = stack.copy();
			IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			IVolumeType vType = vStack.getVolumeType();
			while(!stack.isEmpty()) {
				int simulateFilled = vType.fill(level, pos, direction, vStack, true);
				if(simulateFilled == vStack.getAmount()) {
					if(!simulate) {
						vType.fill(level, pos, direction, vStack, false);
					}
					stack.shrink(1);
					if(stack.isEmpty()) {
						return ItemStack.EMPTY;
					}
				}
				else {
					break;
				}
			}
		}
		return stack;
	}

	@Override
	public Runnable conditionalRunnable(BooleanSupplier conditionSupplier, Supplier<Runnable> trueRunnable, Supplier<Runnable> falseRunnable) {
		return ()->(conditionSupplier.getAsBoolean() ? trueRunnable : falseRunnable).get().run();
	}

	@Override
	public <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->(conditionSupplier.getAsBoolean() ? trueSupplier : falseSupplier).get().get();
	}

	public void setServer(MinecraftServer server) {
		MiscHelper.server = server;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return server != null ? server.getRecipeManager() :
			conditionalSupplier(FMLEnvironment.dist::isClient, ()->()->Minecraft.getInstance().level.getRecipeManager(), ()->()->null).get();
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return server != null ? server.registryAccess() :
			conditionalSupplier(FMLEnvironment.dist::isClient, ()->()->Minecraft.getInstance().level.registryAccess(), ()->()->null).get();
	}
}
