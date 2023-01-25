package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
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
import com.google.gson.internal.LinkedTreeMap;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;

public class MiscHelper implements IMiscHelper {

	public static final MiscHelper INSTANCE = new MiscHelper();

	private static final Cache<NBTTagCompound, IPackageRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private MiscHelper() {}

	@Override
	public List<ItemStack> condenseStacks(IInventory inventory) {
		List<ItemStack> stacks = new ArrayList<>(inventory.getSizeInventory());
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			stacks.add(inventory.getStackInSlot(i));
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
		Map<Triple<Item, Integer, NBTTagCompound>, Integer> map = new LinkedTreeMap<Triple<Item, Integer, NBTTagCompound>, Integer>(
				Comparator.comparing(triple->Triple.of(GameRegistry.findUniqueIdentifierFor(triple.getLeft()).toString(), triple.getMiddle(), ""+triple.getRight())));
		for(ItemStack stack : stacks) {
			if(stack == null) {
				continue;
			}
			Triple<Item, Integer, NBTTagCompound> triple = Triple.of(stack.getItem(), stack.getItemDamage(), stack.getTagCompound());
			if(!map.containsKey(triple)) {
				map.put(triple, 0);
			}
			map.merge(triple, stack.stackSize, Integer::sum);
		}
		List<ItemStack> list = new ArrayList<>();
		for(Map.Entry<Triple<Item, Integer, NBTTagCompound>, Integer> entry : map.entrySet()) {
			Triple<Item, Integer, NBTTagCompound> triple = entry.getKey();
			int count = entry.getValue();
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
					toAdd.stackSize = Math.min(count, limit);
					list.add(toAdd);
					count -= limit;
				}
			}
		}
		map.clear();
		return list;
	}

	@Override
	public NBTTagList saveAllItems(NBTTagList tagList, List<ItemStack> list) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack == null;
			if(!empty || i == list.size()-1) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("Index", (byte)i);
				if(!empty) {
					stack.writeToNBT(nbt);
				}
				tagList.appendTag(nbt);
			}
		}
		return tagList;
	}

	@Override
	public void loadAllItems(NBTTagList tagList, List<ItemStack> list) {
		list.clear();
		for(int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbt = tagList.getCompoundTagAt(i);
			int j = nbt.getByte("Index") & 255;
			while(j >= list.size()) {
				list.add(null);
			}
			if(j >= 0)  {
				ItemStack stack = ItemStack.loadItemStackFromNBT(nbt);
				list.set(j, stack);
			}
		}
	}

	@Override
	public IPackagePattern getPatternHelper(IPackageRecipeInfo recipeInfo, int index) {
		return new PackagePattern(recipeInfo, index);
	}

	@Override
	public List<ItemStack> getRemainingItems(IInventory inventory) {
		return getRemainingItems(IntStream.range(0, inventory.getSizeInventory()).mapToObj(inventory::getStackInSlot).collect(Collectors.toList()));
	}

	@Override
	public List<ItemStack> getRemainingItems(IInventory inventory, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(inventory::getStackInSlot).collect(Collectors.toList()));
	}

	@Override
	public List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(Arrays.asList(stacks));
	}

	@Override
	public List<ItemStack> getRemainingItems(List<ItemStack> stacks) {
		List<ItemStack> ret = Arrays.asList(new ItemStack[stacks.size()]);
		for(int i = 0; i < ret.size(); i++) {
			ret.set(i, getContainerItem(stacks.get(i)));
		}
		return ret;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if(stack == null) {
			return null;
		}
		if(stack.getItem().hasContainerItem(stack)) {
			stack = stack.getItem().getContainerItem(stack);
			if(stack != null && stack.isItemStackDamageable() && stack.getItemDamage() > stack.getMaxDamage()) {
				return null;
			}
			return stack;
		}
		else {
			if(stack.stackSize > 1) {
				stack = stack.copy();
				stack.stackSize -= 1;
				return stack;
			}
			return null;
		}
	}

	@Override
	public ItemStack cloneStack(ItemStack stack, int stackSize) {
		if(stack == null || stackSize == 0) {
			return null;
		}
		ItemStack retStack = stack.copy();
		retStack.stackSize = stackSize;
		return retStack;
	}

	@Override
	public boolean isEmpty(IInventory inventory, ForgeDirection side) {
		for(int i : getSlots(inventory, side)) {
			if(inventory.getStackInSlot(i) != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public NBTTagCompound writeRecipeToNBT(NBTTagCompound nbt, IPackageRecipeInfo recipe) {
		nbt.setString("RecipeType", recipe.getRecipeType().getName().toString());
		recipe.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public IPackageRecipeInfo readRecipeFromNBT(NBTTagCompound nbt) {
		IPackageRecipeInfo recipe = RECIPE_CACHE.getIfPresent(nbt);
		if(recipe != null && recipe.isValid()) {
			return recipe;
		}
		IPackageRecipeType recipeType = ApiImpl.INSTANCE.getRecipeType(nbt.getString("RecipeType"));
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
	@Override
	public boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.stackSize <= offer.stackSize && req.getItem() == offer.getItem() &&
						offer.getItemDamage() == req.getItemDamage() &&
						(!req.hasTagCompound() || ItemStack.areItemStackTagsEqual(req, offer))) {
					continue f;
				}
			}
			return false;
		}
		if(simulate) {
			return true;
		}
		for(ItemStack req : condensedRequired) {
			int count = req.stackSize;
			for(ItemStack offer : offered) {
				if(offer != null) {
					if(req.getItem() == offer.getItem() && offer.getItemDamage() == req.getItemDamage() &&
							(!req.hasTagCompound() || ItemStack.areItemStackTagsEqual(req, offer))) {
						int toRemove = Math.min(count, offer.stackSize);
						offer.stackSize -= toRemove;
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
		TreeSet<Triple<Item, Integer, NBTTagCompound>> set = new TreeSet<>(
				Comparator.comparing(triple->Triple.of(GameRegistry.findUniqueIdentifierFor(triple.getLeft()).toString(), triple.getMiddle(), ""+triple.getRight())));
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

	@Override
	public <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->conditionSupplier.getAsBoolean() ? trueSupplier.get().get() : falseSupplier.get().get();
	}

	@Override
	public int[] getSlots(IInventory inv, ForgeDirection side) {
		if(inv instanceof ISidedInventory) {
			return ((ISidedInventory)inv).getAccessibleSlotsFromSide(side.ordinal());
		}
		return IntStream.range(0, inv.getSizeInventory()).toArray();
	}

	// From Forge InvWrapper
	@Override
	public ItemStack insertItem(IInventory inv, int slot, ForgeDirection side, ItemStack stack, boolean simulate) {
		if(inv instanceof ISidedInventory) {
			return insertItem((ISidedInventory)inv, slot, side, stack, simulate);
		}
		if(stack == null) {
			return null;
		}
		ItemStack stackInSlot = inv.getStackInSlot(slot);
		int m;
		if(stackInSlot != null) {
			if(stackInSlot.stackSize >= Math.min(stackInSlot.getMaxStackSize(), inv.getInventoryStackLimit())) {
				return stack;
			}
			if(!stack.getItem().equals(stackInSlot.getItem()) || stack.getItemDamage() != stackInSlot.getItemDamage() || !ItemStack.areItemStackTagsEqual(stack, stackInSlot)) {
				return stack;
			}
			if(!inv.isItemValidForSlot(slot, stack)) {
				return stack;
			}
			m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit())-stackInSlot.stackSize;
			if(stack.stackSize <= m) {
				if(!simulate) {
					ItemStack copy = stack.copy();
					copy.stackSize += stackInSlot.stackSize;
					inv.setInventorySlotContents(slot, copy);
					inv.markDirty();
				}
				return null;
			}
			else {
				stack = stack.copy();
				if(!simulate) {
					ItemStack copy = stack.splitStack(m);
					copy.stackSize += stackInSlot.stackSize;
					inv.setInventorySlotContents(slot, copy);
					inv.markDirty();
					return stack;
				}
				else {
					stack.stackSize -= m;
					return stack;
				}
			}
		}
		else {
			if(!inv.isItemValidForSlot(slot, stack)) {
				return stack;
			}
			m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
			if(m < stack.stackSize) {
				stack = stack.copy();
				if(!simulate) {
					inv.setInventorySlotContents(slot, stack.splitStack(m));
					inv.markDirty();
					return stack;
				}
				else {
					stack.stackSize -= m;
					return stack;
				}
			}
			else {
				if(!simulate) {
					inv.setInventorySlotContents(slot, stack);
					inv.markDirty();
				}
				return null;
			}
		}
	}

	// From Forge SidedInvWrapper
	public ItemStack insertItem(ISidedInventory inv, int slot, ForgeDirection side, ItemStack stack, boolean simulate) {
		if(stack == null) {
			return null;
		}
		ItemStack stackInSlot = inv.getStackInSlot(slot);
		int m;
		if(stackInSlot != null) {
			if(stackInSlot.stackSize >= Math.min(stackInSlot.getMaxStackSize(), inv.getInventoryStackLimit())) {
				return stack;
			}
			if(!stack.getItem().equals(stackInSlot.getItem()) || stack.getItemDamage() != stackInSlot.getItemDamage() || !ItemStack.areItemStackTagsEqual(stack, stackInSlot)) {
				return stack;
			}
			if(!inv.canInsertItem(slot, stack, side.ordinal()) || !inv.isItemValidForSlot(slot, stack)) {
				return stack;
			}
			m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit())-stackInSlot.stackSize;
			if(stack.stackSize <= m) {
				if(!simulate) {
					ItemStack copy = stack.copy();
					copy.stackSize += stackInSlot.stackSize;
					inv.setInventorySlotContents(slot, copy);
					inv.markDirty();
				}
				return null;
			}
			else {
				stack = stack.copy();
				if(!simulate) {
					ItemStack copy = stack.splitStack(m);
					copy.stackSize += stackInSlot.stackSize;
					inv.setInventorySlotContents(slot, copy);
					inv.markDirty();
					return stack;
				}
				else {
					stack.stackSize -= m;
					return stack;
				}
			}
		}
		else {
			if(!inv.canInsertItem(slot, stack, side.ordinal()) || !inv.isItemValidForSlot(slot, stack)) {
				return stack;
			}
			m = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
			if(m < stack.stackSize) {
				stack = stack.copy();
				if(!simulate) {
					inv.setInventorySlotContents(slot, stack.splitStack(m));
					inv.markDirty();
					return stack;
				}
				else {
					stack.stackSize -= m;
					return stack;
				}
			}
			else {
				if(!simulate) {
					inv.setInventorySlotContents(slot, stack);
					inv.markDirty();
				}
				return null;
			}
		}
	}

	// From Forge InvWrapper
	@Override
	public ItemStack extractItem(IInventory inv, int slot, ForgeDirection side, int amount, boolean simulate) {
		if(inv instanceof ISidedInventory) {
			return extractItem((ISidedInventory)inv, slot, side, amount, simulate);
		}
		if(amount == 0) {
			return null;
		}
		ItemStack stackInSlot = inv.getStackInSlot(slot);
		if(stackInSlot == null) {
			return null;
		}
		if(simulate) {
			if(stackInSlot.stackSize < amount) {
				return stackInSlot.copy();
			}
			else {
				ItemStack copy = stackInSlot.copy();
				copy.stackSize = amount;
				return copy;
			}
		}
		else {
			int m = Math.min(stackInSlot.stackSize, amount);
			ItemStack ret = inv.decrStackSize(slot, m);
			inv.markDirty();
			return ret;
		}
	}

	// From Forge SidedInvWrapper
	public ItemStack extractItem(ISidedInventory inv, int slot, ForgeDirection side, int amount, boolean simulate) {
		if(amount == 0) {
			return null;
		}
		ItemStack stackInSlot = inv.getStackInSlot(slot);
		if(stackInSlot == null || !inv.canExtractItem(slot, stackInSlot, side.ordinal())) {
			return null;
		}
		if(simulate) {
			if(stackInSlot.stackSize < amount) {
				return stackInSlot.copy();
			}
			else {
				ItemStack copy = stackInSlot.copy();
				copy.stackSize = amount;
				return copy;
			}
		}
		else {
			int m = Math.min(stackInSlot.stackSize, amount);
			ItemStack ret = inv.decrStackSize(slot, m);
			inv.markDirty();
			return ret;
		}
	}

	// From Forge RecipeMatcher
	@Override
	public <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests) {
		int elements = inputs.size();
		if(elements != tests.size()) {
			return null;
		}
		int[] ret = new int[elements];
		for(int x = 0; x < elements; x++) {
			ret[x] = -1;
		}
		BitSet data = new BitSet((elements+2)*elements);
		for(int x = 0; x < elements; x++) {
			int matched = 0;
			int offset = (x + 2) * elements;
			Predicate<T> test = tests.get(x);
			for(int y = 0; y < elements; y++) {
				if(data.get(y)) {
					continue;
				}
				if(test.test(inputs.get(y))) {
					data.set(offset + y);
					matched++;
				}
			}
			if(matched == 0) {
				return null;
			}
			if(matched == 1) {
				if(!claim(ret, data, x, elements)) {
					return null;
				}
			}
		}
		if(data.nextClearBit(0) >= elements) {
			return ret;
		}
		if(backtrack(data, ret, 0, elements)) {
			return ret;
		}
		return null;
	}

	private boolean claim(int[] ret, BitSet data, int claimed, int elements) {
		Queue<Integer> pending = new LinkedList<Integer>();
		pending.add(claimed);
		while(pending.peek() != null) {
			int test = pending.poll();
			int offset = (test + 2) * elements;
			int used = data.nextSetBit(offset) - offset;
			if(used >= elements || used < 0) {
				return false;
			}
			data.set(used);
			data.set(elements + test);
			ret[used] = test;
			for(int x = 0; x < elements; ++x) {
				offset = (x+2)*elements;
				if(data.get(offset+used) && !data.get(elements+x)) {
					data.clear(offset+used);
					int count = 0;
					for (int y = offset; y < offset+elements; ++y) {
						if(data.get(y)) {
							count++;
						}
					}
					if(count == 0) {
						return false;
					}
					if(count == 1) {
						pending.add(x);
					}
				}
			}
		}
		return true;
	}

	private boolean backtrack(BitSet data, int[] ret, int start, int elements) {
		int test = data.nextClearBit(elements + start) - elements;
		if(test >= elements) {
			return true;
		}
		if(test < 0) {
			return false;
		}
		int offset = (test + 2) * elements;
		for(int x = 0; x < elements; ++x) {
			if(!data.get(offset + x) || data.get(x)) {
				continue;
			}
			data.set(x);
			if(backtrack(data, ret, test + 1, elements)) {
				ret[x] = test;
				return true;
			}
			data.clear(x);
		}
		return false;
	}
}
