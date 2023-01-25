package thelm.packagedauto.proxy;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.block.BlockCrafter;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.block.BlockPackagerExtension;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.integration.nei.NEIHandler;
import thelm.packagedauto.item.ItemMisc;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.item.ItemRecipeHolder;
import thelm.packagedauto.network.GuiHandler;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.PackageRecipeTypeCrafting;
import thelm.packagedauto.recipe.PackageRecipeTypeProcessing;
import thelm.packagedauto.recipe.PackageRecipeTypeProcessingOrdered;
import thelm.packagedauto.tile.TileCrafter;
import thelm.packagedauto.tile.TileEncoder;
import thelm.packagedauto.tile.TilePackager;
import thelm.packagedauto.tile.TilePackagerExtension;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;

public class CommonProxy {

	public void register(FMLPreInitializationEvent event) {
		registerConfig(event);
		registerBlocks();
		registerItems();
		registerModels();
		registerTileEntities();
		registerRecipeTypes();
		registerNetwork();
	}

	public void register(FMLInitializationEvent event) {
		registerRecipes();
		registerNEI();
	}

	protected void registerConfig(FMLPreInitializationEvent event) {
		PackagedAutoConfig.init(event.getSuggestedConfigurationFile());
	}

	protected void registerBlocks() {
		GameRegistry.registerBlock(BlockEncoder.INSTANCE, null, "encoder");
		GameRegistry.registerBlock(BlockPackager.INSTANCE, null, "packager");
		GameRegistry.registerBlock(BlockPackagerExtension.INSTANCE, null, "packager_extension");
		GameRegistry.registerBlock(BlockUnpackager.INSTANCE, null, "unpackager");
		if(TileCrafter.enabled) {
			GameRegistry.registerBlock(BlockCrafter.INSTANCE, null, "crafter");
		}
	}

	protected void registerItems() {
		GameRegistry.registerItem(BlockEncoder.ITEM_INSTANCE, "encoder");
		GameRegistry.registerItem(BlockPackager.ITEM_INSTANCE, "packager");
		GameRegistry.registerItem(BlockPackagerExtension.ITEM_INSTANCE, "packager_extension");
		GameRegistry.registerItem(BlockUnpackager.ITEM_INSTANCE, "unpackager");
		if(TileCrafter.enabled) {
			GameRegistry.registerItem(BlockCrafter.ITEM_INSTANCE, "crafter");
		}

		GameRegistry.registerItem(ItemRecipeHolder.INSTANCE, "recipe_holder");
		GameRegistry.registerItem(ItemPackage.INSTANCE, "package");
		GameRegistry.registerItem(ItemMisc.PACKAGE_COMPONENT, "package_component");
		GameRegistry.registerItem(ItemMisc.ME_PACKAGE_COMPONENT, "me_package_component");
	}

	protected void registerModels() {}

	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEncoder.class, "packagedauto:encoder");
		GameRegistry.registerTileEntity(TilePackager.class, "packagedauto:packager");
		GameRegistry.registerTileEntity(TilePackagerExtension.class, "packagedauto:packager_extension");
		GameRegistry.registerTileEntity(TileUnpackager.class, "packagedauto:unpackager");
		if(TileCrafter.enabled) {
			GameRegistry.registerTileEntity(TileCrafter.class, "packagedauto:crafter");
		}
	}

	protected void registerRecipeTypes() {
		ApiImpl.INSTANCE.registerRecipeType(PackageRecipeTypeProcessing.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(PackageRecipeTypeProcessingOrdered.INSTANCE);
		if(TileCrafter.enabled) {
			ApiImpl.INSTANCE.registerRecipeType(PackageRecipeTypeCrafting.INSTANCE);
		}
	}

	protected void registerNetwork() {
		NetworkRegistry.INSTANCE.registerGuiHandler(PackagedAuto.instance, GuiHandler.INSTANCE);
		PacketHandler.registerPackets();
	}

	protected void registerRecipes() {
		boolean ae2Loaded = Loader.isModLoaded("appliedenergistics2");
		Item component = ae2Loaded ? ItemMisc.ME_PACKAGE_COMPONENT : ItemMisc.PACKAGE_COMPONENT;
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockEncoder.INSTANCE, 1), new Object[] {
				"ICI",
				"TGT",
				"IRI",
				'C', ItemMisc.PACKAGE_COMPONENT,
				'G', "glowstone",
				'R', Items.comparator,
				'T', Blocks.crafting_table,
				'I', "ingotIron",
		}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockPackager.INSTANCE, 1), new Object[] {
				"ICI",
				"RTR",
				"IPI",
				'C', component,
				'T', Blocks.crafting_table,
				'P', Blocks.piston,
				'I', "ingotIron",
				'R', "dustRedstone",
		}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockPackagerExtension.INSTANCE, 1), new Object[] {
				"ICI",
				"GTG",
				"IPI",
				'C', component,
				'T', Blocks.crafting_table,
				'P', Blocks.piston,
				'I', "ingotIron",
				'G', "dustGlowstone",
		}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockUnpackager.INSTANCE, 1), new Object[] {
				"ICI",
				"RSR",
				"IHI",
				'C', component,
				'S', "chestWood",
				'H', Blocks.hopper,
				'I', "ingotIron",
				'R', "dustRedstone",
		}));
		if(TileCrafter.enabled) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockCrafter.INSTANCE, 1), new Object[] {
					"ICI",
					"RTR",
					"IHI",
					'C', component,
					'T', Blocks.crafting_table,
					'H', Blocks.hopper,
					'I', "ingotIron",
					'R', "dustRedstone",
			}));
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ItemRecipeHolder.INSTANCE, 2), new Object[] {
				"TRT",
				"RGR",
				"ICI",
				'C', ItemMisc.PACKAGE_COMPONENT,
				'G', "glowstone",
				'T', "blockGlass",
				'I', "ingotIron",
				'R', "dustRedstone",
		}));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ItemMisc.PACKAGE_COMPONENT, 1), new Object[] {
				"GWG",
				"WEW",
				"GWG",
				'E', Items.ender_eye,
				'W', "plankWood",
				'G', "ingotGold",
		}));
		if(ae2Loaded) {
			Item material = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial");
			ItemStack annihilationCore = new ItemStack(material, 1, 44);
			ItemStack formationCore = new ItemStack(material, 1, 43);
			Item quartzGlass = GameRegistry.findItem("appliedenergistics2", "tile.BlockQuartzGlass");
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ItemMisc.ME_PACKAGE_COMPONENT, 1), new Object[] {
					"IGI",
					"ACF",
					"IGI",
					'C', ItemMisc.PACKAGE_COMPONENT,
					'A', annihilationCore,
					'F', formationCore,
					'G', quartzGlass,
					'I', "ingotIron",
			}));
		}
	}

	protected void registerNEI() {}
}
