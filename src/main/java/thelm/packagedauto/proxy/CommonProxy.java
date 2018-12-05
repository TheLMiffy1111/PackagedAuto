package thelm.packagedauto.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.item.ItemMisc;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.item.ItemRecipeHolder;
import thelm.packagedauto.network.GuiHandler;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.RecipeTypeProcessing;
import thelm.packagedauto.tile.TileEncoder;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TileUnpackager;

public class CommonProxy {

	public void registerBlock(Block block) {
		ForgeRegistries.BLOCKS.register(block);
	}

	public void registerItem(Item item) {
		ForgeRegistries.ITEMS.register(item);
	}

	public void registerBlocks() {
		registerBlock(BlockPackager.INSTANCE);
		registerBlock(BlockEncoder.INSTANCE);
		registerBlock(BlockUnpackager.INSTANCE);
	}

	public void registerItems() {
		registerItem(BlockPackager.ITEM_INSTANCE);
		registerItem(BlockEncoder.ITEM_INSTANCE);
		registerItem(BlockUnpackager.ITEM_INSTANCE);

		registerItem(ItemRecipeHolder.INSTANCE);
		registerItem(ItemPackage.INSTANCE);
		registerItem(ItemMisc.PACKAGE_COMPONENT);
	}

	public void registerModels() {}

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TilePackager.class, new ResourceLocation(PackagedAuto.MOD_ID, "packager"));
		GameRegistry.registerTileEntity(TileEncoder.class, new ResourceLocation(PackagedAuto.MOD_ID, "encoder"));
		GameRegistry.registerTileEntity(TileUnpackager.class, new ResourceLocation(PackagedAuto.MOD_ID, "unpackager"));
	}

	public void registerMisc() {
		NetworkRegistry.INSTANCE.registerGuiHandler(PackagedAuto.instance, GuiHandler.INSTANCE);
		PacketHandler.registerPackets();
		RecipeTypeRegistry.registerRecipeType(RecipeTypeProcessing.INSTANCE);
	}
}
