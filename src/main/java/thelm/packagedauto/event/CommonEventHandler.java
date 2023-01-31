package thelm.packagedauto.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import thelm.packagedauto.block.CrafterBlock;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.item.MiscItem;
import thelm.packagedauto.item.PackageItem;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.item.VolumePackageItem;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.recipe.CraftingPackageRecipeType;
import thelm.packagedauto.recipe.OrderedProcessingPackageRecipeType;
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
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.register(this);
		MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
		PackagedAutoConfig.registerConfig();

		DeferredRegister<Block> blockRegister = DeferredRegister.create(Registries.BLOCK, "packagedauto");
		blockRegister.register(modEventBus);
		blockRegister.register("encoder", ()->EncoderBlock.INSTANCE);
		blockRegister.register("packager", ()->PackagerBlock.INSTANCE);
		blockRegister.register("packager_extension", ()->PackagerExtensionBlock.INSTANCE);
		blockRegister.register("unpackager", ()->UnpackagerBlock.INSTANCE);
		blockRegister.register("crafter", ()->CrafterBlock.INSTANCE);
		blockRegister.register("fluid_package_filler", ()->FluidPackageFillerBlock.INSTANCE);

		DeferredRegister<Item> itemRegister = DeferredRegister.create(Registries.ITEM, "packagedauto");
		itemRegister.register(modEventBus);
		itemRegister.register("encoder", ()->EncoderBlock.ITEM_INSTANCE);
		itemRegister.register("packager", ()->PackagerBlock.ITEM_INSTANCE);
		itemRegister.register("packager_extension", ()->PackagerExtensionBlock.ITEM_INSTANCE);
		itemRegister.register("unpackager", ()->UnpackagerBlock.ITEM_INSTANCE);
		itemRegister.register("crafter", ()->CrafterBlock.ITEM_INSTANCE);
		itemRegister.register("fluid_package_filler", ()->FluidPackageFillerBlock.ITEM_INSTANCE);
		itemRegister.register("recipe_holder", ()->RecipeHolderItem.INSTANCE);
		itemRegister.register("package", ()->PackageItem.INSTANCE);
		itemRegister.register("volume_package", ()->VolumePackageItem.INSTANCE);
		itemRegister.register("package_component", ()->MiscItem.PACKAGE_COMPONENT);
		itemRegister.register("me_package_component", ()->MiscItem.ME_PACKAGE_COMPONENT);

		DeferredRegister<BlockEntityType<?>> blockEntityRegister = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "packagedauto");
		blockEntityRegister.register(modEventBus);
		blockEntityRegister.register("encoder", ()->EncoderBlockEntity.TYPE_INSTANCE);
		blockEntityRegister.register("packager", ()->PackagerBlockEntity.TYPE_INSTANCE);
		blockEntityRegister.register("packager_extension", ()->PackagerExtensionBlockEntity.TYPE_INSTANCE);
		blockEntityRegister.register("unpackager", ()->UnpackagerBlockEntity.TYPE_INSTANCE);
		blockEntityRegister.register("crafter", ()->CrafterBlockEntity.TYPE_INSTANCE);
		blockEntityRegister.register("fluid_package_filler", ()->FluidPackageFillerBlockEntity.TYPE_INSTANCE);

		DeferredRegister<MenuType<?>> menuRegister = DeferredRegister.create(Registries.MENU, "packagedauto");
		menuRegister.register(modEventBus);
		menuRegister.register("encoder", ()->EncoderMenu.TYPE_INSTANCE);
		menuRegister.register("packager", ()->PackagerMenu.TYPE_INSTANCE);
		menuRegister.register("packager_extension", ()->PackagerExtensionMenu.TYPE_INSTANCE);
		menuRegister.register("unpackager", ()->UnpackagerMenu.TYPE_INSTANCE);
		menuRegister.register("crafter", ()->CrafterMenu.TYPE_INSTANCE);
		menuRegister.register("fluid_package_filler", ()->FluidPackageFillerMenu.TYPE_INSTANCE);
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		ApiImpl.INSTANCE.registerVolumeType(FluidVolumeType.INSTANCE);

		ApiImpl.INSTANCE.registerRecipeType(ProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(OrderedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(CraftingPackageRecipeType.INSTANCE);

		PacketHandler.registerPackets();
	}

	@SubscribeEvent
	public void onCreativeModeTabRegister(CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation("packagedauto:tab"),
				builder->builder.
				title(Component.translatable("itemGroup.packagedauto")).
				icon(()->new ItemStack(PackageItem.INSTANCE)).
				displayItems((enabledFeatures, output, displayOperatorCreativeTab)->{
					output.accept(EncoderBlock.ITEM_INSTANCE);
					output.accept(PackagerBlock.ITEM_INSTANCE);
					output.accept(PackagerExtensionBlock.ITEM_INSTANCE);
					output.accept(UnpackagerBlock.ITEM_INSTANCE);
					output.accept(CrafterBlock.ITEM_INSTANCE);
					output.accept(FluidPackageFillerBlock.ITEM_INSTANCE);
					output.accept(RecipeHolderItem.INSTANCE);
					output.accept(MiscItem.PACKAGE_COMPONENT);
					output.accept(MiscItem.ME_PACKAGE_COMPONENT);
				}).
				build());
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
