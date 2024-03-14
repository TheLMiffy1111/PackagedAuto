package thelm.packagedauto.integration.jei;

import java.lang.reflect.Field;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeWrapper;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetRecipe;

public class PackageRecipeTransferHandler implements IRecipeTransferHandler<ContainerEncoder> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public PackageRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<ContainerEncoder> getContainerClass() {
		return ContainerEncoder.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(ContainerEncoder container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		IRecipeInfo recipeInfo;
		try {
			Field recipeField = recipeLayout.getClass().getDeclaredField("recipeWrapper");
			recipeField.setAccessible(true);
			Object recipe = recipeField.get(recipeLayout);
			if(recipe instanceof PackageRecipeWrapper) {
				recipeInfo = ((PackageRecipeWrapper)recipe).recipe;
			}
			else {
				return transferHelper.createInternalError();
			}
		}
		catch(Exception e) {
			return transferHelper.createInternalError();
		}
		IRecipeType recipeType = container.patternInventory.recipeType;
		Int2ObjectMap<ItemStack> map;
		if(recipeInfo.getRecipeType() == recipeType) {
			map = recipeInfo.getEncoderStacks();
		}
		else if(recipeType.getJEICategories().contains(PackageRecipeCategory.UID)) {
			map = recipeType.getRecipeTransferMap(recipeLayout, PackageRecipeCategory.UID);
		}
		else {
			return transferHelper.createInternalError();
		}
		if(map == null || map.isEmpty()) {
			return transferHelper.createInternalError();
		}
		if(!doTransfer) {
			return null;
		}
		PacketHandler.INSTANCE.sendToServer(new PacketSetRecipe(map));
		return null;
	}
}
