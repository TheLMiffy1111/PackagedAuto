package thelm.packagedauto.integration.jei;


import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetRecipePacket;

public class EncodingCategoryTransferHandler implements IRecipeTransferHandler<EncoderMenu, IPackageRecipeInfo> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public EncodingCategoryTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<EncoderMenu> getContainerClass() {
		return EncoderMenu.class;
	}

	@Override
	public Optional<MenuType<EncoderMenu>> getMenuType() {
		return Optional.empty();
	}

	@Override
	public RecipeType<IPackageRecipeInfo> getRecipeType() {
		return PackageRecipeCategory.TYPE;
	}

	@Override
	public IRecipeTransferError transferRecipe(EncoderMenu menu, IPackageRecipeInfo recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		if(recipe.getRecipeType() != recipeType) {
			return transferHelper.createInternalError();
		}
		Int2ObjectMap<ItemStack> map = recipe.getEncoderStacks();
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
