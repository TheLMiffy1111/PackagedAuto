package thelm.packagedauto.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Triple;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class MiscUtil {

	private MiscUtil() {}

	public static List<ItemStack> condenseStacks(ItemStack... stacks) {
		return condenseStacks(Arrays.asList(stacks));
	}

	public static List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		Object2IntRBTreeMap<Triple<Item, Integer, NBTTagCompound>> map = new Object2IntRBTreeMap<>((triple1, triple2)->
		Triple.of(triple1.getLeft().getRegistryName(), triple1.getMiddle(), ""+triple1.getRight()).compareTo(
				Triple.of(triple2.getLeft().getRegistryName(), triple2.getMiddle(), ""+triple2.getRight())));
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Triple<Item, Integer, NBTTagCompound> triple = Triple.of(stack.getItem(), stack.getItemDamage(), stack.getTagCompound());
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
			while(count > 0) {
				ItemStack toAdd = new ItemStack(item, 1, meta);
				toAdd.setTagCompound(nbt);
				int limit = item.getItemStackLimit(toAdd);
				toAdd.setCount(Math.min(count, limit));
				list.add(toAdd);
				count -= limit;
			}
		}
		return list;
	}

	public static NBTTagList saveAllItems(NBTTagList tagList, List<ItemStack> list) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setByte("Index", (byte)i);
			stack.writeToNBT(nbt);
			tagList.appendTag(nbt);
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
}
