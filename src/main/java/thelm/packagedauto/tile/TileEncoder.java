package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.inventory.InventoryEncoder;
import thelm.packagedauto.inventory.InventoryEncoderPattern;
import thelm.packagedauto.recipe.PackageRecipeTypeProcessing;

public class TileEncoder extends TileBase {

	public static int patternSlots = 20;

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
		return StatCollector.translateToLocal("tile.packagedauto.encoder.name");
	}

	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		for(InventoryEncoderPattern inv : patternInventories) {
			inv.updateRecipeInfo();
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
		syncTile();
		markDirty();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = inventory.getStackInSlot(0);
		if(stack != null && stack.getItem() instanceof IPackageRecipeListItem) {
			List<IPackageRecipeInfo> recipeList = new ArrayList<>();
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
			IPackageRecipeList recipeListItem = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(stack);
			recipeListItem.setRecipeList(recipeList);
			NBTTagCompound nbt = recipeListItem.writeToNBT(new NBTTagCompound());
			inventory.getStackInSlot(0).setTagCompound(nbt);
		}
	}

	public void loadRecipeList() {
		ItemStack stack = inventory.getStackInSlot(0);
		if(stack != null && stack.getItem() instanceof IPackageRecipeListItem) {
			IPackageRecipeList recipeListItem = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(stack);
			List<IPackageRecipeInfo> recipeList = recipeListItem.getRecipeList();
			for(int i = 0; i < patternInventories.length; ++i) {
				InventoryEncoderPattern inv = patternInventories[i];
				if(i < recipeList.size()) {
					IPackageRecipeInfo recipe = recipeList.get(i);
					inv.recipeType = recipe.getRecipeType();
					inv.setRecipe(recipe.getEncoderStacks());
				}
				else {
					inv.recipeType = PackageRecipeTypeProcessing.INSTANCE;
					inv.setRecipe(null);
				}
			}
		}
		else {
			for(InventoryEncoderPattern inv : patternInventories) {
				inv.recipeType = PackageRecipeTypeProcessing.INSTANCE;
				inv.setRecipe(null);
			}
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
