package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.inventory.InventoryEncoder;
import thelm.packagedauto.inventory.InventoryEncoderPattern;

public class TileEncoder extends TileBase {

	public static int patternSlots = 20;
	public static Set<String> disabledRecipeTypes = Collections.emptySet();

	public final InventoryEncoderPattern[] patternInventories = new InventoryEncoderPattern[patternSlots];
	public int patternIndex;

	public TileEncoder() {
		setInventory(new InventoryEncoder(this));
		for(int i = 0; i < patternInventories.length; ++i) {
			patternInventories[i] = new InventoryEncoderPattern(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.encoder.name");
	}

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		for(InventoryEncoderPattern inv : patternInventories) {
			inv.updateRecipeInfo(false);
		}
	}

	@Override
	public void readSyncNBT(NBTTagCompound nbt) {
		super.readSyncNBT(nbt);
		patternIndex = nbt.getByte("PatternIndex");
		for(int i = 0; i < patternInventories.length; ++i) {
			patternInventories[i].readFromNBT(nbt.getCompoundTag(String.format("Pattern%02d", i)));
		}
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
		super.writeSyncNBT(nbt);
		nbt.setByte("PatternIndex", (byte)patternIndex);
		for(int i = 0; i < patternInventories.length; ++i) {
			NBTTagCompound subNBT = new NBTTagCompound();
			patternInventories[i].writeToNBT(subNBT);
			nbt.setTag(String.format("Pattern%02d", i), subNBT);
		}
		return nbt;
	}

	public void setPatternIndex(int patternIndex) {
		this.patternIndex = patternIndex;
		syncTile(false);
		markDirty();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = inventory.getStackInSlot(0);
		if(stack.getItem() instanceof IRecipeListItem) {
			IRecipeListItem recipeListItem = (IRecipeListItem)stack.getItem();
			IRecipeList recipeListObj = recipeListItem.getRecipeList(stack);
			List<IRecipeInfo> recipeList = new ArrayList<>();
			if(!single) {
				for(InventoryEncoderPattern inv : patternInventories) {
					if(inv.recipeInfo != null) {
						recipeList.add(inv.recipeInfo);
					}
				}
			}
			else {
				InventoryEncoderPattern inv = patternInventories[patternIndex];
				if(inv.recipeInfo != null) {
					recipeList.add(inv.recipeInfo);
				}
			}
			recipeListObj.setRecipeList(recipeList);
			recipeListItem.setRecipeList(stack, recipeListObj);
		}
	}

	public void loadRecipeList(boolean single, boolean clear) {
		ItemStack stack = inventory.getStackInSlot(0);
		if(stack.getItem() instanceof IRecipeListItem) {
			IRecipeList recipeListObj = ((IRecipeListItem)stack.getItem()).getRecipeList(stack);
			List<IRecipeInfo> recipeList = recipeListObj.getRecipeList();
			if(single) {
				InventoryEncoderPattern inv = patternInventories[patternIndex];
				if(!clear && !recipeList.isEmpty()) {
					IRecipeInfo recipe = recipeList.get(0);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
			else for(int i = 0; i < patternInventories.length; ++i) {
				InventoryEncoderPattern inv = patternInventories[i];
				if(!clear && i < recipeList.size()) {
					IRecipeInfo recipe = recipeList.get(i);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
		}
		else if(single) {
			patternInventories[patternIndex].setRecipe(null);
		}
		else for(InventoryEncoderPattern inv : patternInventories) {
			inv.setRecipe(null);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiEncoder(new ContainerEncoder(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerEncoder(player.inventory, this);
	}
}
