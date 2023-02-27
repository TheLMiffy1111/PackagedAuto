package thelm.packagedauto.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.PackagedAutoApi;

public class MiscHelper implements IMiscHelper {

	public static final MiscHelper INSTANCE = new MiscHelper();

	private static final Cache<CompoundNBT, IPackageRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private static MinecraftServer server;

	private MiscHelper() {}

	@Override
	public List<ItemStack> condenseStacks(IInventory inventory) {
		List<ItemStack> stacks = new ArrayList<>(inventory.getContainerSize());
		for(int i = 0; i < inventory.getContainerSize(); ++i) {
			stacks.add(inventory.getItem(i));
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
		return condenseStacks(Arrays.asList(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(Stream<ItemStack> stacks) {
		return condenseStacks(stacks.collect(Collectors.toList()));
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
		Object2IntRBTreeMap<Pair<Item, CompoundNBT>> map = new Object2IntRBTreeMap<>(
				Comparator.comparing(pair->Pair.of(pair.getLeft().getRegistryName(), ""+pair.getRight())));
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Pair<Item, CompoundNBT> pair = Pair.of(stack.getItem(), stack.getTag());
			if(!map.containsKey(pair)) {
				map.put(pair, 0);
			}
			map.addTo(pair, stack.getCount());
		}
		List<ItemStack> list = new ArrayList<>();
		for(Object2IntMap.Entry<Pair<Item, CompoundNBT>> entry : map.object2IntEntrySet()) {
			Pair<Item, CompoundNBT> pair = entry.getKey();
			int count = entry.getIntValue();
			Item item = pair.getLeft();
			CompoundNBT nbt = pair.getRight();
			if(ignoreStackSize) {
				list.add(new ItemStack(item, count));
			}
			else {
				while(count > 0) {
					ItemStack toAdd = new ItemStack(item, 1);
					toAdd.setTag(nbt);
					int limit = item.getItemStackLimit(toAdd);
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
	public ListNBT saveAllItems(ListNBT tagList, List<ItemStack> list) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack.isEmpty();
			if(!empty || i == list.size()-1) {
				if(empty) {
					// Ensure that the end-of-list stack if empty is always the default empty stack
					stack = new ItemStack((Item)null);
				}
				CompoundNBT nbt = new CompoundNBT();
				nbt.putByte("Index", (byte)i);
				stack.save(nbt);
				tagList.add(nbt);
			}
		}
		return tagList;
	}

	@Override
	public void loadAllItems(ListNBT tagList, List<ItemStack> list) {
		list.clear();
		for(int i = 0; i < tagList.size(); ++i) {
			CompoundNBT nbt = tagList.getCompound(i);
			int j = nbt.getByte("Index") & 255;
			while(j >= list.size()) {
				list.add(ItemStack.EMPTY);
			}
			if(j >= 0)  {
				ItemStack stack = ItemStack.of(nbt);
				list.set(j, stack.isEmpty() ? ItemStack.EMPTY : stack);
			}
		}
	}

	@Override
	public IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index) {
		return new PackagePattern(recipeInfo, index);
	}

	@Override
	public List<ItemStack> getRemainingItems(IInventory inventory) {
		return getRemainingItems(IntStream.range(0, inventory.getContainerSize()).mapToObj(inventory::getItem).collect(Collectors.toList()));
	}

	@Override
	public List<ItemStack> getRemainingItems(IInventory inventory, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(inventory::getItem).collect(Collectors.toList()));
	}

	@Override
	public List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(Arrays.asList(stacks));
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
		if(stack.getItem().hasContainerItem(stack)) {
			stack = stack.getItem().getContainerItem(stack);
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
	public boolean isEmpty(IItemHandler itemHandler) {
		if(itemHandler == null) {
			return false;
		}
		if(itemHandler.getSlots() == 0) {
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
	public CompoundNBT writeRecipe(CompoundNBT nbt, IPackageRecipeInfo recipe) {
		nbt.putString("RecipeType", recipe.getRecipeType().getName().toString());
		recipe.write(nbt);
		return nbt;
	}

	@Override
	public IPackageRecipeInfo readRecipe(CompoundNBT nbt) {
		IPackageRecipeInfo recipe = RECIPE_CACHE.getIfPresent(nbt);
		if(recipe != null && recipe.isValid()) {
			return recipe;
		}
		IPackageRecipeType recipeType = PackagedAutoApi.instance().getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		if(recipeType != null) {
			recipe = recipeType.getNewRecipeInfo();
			recipe.read(nbt);
			RECIPE_CACHE.put(nbt, recipe);
			if(recipe.isValid()) {
				return recipe;
			}
		}
		return null;
	}

	//Modified from Forestry
	@Override
	public boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.getCount() <= offer.getCount() && req.getItem() == offer.getItem() &&
						(!req.hasTag() || ItemStack.tagMatches(req, offer))) {
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
					if(req.getItem() == offer.getItem() &&
							(!req.hasTag() || ItemStack.tagMatches(req, offer))) {
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
		ObjectRBTreeSet<Pair<Item, CompoundNBT>> set = new ObjectRBTreeSet<>(
				Comparator.comparing(pair->Pair.of(pair.getLeft().getRegistryName(), ""+pair.getRight())));
		for(IPackagePattern pattern : patternList) {
			List<ItemStack> condensedInputs = condenseStacks(pattern.getInputs(), true);
			for(ItemStack stack : condensedInputs) {
				Pair<Item, CompoundNBT> toAdd = Pair.of(stack.getItem(), stack.getTag());
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
	public <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->conditionSupplier.getAsBoolean() ? trueSupplier.get().get() : falseSupplier.get().get();
	}

	public void setServer(MinecraftServer server) {
		MiscHelper.server = server;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return server != null ? server.getRecipeManager() :
			DistExecutor.callWhenOn(Dist.CLIENT, ()->()->Minecraft.getInstance().level.getRecipeManager());
	}
}
