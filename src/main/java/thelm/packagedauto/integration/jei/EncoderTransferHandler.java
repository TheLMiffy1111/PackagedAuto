package thelm.packagedauto.integration.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetRecipe;

public class EncoderTransferHandler implements IRecipeTransferHandler<ContainerEncoder> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public EncoderTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<ContainerEncoder> getContainerClass() {
		return ContainerEncoder.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(ContainerEncoder container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		String category = recipeLayout.getRecipeCategory().getUid();
		IRecipeType recipeType = container.patternInventory.recipeType;
		if(!recipeType.getJEICategories().contains(category)) {
			return transferHelper.createInternalError();
		}
		Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(recipeLayout, category);
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
