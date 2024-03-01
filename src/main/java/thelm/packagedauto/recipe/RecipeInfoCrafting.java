package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.container.ContainerEmpty;
import thelm.packagedauto.util.PatternHelper;

public class RecipeInfoCrafting implements IRecipeInfoCrafting {

	IRecipe recipe;
	List<ItemStack> input = new ArrayList<>();
	InventoryCrafting matrix = new InventoryCrafting(new ContainerEmpty(), 3, 3);
	ItemStack output;
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		input.clear();
		output = ItemStack.EMPTY;
		patterns.clear();
		recipe = CraftingManager.getRecipe(new ResourceLocation(nbt.getString("Recipe")));
		List<ItemStack> matrixList = new ArrayList<>();
		MiscUtil.loadAllItems(nbt.getTagList("Matrix", 10), matrixList);
		for(int i = 0; i < 9 && i < matrixList.size(); ++i) {
			matrix.setInventorySlotContents(i, matrixList.get(i));
		}
		if(recipe != null) {
			input.addAll(MiscUtil.condenseStacks(matrix));
			output = recipe.getCraftingResult(matrix).copy();
			for(int i = 0; i*9 < input.size(); ++i) {
				patterns.add(new PatternHelper(this, i));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if(recipe != null) {
			nbt.setString("Recipe", recipe.getRegistryName().toString());
		}
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 9; ++i) {
			matrixList.add(matrix.getStackInSlot(i));
		}
		NBTTagList matrixTag = MiscUtil.saveAllItems(new NBTTagList(), matrixList);
		nbt.setTag("Matrix", matrixTag);
		return nbt;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeTypeCrafting.INSTANCE;
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
	public List<ItemStack> getRemainingItems() {
		return recipe.getRemainingItems(matrix);
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		recipe = null;
		this.input.clear();
		patterns.clear();
		if(world != null) {
			int[] slotArray = RecipeTypeCrafting.SLOTS.toIntArray();
			for(int i = 0; i < 9; ++i) {
				ItemStack toSet = input.get(slotArray[i]);
				toSet.setCount(1);
				matrix.setInventorySlotContents(i, toSet.copy());
			}
			IRecipe recipe = CraftingManager.findMatchingRecipe(matrix, world);
			if(recipe != null) {
				this.recipe = recipe;
				this.input.addAll(MiscUtil.condenseStacks(matrix));
				this.output = recipe.getCraftingResult(matrix).copy();
				for(int i = 0; i*9 < this.input.size(); ++i) {
					patterns.add(new PatternHelper(this, i));
				}
				return;
			}
		}
		matrix.clear();
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int[] slotArray = RecipeTypeCrafting.SLOTS.toIntArray();
		for(int i = 0; i < 9; ++i) {
			map.put(slotArray[i], matrix.getStackInSlot(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeInfoCrafting) {
			RecipeInfoCrafting other = (RecipeInfoCrafting)obj;
			return MiscUtil.recipeEquals(this, recipe, other, other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscUtil.recipeHashCode(this, recipe);
	}
}
