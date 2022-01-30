package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.api.IGuiIngredientWrapper;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IRecipeLayoutWrapper;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.FluidPackageItem;
import thelm.packagedauto.util.MiscHelper;

public class ProcessingPackageRecipeType implements IPackageRecipeType {

	public static final ProcessingPackageRecipeType INSTANCE = new ProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:processing");
	public static final IntSet SLOTS;
	public static final Vec3i COLOR = new Vec3i(139, 139, 139);
	public static final Vec3i COLOR_HIGHLIGHT =new Vec3i(139, 139, 179);

	static {
		SLOTS = new IntRBTreeSet();
		IntStream.range(0, 90).forEachOrdered(SLOTS::add);
	}

	protected ProcessingPackageRecipeType() {};

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new ProcessingPackageRecipeInfo();
	}

	@Override
	public IntSet getEnabledSlots() {
		return SLOTS;
	}

	@Override
	public boolean canSetOutput() {
		return true;
	}

	@Override
	public boolean hasMachine() {
		return false;
	}

	@Override
	public boolean hasContainerItem() {
		return false;
	}

	@Override
	public List<ResourceLocation> getJEICategories() {
		return MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("jei"),
				()->PackagedAutoJEIPlugin::getAllRecipeCategories, ()->ArrayList<ResourceLocation>::new).get();
	}

	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayoutWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		Map<Integer, IGuiIngredientWrapper<ItemStack>> ingredients = recipeLayoutWrapper.getItemStackIngredients();
		int inputIndex = 0;
		int outputIndex = 81;
		for(Map.Entry<Integer, IGuiIngredientWrapper<ItemStack>> entry : ingredients.entrySet()) {
			IGuiIngredientWrapper<ItemStack> ingredient = entry.getValue();
			if(ingredient.isInput()) {
				if(inputIndex >= 81) {
					continue;
				}
				ItemStack displayed = entry.getValue().getDisplayedIngredient();
				if(displayed != null && !displayed.isEmpty()) {
					map.put(inputIndex, displayed);
				}
				++inputIndex;
			}
			else {
				if(outputIndex >= 90) {
					continue;
				}
				ItemStack displayed = entry.getValue().getDisplayedIngredient();
				if(displayed != null && !displayed.isEmpty()) {
					map.put(outputIndex, displayed);
				}
				++outputIndex;
				outputIndex += 3;
			}
			if(inputIndex >= 81 && outputIndex >= 90) {
				break;
			}
		}
		Map<Integer, IGuiIngredientWrapper<FluidStack>> fluidIngredients = recipeLayoutWrapper.getFluidStackIngredients();
		for(Map.Entry<Integer, IGuiIngredientWrapper<FluidStack>> entry : fluidIngredients.entrySet()) {
			IGuiIngredientWrapper<FluidStack> ingredient = entry.getValue();
			if(ingredient.isInput()) {
				if(inputIndex >= 81) {
					continue;
				}
				ItemStack displayed = FluidPackageItem.makeFluidPackage(entry.getValue().getDisplayedIngredient());
				if(displayed != null && !displayed.isEmpty()) {
					map.put(inputIndex, displayed);
				}
				++inputIndex;
			}
			else {
				if(outputIndex >= 90) {
					continue;
				}
				ItemStack displayed = FluidPackageItem.makeFluidPackage(entry.getValue().getDisplayedIngredient());
				if(displayed != null && !displayed.isEmpty()) {
					map.put(outputIndex, displayed);
				}
				++outputIndex;
				outputIndex += 3;
			}
			if(inputIndex >= 81 && outputIndex >= 90) {
				break;
			}
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.FURNACE);
	}

	@Override
	public Vec3i getSlotColor(int slot) {
		return slot == 81 ? COLOR_HIGHLIGHT : COLOR;
	}
}
