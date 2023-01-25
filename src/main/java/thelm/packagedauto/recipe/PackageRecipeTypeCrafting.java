package thelm.packagedauto.recipe;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;

public class PackageRecipeTypeCrafting implements IPackageRecipeType {

	public static final PackageRecipeTypeCrafting INSTANCE = new PackageRecipeTypeCrafting();
	public static final Set<Integer> SLOTS;
	public static final Map<Pair<Integer, Integer>, Integer> NEI_SLOTS;
	public static final List<String> CATEGORIES = Collections.singletonList("crafting");
	public static final Color COLOR = new Color(139, 139, 139);
	public static final Color COLOR_DISABLED = new Color(64, 64, 64);

	static {
		SLOTS = new TreeSet<>();
		NEI_SLOTS = new TreeMap<>();
		for(int i = 3; i < 6; ++i) {
			for(int j = 3; j < 6; ++j) {
				SLOTS.add(9*i+j);
				NEI_SLOTS.put(Pair.of(25+(j-3)*18, 6+(i-3)*18), 9*i+j);
			}
		}
	}

	protected PackageRecipeTypeCrafting() {}

	@Override
	public String getName() {
		return "packagedauto:crafting";
	}

	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal("recipe.packagedauto.crafting");
	}

	@Override
	public String getLocalizedNameShort() {
		return StatCollector.translateToLocal("recipe.packagedauto.crafting.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new PackageRecipeInfoCrafting();
	}

	@Override
	public Set<Integer> getEnabledSlots() {
		return SLOTS;
	}

	@Override
	public boolean canSetOutput() {
		return false;
	}

	@Override
	public boolean hasMachine() {
		return true;
	}

	@Override
	public List<String> getNEICategories() {
		return CATEGORIES;
	}

	@Override
	public Map<Integer, ItemStack> getRecipeTransferMap(IRecipeHandler recipeHandler, int recipeIndex, Set<String> categories) {
		Map<Integer, ItemStack> map = new HashMap<>();
		List<PositionedStack> ingredients = recipeHandler.getIngredientStacks(recipeIndex);
		int[] slotArray = SLOTS.stream().mapToInt(Integer::intValue).toArray();
		for(PositionedStack ingredient : ingredients) {
			ItemStack displayed = ingredient.item;
			int slot = NEI_SLOTS.getOrDefault(Pair.of(ingredient.relx, ingredient.rely), -1);
			if(displayed != null && slot != -1) {
				map.put(slot, displayed);
			}
		}
		return map;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.crafting_table);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Color getSlotColor(int slot) {
		if(!SLOTS.contains(slot) && slot != 85) {
			return COLOR_DISABLED;
		}
		return COLOR;
	}
}
