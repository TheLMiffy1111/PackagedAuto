package thelm.packagedauto.integration.jei;


import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.packet.SetRecipePacket;

public class EncoderTransferHandler implements IUniversalRecipeTransferHandler<EncoderMenu> {

	private final IRecipeTransferHandlerHelper transferHelper;

	public EncoderTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
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
	public IRecipeTransferError transferRecipe(EncoderMenu menu, Object recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		List<Identifier> categories = PackagedAutoJEIPlugin.getRecipeCategoriesForRecipe(recipe);
		if(categories.isEmpty()) {
			return transferHelper.createInternalError();
		}
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		if(!categories.stream().anyMatch(recipeType.getJEICategories()::contains)) {
			return transferHelper.createInternalError();
		}
		Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(new RecipeSlotsViewWrapper(recipe, recipeSlots));
		if(map == null || map.isEmpty()) {
			return transferHelper.createInternalError();
		}
		if(!doTransfer) {
			return null;
		}
		ClientPacketDistributor.sendToServer(new SetRecipePacket(map));
		return null;
	}
}
