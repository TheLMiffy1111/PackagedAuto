package thelm.packagedauto.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.block.BlockCrafter;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.item.ItemMisc;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.item.ItemRecipeHolder;
import thelm.packagedauto.network.GuiHandler;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.RecipeTypeCrafting;
import thelm.packagedauto.recipe.RecipeTypeProcessing;
import thelm.packagedauto.tile.TileCrafter;
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

	public void register(FMLPreInitializationEvent event) {
		registerConfig(event);
		registerBlocks();
		registerItems();
		registerModels();
		registerTileEntities();
		registerRecipeTypes();
		registerNetwork();
	}

	protected void registerConfig(FMLPreInitializationEvent event) {
		PackagedAutoConfig.init(event.getSuggestedConfigurationFile());
	}

	protected void registerBlocks() {
		registerBlock(BlockPackager.INSTANCE);
		registerBlock(BlockEncoder.INSTANCE);
		registerBlock(BlockUnpackager.INSTANCE);
		if(TileCrafter.enabled) {
			registerBlock(BlockCrafter.INSTANCE);
		}
	}

	protected void registerItems() {
		registerItem(BlockPackager.ITEM_INSTANCE);
		registerItem(BlockEncoder.ITEM_INSTANCE);
		registerItem(BlockUnpackager.ITEM_INSTANCE);
		if(TileCrafter.enabled) {
			registerItem(BlockCrafter.ITEM_INSTANCE);
		}

		registerItem(ItemRecipeHolder.INSTANCE);
		registerItem(ItemPackage.INSTANCE);
		registerItem(ItemMisc.PACKAGE_COMPONENT);
		registerItem(ItemMisc.ME_PACKAGE_COMPONENT);
	}

	protected void registerModels() {}

	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TilePackager.class, new ResourceLocation("packagedauto:packager"));
		GameRegistry.registerTileEntity(TileEncoder.class, new ResourceLocation("packagedauto:encoder"));
		GameRegistry.registerTileEntity(TileUnpackager.class, new ResourceLocation("packagedauto:unpackager"));
		if(TileCrafter.enabled) {
			GameRegistry.registerTileEntity(TileCrafter.class, new ResourceLocation("packagedauto:crafter"));
		}
	}

	protected void registerRecipeTypes() {
		RecipeTypeRegistry.registerRecipeType(RecipeTypeProcessing.INSTANCE);
		if(TileCrafter.enabled) {
			RecipeTypeRegistry.registerRecipeType(RecipeTypeCrafting.INSTANCE);
		}
	}

	protected void registerNetwork() {
		NetworkRegistry.INSTANCE.registerGuiHandler(PackagedAuto.instance, GuiHandler.INSTANCE);
		PacketHandler.registerPackets();
	}
}
