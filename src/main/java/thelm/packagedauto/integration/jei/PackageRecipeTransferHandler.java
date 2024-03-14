package thelm.packagedauto.integration.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetRecipePacket;

public class PackageRecipeTransferHandler implements IRecipeTransferHandler<EncoderContainer> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public PackageRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<EncoderContainer> getContainerClass() {
		return EncoderContainer.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(EncoderContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
		IPackageRecipeInfo recipeInfo = (IPackageRecipeInfo)recipe;
		IPackageRecipeType recipeType = container.patternItemHandler.recipeType;
		Int2ObjectMap<ItemStack> map;
		if(recipeInfo.getRecipeType() == recipeType) {
			map = recipeInfo.getEncoderStacks();
		}
		else if(recipeType.getJEICategories().contains(PackageRecipeCategory.UID)) {
			map = recipeType.getRecipeTransferMap(new RecipeLayoutWrapper(recipeLayout));
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
		PacketHandler.INSTANCE.sendToServer(new SetRecipePacket(map));
		return null;
	}
}
