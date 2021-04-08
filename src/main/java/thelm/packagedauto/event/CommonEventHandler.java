package thelm.packagedauto.event;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import thelm.packagedauto.block.CrafterBlock;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.container.CrafterContainer;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.container.PackagerContainer;
import thelm.packagedauto.container.PackagerExtensionContainer;
import thelm.packagedauto.container.UnpackagerContainer;
import thelm.packagedauto.item.MiscItem;
import thelm.packagedauto.item.PackageItem;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.CraftingPackageRecipeType;
import thelm.packagedauto.recipe.OrderedProcessingPackageRecipeType;
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;
import thelm.packagedauto.tile.CrafterTile;
import thelm.packagedauto.tile.EncoderTile;
import thelm.packagedauto.tile.PackagerExtensionTile;
import thelm.packagedauto.tile.PackagerTile;
import thelm.packagedauto.tile.UnpackagerTile;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;

public class CommonEventHandler {

	public static final CommonEventHandler INSTANCE = new CommonEventHandler();

	public static CommonEventHandler getInstance() {
		return INSTANCE;
	}

	public void onConstruct() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
		PackagedAutoConfig.registerConfig();
	}

	@SubscribeEvent
	public void onBlockRegister(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		registry.register(EncoderBlock.INSTANCE);
		registry.register(PackagerBlock.INSTANCE);
		registry.register(PackagerExtensionBlock.INSTANCE);
		registry.register(UnpackagerBlock.INSTANCE);
		registry.register(CrafterBlock.INSTANCE);
	}

	@SubscribeEvent
	public void onItemRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registry.register(EncoderBlock.ITEM_INSTANCE);
		registry.register(PackagerBlock.ITEM_INSTANCE);
		registry.register(PackagerExtensionBlock.ITEM_INSTANCE);
		registry.register(UnpackagerBlock.ITEM_INSTANCE);
		registry.register(CrafterBlock.ITEM_INSTANCE);
		registry.register(RecipeHolderItem.INSTANCE);
		registry.register(PackageItem.INSTANCE);
		registry.register(MiscItem.PACKAGE_COMPONENT);
		registry.register(MiscItem.ME_PACKAGE_COMPONENT);
	}

	@SubscribeEvent
	public void onTileRegister(RegistryEvent.Register<TileEntityType<?>> event) {
		IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
		registry.register(EncoderTile.TYPE_INSTANCE);
		registry.register(PackagerTile.TYPE_INSTANCE);
		registry.register(PackagerExtensionTile.TYPE_INSTANCE);
		registry.register(UnpackagerTile.TYPE_INSTANCE);
		registry.register(CrafterTile.TYPE_INSTANCE);
	}

	@SubscribeEvent
	public void onContainerRegister(RegistryEvent.Register<ContainerType<?>> event) {
		IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
		registry.register(EncoderContainer.TYPE_INSTANCE);
		registry.register(PackagerContainer.TYPE_INSTANCE);
		registry.register(PackagerExtensionContainer.TYPE_INSTANCE);
		registry.register(UnpackagerContainer.TYPE_INSTANCE);
		registry.register(CrafterContainer.TYPE_INSTANCE);
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		ApiImpl.INSTANCE.registerRecipeType(ProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(OrderedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(CraftingPackageRecipeType.INSTANCE);

		PacketHandler.registerPackets();
	}

	@SubscribeEvent
	public void onModConfig(ModConfig.ModConfigEvent event) {
		switch(event.getConfig().getType()) {
		case SERVER:
			PackagedAutoConfig.reloadServerConfig();
			break;
		default:
			break;
		}
	}

	public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
		MiscHelper.INSTANCE.setServer(event.getServer());
	}
}
