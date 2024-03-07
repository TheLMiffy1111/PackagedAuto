package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EmptyMenu;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class CraftingPackageRecipeInfo implements ICraftingPackageRecipeInfo {

	ResourceLocation id;
	CraftingRecipe recipe;
	List<ItemStack> input = new ArrayList<>();
	CraftingContainer matrix = new TransientCraftingContainer(new EmptyMenu(), 3, 3);
	ItemStack output;
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void load(CompoundTag nbt) {
		input.clear();
		output = ItemStack.EMPTY;
		patterns.clear();
		id = new ResourceLocation(nbt.getString("Recipe"));
		Recipe<?> recipe = MiscHelper.INSTANCE.getRecipeManager().byKey(id).map(RecipeHolder::value).orElse(null);
		List<ItemStack> matrixList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Matrix", 10), matrixList);
		for(int i = 0; i < 9 && i < matrixList.size(); ++i) {
			matrix.setItem(i, matrixList.get(i));
		}
		if(recipe instanceof CraftingRecipe craftingRecipe) {
			this.recipe = craftingRecipe;
			input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
			output = this.recipe.assemble(matrix, MiscHelper.INSTANCE.getRegistryAccess()).copy();
			for(int i = 0; i*9 < input.size(); ++i) {
				patterns.add(new PackagePattern(this, i));
			}
		}
	}

	@Override
	public void save(CompoundTag nbt) {
		if(isValid()) {
			nbt.putString("Recipe", id.toString());
		}
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 9; ++i) {
			matrixList.add(matrix.getItem(i));
		}
		ListTag matrixTag = MiscHelper.INSTANCE.saveAllItems(new ListTag(), matrixList);
		nbt.put("Matrix", matrixTag);
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return CraftingPackageRecipeType.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return id != null && recipe != null;
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
	public CraftingRecipe getRecipe() {
		return recipe;
	}

	@Override
	public CraftingContainer getMatrix() {
		return matrix;
	}

	@Override
	public List<ItemStack> getRemainingItems() {
		return recipe.getRemainingItems(matrix);
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, Level level) {
		id = null;
		recipe = null;
		this.input.clear();
		patterns.clear();
		if(level != null) {
			int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
			for(int i = 0; i < 9; ++i) {
				ItemStack toSet = input.get(slotArray[i]);
				toSet.setCount(1);
				matrix.setItem(i, toSet.copy());
			}
			RecipeHolder<CraftingRecipe> recipeHolder = MiscHelper.INSTANCE.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, matrix, level).orElse(null);
			if(recipeHolder != null) {
				this.id = recipeHolder.id();
				this.recipe = recipeHolder.value();
				this.input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
				this.output = recipe.assemble(matrix, level.registryAccess()).copy();
				for(int i = 0; i*9 < this.input.size(); ++i) {
					patterns.add(new PackagePattern(this, i));
				}
				return;
			}
		}
		matrix.clearContent();
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
		for(int i = 0; i < 9; ++i) {
			map.put(slotArray[i], matrix.getItem(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CraftingPackageRecipeInfo other) {
			return MiscHelper.INSTANCE.recipeEquals(this, recipe, other, other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, recipe);
	}
}
