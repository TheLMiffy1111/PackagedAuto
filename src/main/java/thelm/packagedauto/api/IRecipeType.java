package thelm.packagedauto.api;

import java.awt.Color;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRecipeType {

	ResourceLocation getName();

	String getLocalizedName();

	String getLocalizedNameShort();

	IRecipeInfo getNewRecipeInfo();

	IntSet getEnabledSlots();

	boolean canSetOutput();

	boolean hasMachine();

	@SideOnly(Side.CLIENT)
	Object getRepresentation();

	@SideOnly(Side.CLIENT)
	Color getSlotColor(int slot);
}
