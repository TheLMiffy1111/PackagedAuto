package thelm.packagedauto.recipe;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.integration.nei.NEIHandler;
import thelm.packagedauto.util.MiscHelper;

public class PackageRecipeTypeProcessing implements IPackageRecipeType {

	public static final PackageRecipeTypeProcessing INSTANCE = new PackageRecipeTypeProcessing();
	public static final Set<Integer> SLOTS;
	public static final Color COLOR = new Color(139, 139, 139);

	static {
		SLOTS = new TreeSet<>();
		IntStream.range(0, 90).forEachOrdered(SLOTS::add);
	}

	protected PackageRecipeTypeProcessing() {}

	@Override
	public String getName() {
		return "packagedauto:processing";
	}

	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal("recipe.packagedauto.processing");
	}

	@Override
	public String getLocalizedNameShort() {
		return StatCollector.translateToLocal("recipe.packagedauto.processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new PackageRecipeInfoProcessing();
	}

	@Override
	public Set<Integer> getEnabledSlots() {
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
	public List<String> getNEICategories() {
		return MiscHelper.INSTANCE.conditionalSupplier(()->PackagedAuto.proxy.neiLoaded,
				()->()->NEIHandler.INSTANCE.getAllRecipeCategories(), ()->ArrayList<String>::new).get();
	}

	@Optional.Method(modid="NotEnoughItems")
	@Override
	public Map<Integer, ItemStack> getRecipeTransferMap(IRecipeHandler recipeHandler, int recipeIndex, Set<String> categories) {
		Map<Integer, ItemStack> map = new HashMap<>();
		Pair<List<PositionedStack>, List<PositionedStack>> stacks = NEIHandler.INSTANCE.getInputOutputLists(recipeHandler, recipeIndex);
		List<PositionedStack> ingredients = stacks.getLeft();
		List<PositionedStack> results = stacks.getRight();
		int inputIndex = 0;
		int outputIndex = 81;
		for(PositionedStack ingredient : ingredients) {
			ItemStack displayed = ingredient.item;
			if(displayed != null) {
				map.put(inputIndex, displayed);
			}
			++inputIndex;
			if(inputIndex >= 81) {
				break;
			}
		}
		for(PositionedStack result : results) {
			ItemStack displayed = result.item;
			if(displayed != null) {
				map.put(outputIndex, displayed);
			}
			++outputIndex;
			if(outputIndex >= 90) {
				break;
			}
		}
		return map;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.furnace);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Color getSlotColor(int slot) {
		return COLOR;
	}
}
