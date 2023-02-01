package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.ContainerEmpty;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class PackageRecipeInfoCrafting implements IPackageRecipeInfoCrafting {

	IRecipe recipe;
	List<ItemStack> input = new ArrayList<>();
	InventoryCrafting matrix = new InventoryCrafting(new ContainerEmpty(), 3, 3);
	ItemStack output;
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		input.clear();
		output = null;
		patterns.clear();
		recipe = (IRecipe)CraftingManager.getInstance().getRecipeList().get(nbt.getInteger("Recipe"));
		List<ItemStack> matrixList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getTagList("Matrix", 10), matrixList);
		for(int i = 0; i < 9 && i < matrixList.size(); ++i) {
			matrix.setInventorySlotContents(i, matrixList.get(i));
		}
		if(recipe != null) {
			input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
			output = recipe.getCraftingResult(matrix).copy();
			for(int i = 0; i*9 < input.size(); ++i) {
				patterns.add(new PackagePattern(this, i));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if(recipe != null) {
			nbt.setInteger("Recipe", CraftingManager.getInstance().getRecipeList().indexOf(recipe));
		}
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 9; ++i) {
			matrixList.add(matrix.getStackInSlot(i));
		}
		NBTTagList matrixTag = MiscHelper.INSTANCE.saveAllItems(new NBTTagList(), matrixList);
		nbt.setTag("Matrix", matrixTag);
		return nbt;
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return PackageRecipeTypeCrafting.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return recipe != null;
	}

	@Override
	public List<IPackagePattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.unmodifiableList(input);
	}

	@Override
	public ItemStack getOutput() {
		return output.copy();
	}

	@Override
	public IRecipe getRecipe() {
		return recipe;
	}

	@Override
	public InventoryCrafting getMatrix() {
		return matrix;
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		recipe = null;
		this.input.clear();
		patterns.clear();
		if(world != null) {
			int[] slotArray = PackageRecipeTypeCrafting.SLOTS.stream().mapToInt(Integer::intValue).toArray();
			for(int i = 0; i < 9; ++i) {
				ItemStack toSet = input.get(slotArray[i]);
				if(toSet != null) {
					toSet.stackSize = 1;
					matrix.setInventorySlotContents(i, toSet.copy());
				}
			}
			IRecipe recipe = ((List<IRecipe>)CraftingManager.getInstance().getRecipeList()).stream().
					filter(rec->rec.matches(matrix, world)).findFirst().orElse(null);
			if(recipe != null) {
				this.recipe = recipe;
				this.input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
				this.output = recipe.getCraftingResult(matrix).copy();
				for(int i = 0; i*9 < this.input.size(); ++i) {
					patterns.add(new PackagePattern(this, i));
				}
				return;
			}
		}
		for(int i = 0; i < 9; ++i) {
			matrix.setInventorySlotContents(i, null);
		}
	}

	@Override
	public Map<Integer, ItemStack> getEncoderStacks() {
		Map<Integer, ItemStack> map = new HashMap<>();
		int[] slotArray = PackageRecipeTypeCrafting.SLOTS.stream().mapToInt(Integer::intValue).toArray();
		for(int i = 0; i < 9; ++i) {
			map.put(slotArray[i], matrix.getStackInSlot(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PackageRecipeInfoCrafting) {
			PackageRecipeInfoCrafting other = (PackageRecipeInfoCrafting)obj;
			for(int i = 0; i < input.size(); ++i) {
				if(!ItemStack.areItemStacksEqual(input.get(i), other.input.get(i))) {
					return false;
				}
			}
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		Object[] toHash = new Object[2];
		Object[] inputArray = new Object[input.size()];
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.get(i);
			inputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.stackSize, stack.getTagCompound()};
		}
		toHash[0] = recipe;
		toHash[1] = inputArray;
		return Arrays.deepHashCode(toHash);
	}
}
