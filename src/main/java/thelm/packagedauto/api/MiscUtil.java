package thelm.packagedauto.api;

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

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

public class MiscUtil {

	private static final Cache<NBTTagCompound, IRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private MiscUtil() {}

	public static List<ItemStack> condenseStacks(IInventory inventory) {
		List<ItemStack> stacks = new ArrayList<>(inventory.getSizeInventory());
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			stacks.add(inventory.getStackInSlot(i));
		}
		return condenseStacks(stacks);
	}

	public static List<ItemStack> condenseStacks(IItemHandler itemHandler) {
		List<ItemStack> stacks = new ArrayList<>(itemHandler.getSlots());
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			stacks.add(itemHandler.getStackInSlot(i));
		}
		return condenseStacks(stacks);
	}

	public static List<ItemStack> condenseStacks(ItemStack... stacks) {
		return condenseStacks(Arrays.asList(stacks));
	}

	public static List<ItemStack> condenseStacks(Stream<ItemStack> stacks) {
		return condenseStacks(stacks.collect(Collectors.toList()));
	}

	public static List<ItemStack> condenseStacks(Iterable<ItemStack> stacks) {
		return condenseStacks(stacks instanceof List<?> ? (List<ItemStack>)stacks : Lists.newArrayList(stacks));
	}

	public static List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		return condenseStacks(stacks, false);
	}

	public static List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize) {
		Object2IntRBTreeMap<Triple<Item, Integer, NBTTagCompound>> map = new Object2IntRBTreeMap<>(
				Comparator.comparing(triple->Triple.of(triple.getLeft().getRegistryName(), triple.getMiddle(), ""+triple.getRight())));
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Triple<Item, Integer, NBTTagCompound> triple = Triple.of(stack.getItem(), stack.getMetadata(), stack.getTagCompound());
			if(!map.containsKey(triple)) {
				map.put(triple, 0);
			}
			map.addTo(triple, stack.getCount());
		}
		List<ItemStack> list = new ArrayList<>();
		for(Object2IntMap.Entry<Triple<Item, Integer, NBTTagCompound>> entry : map.object2IntEntrySet()) {
			Triple<Item, Integer, NBTTagCompound> triple = entry.getKey();
			int count = entry.getIntValue();
			Item item = triple.getLeft();
			int meta = triple.getMiddle();
			NBTTagCompound nbt = triple.getRight();
			if(ignoreStackSize) {
				list.add(new ItemStack(item, count, meta));
			}
			else {
				while(count > 0) {
					ItemStack toAdd = new ItemStack(item, 1, meta);
					toAdd.setTagCompound(nbt);
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

	public static NBTTagList saveAllItems(NBTTagList tagList, List<ItemStack> list) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack.isEmpty();
			if(!empty || i == list.size()-1) {
				if(empty) {
					// Ensure that the end-of-list stack if empty is always the default empty stack
					stack = new ItemStack((Item)null);
				}
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("Index", (byte)i);
				stack.writeToNBT(nbt);
				tagList.appendTag(nbt);
			}
		}
		return tagList;
	}

	public static void loadAllItems(NBTTagList tagList, List<ItemStack> list) {
		list.clear();
		for(int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			int j = nbt.getByte("Index") & 255;
			while(j >= list.size()) {
				list.add(ItemStack.EMPTY);
			}
			if(j >= 0)  {
				ItemStack stack = new ItemStack(nbt);
				list.set(j, stack.isEmpty() ? ItemStack.EMPTY : stack);
			}
		}
	}

	public static IPackagePattern getPatternHelper(IRecipeInfo recipeInfo, int index) {
		try {
			Class<? extends IPackagePattern> helperClass = (Class<? extends IPackagePattern>)Class.forName("thelm.packagedauto.util.PatternHelper.PatternHelper");
			Constructor<? extends IPackagePattern> helperConstructor = helperClass.getConstructor(IRecipeInfo.class, int.class);
			return helperConstructor.newInstance(recipeInfo, index);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<ItemStack> getRemainingItems(IInventory inventory) {
		return getRemainingItems(IntStream.range(0, inventory.getSizeInventory()).mapToObj(inventory::getStackInSlot).collect(Collectors.toList()));
	}

	public static List<ItemStack> getRemainingItems(IInventory inventory, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(inventory::getStackInSlot).collect(Collectors.toList()));
	}

	public static List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(Arrays.asList(stacks));
	}

	public static List<ItemStack> getRemainingItems(List<ItemStack> stacks) {
		NonNullList<ItemStack> ret = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
		for(int i = 0; i < ret.size(); i++) {
			ret.set(i, getContainerItem(stacks.get(i)));
		}
		return ret;
	}

	public static ItemStack getContainerItem(ItemStack stack) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(stack.getItem().hasContainerItem(stack)) {
			stack = stack.getItem().getContainerItem(stack);
			if(!stack.isEmpty() && stack.isItemStackDamageable() && stack.getMetadata() > stack.getMaxDamage()) {
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

	public static ItemStack cloneStack(ItemStack stack, int stackSize) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack retStack = stack.copy();
		retStack.setCount(stackSize);
		return retStack;
	}

	public static boolean isEmpty(IItemHandler itemHandler) {
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			if(!itemHandler.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static NBTTagCompound writeRecipeToNBT(NBTTagCompound nbt, IRecipeInfo recipe) {
		nbt.setString("RecipeType", recipe.getRecipeType().getName().toString());
		recipe.writeToNBT(nbt);
		return nbt;
	}

	public static IRecipeInfo readRecipeFromNBT(NBTTagCompound nbt) {
		IRecipeInfo recipe = RECIPE_CACHE.getIfPresent(nbt);
		if(recipe != null && recipe.isValid()) {
			return recipe;
		}
		IRecipeType recipeType = RecipeTypeRegistry.getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		if(recipeType != null) {
			recipe = recipeType.getNewRecipeInfo();
			recipe.readFromNBT(nbt);
			RECIPE_CACHE.put(nbt, recipe);
			if(recipe.isValid()) {
				return recipe;
			}
		}
		return null;
	}

	//Modified from Forestry
	public static boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.getCount() <= offer.getCount() && req.getItem() == offer.getItem() &&
						offer.getItemDamage() == req.getItemDamage() &&
						(!req.hasTagCompound() || ItemStack.areItemStackShareTagsEqual(req, offer))) {
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
				if(!req.isEmpty()) {
					if(req.getItem() == offer.getItem() && offer.getItemDamage() == req.getItemDamage() &&
							(!req.hasTagCompound() || ItemStack.areItemStackShareTagsEqual(req, offer))) {
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

	public static boolean arePatternsDisjoint(List<IPackagePattern> patternList) {
		ObjectRBTreeSet<Triple<Item, Integer, NBTTagCompound>> set = new ObjectRBTreeSet<>(
				Comparator.comparing(triple->Triple.of(triple.getLeft().getRegistryName(), triple.getMiddle(), ""+triple.getRight())));
		for(IPackagePattern pattern : patternList) {
			List<ItemStack> condensedInputs = condenseStacks(pattern.getInputs(), true);
			for(ItemStack stack : condensedInputs) {
				Triple<Item, Integer, NBTTagCompound> toAdd = Triple.of(stack.getItem(), stack.getItemDamage(), stack.getTagCompound());
				if(set.contains(toAdd)) {
					return false;
				}
				set.add(toAdd);
			}
		}
		set.clear();
		return true;
	}

	public static <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->conditionSupplier.getAsBoolean() ? trueSupplier.get().get() : falseSupplier.get().get();
	}
}
