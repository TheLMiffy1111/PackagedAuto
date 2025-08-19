package thelm.packagedauto.event;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.block.BlockCrafter;
import thelm.packagedauto.block.BlockCraftingProxy;
import thelm.packagedauto.block.BlockDistributor;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.block.BlockPackagerExtension;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.crafting.RecipeDistributorMarkerCloning;
import thelm.packagedauto.crafting.RecipeProxyMarkerCloning;
import thelm.packagedauto.crafting.RecipeRecipeHolderCloning;
import thelm.packagedauto.integration.patchouli.PackagedAutoPatchouliHandler;
import thelm.packagedauto.item.ItemDistributorMarker;
import thelm.packagedauto.item.ItemMisc;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.item.ItemProxyMarker;
import thelm.packagedauto.item.ItemRecipeHolder;
import thelm.packagedauto.item.ItemSettingsCloner;
import thelm.packagedauto.network.GuiHandler;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.RecipeTypeCrafting;
import thelm.packagedauto.recipe.RecipeTypeProcessing;
import thelm.packagedauto.recipe.RecipeTypeProcessingOrdered;
import thelm.packagedauto.recipe.RecipeTypeProcessingPositioned;
import thelm.packagedauto.tile.TileCrafter;
import thelm.packagedauto.tile.TileCraftingProxy;
import thelm.packagedauto.tile.TileDistributor;
import thelm.packagedauto.tile.TileEncoder;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;
import thelm.packagedauto.tile.TileUnpackager;

public class CommonEventHandler {

	public void registerBlock(Block block) {
		ForgeRegistries.BLOCKS.register(block);
	}

	public void registerItem(Item item) {
		ForgeRegistries.ITEMS.register(item);
	}

	public void onPreInit(FMLPreInitializationEvent event) {
		registerConfig(event);
		registerBlocks();
		registerItems();
		registerTileEntities();
		registerRecipeTypes();
		registerNetwork();
	}

	public void onInit(FMLInitializationEvent event) {
		registerRecipes();
		MiscUtil.conditionalRunnable(()->Loader.isModLoaded("patchouli"), ()->()->{
			PackagedAutoPatchouliHandler.init();
		}, ()->()->{}).run();
	}

	protected void registerConfig(FMLPreInitializationEvent event) {
		PackagedAutoConfig.init(event.getSuggestedConfigurationFile());
	}

	protected void registerBlocks() {
		registerBlock(BlockPackager.INSTANCE);
		registerBlock(BlockEncoder.INSTANCE);
		registerBlock(BlockUnpackager.INSTANCE);
		registerBlock(BlockPackagerExtension.INSTANCE);
		registerBlock(BlockDistributor.INSTANCE);
		registerBlock(BlockCraftingProxy.INSTANCE);
		if(TileCrafter.enabled) {
			registerBlock(BlockCrafter.INSTANCE);
		}
	}

	protected void registerItems() {
		registerItem(BlockPackager.ITEM_INSTANCE);
		registerItem(BlockEncoder.ITEM_INSTANCE);
		registerItem(BlockUnpackager.ITEM_INSTANCE);
		registerItem(BlockPackagerExtension.ITEM_INSTANCE);
		registerItem(BlockDistributor.ITEM_INSTANCE);
		registerItem(BlockCraftingProxy.ITEM_INSTANCE);
		if(TileCrafter.enabled) {
			registerItem(BlockCrafter.ITEM_INSTANCE);
		}

		registerItem(ItemRecipeHolder.INSTANCE);
		registerItem(ItemDistributorMarker.INSTANCE);
		registerItem(ItemProxyMarker.INSTANCE);
		registerItem(ItemSettingsCloner.INSTANCE);
		registerItem(ItemPackage.INSTANCE);
		registerItem(ItemMisc.PACKAGE_COMPONENT);
		registerItem(ItemMisc.ME_PACKAGE_COMPONENT);
	}

	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TilePackager.class, new ResourceLocation("packagedauto:packager"));
		GameRegistry.registerTileEntity(TileEncoder.class, new ResourceLocation("packagedauto:encoder"));
		GameRegistry.registerTileEntity(TileUnpackager.class, new ResourceLocation("packagedauto:unpackager"));
		GameRegistry.registerTileEntity(TilePackagerExtension.class, new ResourceLocation("packagedauto:packager_extension"));
		GameRegistry.registerTileEntity(TileDistributor.class, new ResourceLocation("packagedauto:distributor"));
		GameRegistry.registerTileEntity(TileCraftingProxy.class, new ResourceLocation("packagedauto:crafting_proxy"));
		if(TileCrafter.enabled) {
			GameRegistry.registerTileEntity(TileCrafter.class, new ResourceLocation("packagedauto:crafter"));
		}
	}

	protected void registerRecipeTypes() {
		RecipeTypeRegistry.registerRecipeType(RecipeTypeProcessing.INSTANCE);
		RecipeTypeRegistry.registerRecipeType(RecipeTypeProcessingOrdered.INSTANCE);
		RecipeTypeRegistry.registerRecipeType(RecipeTypeProcessingPositioned.INSTANCE);
		if(TileCrafter.enabled) {
			RecipeTypeRegistry.registerRecipeType(RecipeTypeCrafting.INSTANCE);
		}
	}

	protected void registerNetwork() {
		NetworkRegistry.INSTANCE.registerGuiHandler(PackagedAuto.MOD_ID, GuiHandler.INSTANCE);
		PacketHandler.registerPackets();
	}

	protected void registerRecipes() {
		ForgeRegistries.RECIPES.register(RecipeRecipeHolderCloning.INSTANCE);
		ForgeRegistries.RECIPES.register(RecipeDistributorMarkerCloning.INSTANCE);
		ForgeRegistries.RECIPES.register(RecipeProxyMarkerCloning.INSTANCE);
	}
}
