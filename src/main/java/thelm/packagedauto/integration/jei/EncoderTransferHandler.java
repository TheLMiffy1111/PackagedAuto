package thelm.packagedauto.integration.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetRecipePacket;

public class EncoderTransferHandler implements IRecipeTransferHandler<EncoderMenu, Object> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public EncoderTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<EncoderMenu> getContainerClass() {
		return EncoderMenu.class;
	}

	@Override
	public Class<Object> getRecipeClass() {
		return Object.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(EncoderMenu menu, Object recipe, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
		if(recipeLayout instanceof RecipeLayout<?> recipeLayoutImpl) {
			ResourceLocation category = recipeLayoutImpl.getRecipeCategory().getUid();
			IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
			if(!(recipeType.getJEICategories().contains(category))) {
				return transferHelper.createInternalError();
			}
			Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(new RecipeLayoutWrapper(recipeLayoutImpl));
			if(map == null || map.isEmpty()) {
				return transferHelper.createInternalError();
			}
			if(!doTransfer) {
				return null;
			}
			PacketHandler.INSTANCE.sendToServer(new SetRecipePacket(map));
			return null;
		}
		return transferHelper.createInternalError();
	}
}
