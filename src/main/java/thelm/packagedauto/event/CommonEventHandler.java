package thelm.packagedauto.event;

import appeng.api.crafting.PatternDetailsHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import thelm.packagedauto.block.CrafterBlock;
import thelm.packagedauto.block.CraftingProxyBlock;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.PackagingProviderBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.crafting.DistributorMarkerCloningRecipe;
import thelm.packagedauto.crafting.ProxyMarkerCloningRecipe;
import thelm.packagedauto.crafting.RecipeHolderCloningRecipe;
import thelm.packagedauto.integration.appeng.recipe.PackagePatternDetailsDecoder;
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.item.MiscItem;
import thelm.packagedauto.item.PackageItem;
import thelm.packagedauto.item.ProxyMarkerItem;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.item.SettingsClonerItem;
import thelm.packagedauto.item.VolumePackageItem;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.menu.CraftingProxyMenu;
import thelm.packagedauto.menu.DistributorMenu;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.CraftingPackageRecipeType;
import thelm.packagedauto.recipe.OrderedProcessingPackageRecipeType;
import thelm.packagedauto.recipe.PositionedProcessingPackageRecipeType;
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.volume.FluidVolumeType;

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
		registry.register(DistributorBlock.INSTANCE);
		registry.register(CraftingProxyBlock.INSTANCE);
		registry.register(CrafterBlock.INSTANCE);
		registry.register(FluidPackageFillerBlock.INSTANCE);
		registry.register(PackagingProviderBlock.INSTANCE);
	}

	@SubscribeEvent
	public void onItemRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registry.register(EncoderBlock.ITEM_INSTANCE);
		registry.register(PackagerBlock.ITEM_INSTANCE);
		registry.register(PackagerExtensionBlock.ITEM_INSTANCE);
		registry.register(UnpackagerBlock.ITEM_INSTANCE);
		registry.register(DistributorBlock.ITEM_INSTANCE);
		registry.register(CraftingProxyBlock.ITEM_INSTANCE);
		registry.register(CrafterBlock.ITEM_INSTANCE);
		registry.register(FluidPackageFillerBlock.ITEM_INSTANCE);
		registry.register(PackagingProviderBlock.ITEM_INSTANCE);
		registry.register(RecipeHolderItem.INSTANCE);
		registry.register(DistributorMarkerItem.INSTANCE);
		registry.register(ProxyMarkerItem.INSTANCE);
		registry.register(SettingsClonerItem.INSTANCE);
		registry.register(PackageItem.INSTANCE);
		registry.register(VolumePackageItem.INSTANCE);
		registry.register(MiscItem.PACKAGE_COMPONENT);
		registry.register(MiscItem.ME_PACKAGE_COMPONENT);
	}

	@SubscribeEvent
	public void onBlockEntityRegister(RegistryEvent.Register<BlockEntityType<?>> event) {
		IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
		registry.register(EncoderBlockEntity.TYPE_INSTANCE);
		registry.register(PackagerBlockEntity.TYPE_INSTANCE);
		registry.register(PackagerExtensionBlockEntity.TYPE_INSTANCE);
		registry.register(UnpackagerBlockEntity.TYPE_INSTANCE);
		registry.register(DistributorBlockEntity.TYPE_INSTANCE);
		registry.register(CraftingProxyBlockEntity.TYPE_INSTANCE);
		registry.register(CrafterBlockEntity.TYPE_INSTANCE);
		registry.register(FluidPackageFillerBlockEntity.TYPE_INSTANCE);
		registry.register(PackagingProviderBlockEntity.TYPE_INSTANCE);
	}

	@SubscribeEvent
	public void onMenuRegister(RegistryEvent.Register<MenuType<?>> event) {
		IForgeRegistry<MenuType<?>> registry = event.getRegistry();
		registry.register(EncoderMenu.TYPE_INSTANCE);
		registry.register(PackagerMenu.TYPE_INSTANCE);
		registry.register(PackagerExtensionMenu.TYPE_INSTANCE);
		registry.register(UnpackagerMenu.TYPE_INSTANCE);
		registry.register(DistributorMenu.TYPE_INSTANCE);
		registry.register(CraftingProxyMenu.TYPE_INSTANCE);
		registry.register(CrafterMenu.TYPE_INSTANCE);
		registry.register(FluidPackageFillerMenu.TYPE_INSTANCE);
		registry.register(PackagingProviderMenu.TYPE_INSTANCE);
	}

	@SubscribeEvent
	public void onRecipeSerializerRegister(RegistryEvent.Register<RecipeSerializer<?>> event) {
		IForgeRegistry<RecipeSerializer<?>> registry = event.getRegistry();
		registry.register(RecipeHolderCloningRecipe.SERIALIZER);
		registry.register(DistributorMarkerCloningRecipe.SERIALIZER);
		registry.register(ProxyMarkerCloningRecipe.SERIALIZER);
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		ApiImpl.INSTANCE.registerVolumeType(FluidVolumeType.INSTANCE);

		ApiImpl.INSTANCE.registerRecipeType(ProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(OrderedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(PositionedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(CraftingPackageRecipeType.INSTANCE);

		PacketHandler.registerPackets();

		MiscHelper.INSTANCE.conditionalRunnable(()->ModList.get().isLoaded("ae2"), ()->()->{
			PatternDetailsHelper.registerDecoder(PackagePatternDetailsDecoder.INSTANCE);
		}, ()->()->{}).run();
	}

	@SubscribeEvent
	public void onModConfig(ModConfigEvent event) {
		switch(event.getConfig().getType()) {
		case SERVER -> PackagedAutoConfig.reloadServerConfig();
		default -> {}
		}
	}

	public void onServerAboutToStart(ServerAboutToStartEvent event) {
		MiscHelper.INSTANCE.setServer(event.getServer());
	}
}
