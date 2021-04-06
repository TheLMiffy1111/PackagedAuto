package thelm.packagedauto.integration.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetRecipePacket;

public class EncoderTransferHandler implements IRecipeTransferHandler<EncoderContainer> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public EncoderTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<EncoderContainer> getContainerClass() {
		return EncoderContainer.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(EncoderContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
		ResourceLocation category = recipeLayout.getRecipeCategory().getUid();
		IPackageRecipeType recipeType = container.patternItemHandler.recipeType;
		if(!(recipeType.getJEICategories().contains(category))) {
			return transferHelper.createInternalError();
		}
		Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(new RecipeLayoutWrapper(recipeLayout));
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
