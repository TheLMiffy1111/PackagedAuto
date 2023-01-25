package thelm.packagedauto.integration.nei;

import java.util.Map;
import java.util.Set;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetRecipe;

public class EncoderOverlayHandler implements IOverlayHandler {

	public static final EncoderOverlayHandler INSTANCE = new EncoderOverlayHandler();

	@Override
	public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipeHandler, int recipeIndex, boolean shift) {
		if(firstGui instanceof GuiEncoder) {
			IPackageRecipeType recipeType = ((GuiEncoder)firstGui).container.patternInventory.recipeType;
			Set<String> categories = NEIHandler.INSTANCE.getRecipeCategories(recipeHandler);
			if(!recipeType.getNEICategories().stream().anyMatch(categories::contains)) {
				return;
			}
			Map<Integer, ItemStack> map = recipeType.getRecipeTransferMap(recipeHandler, recipeIndex, categories);
			if(map == null || map.isEmpty()) {
				return;
			}
			PacketHandler.INSTANCE.sendToServer(new PacketSetRecipe(map));
		}
	}
}
