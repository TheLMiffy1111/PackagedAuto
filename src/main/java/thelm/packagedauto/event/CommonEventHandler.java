package thelm.packagedauto.event;

import appeng.capabilities.AppEngCapabilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.block.CrafterBlock;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.integration.appeng.AppEngUtil;
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
import thelm.packagedauto.packet.ChangeBlockingPacket;
import thelm.packagedauto.packet.ChangePackagingPacket;
import thelm.packagedauto.packet.CycleRecipeTypePacket;
import thelm.packagedauto.packet.LoadRecipeListPacket;
import thelm.packagedauto.packet.SaveRecipeListPacket;
import thelm.packagedauto.packet.SetFluidAmountPacket;
import thelm.packagedauto.packet.SetItemStackPacket;
import thelm.packagedauto.packet.SetPatternIndexPacket;
import thelm.packagedauto.packet.SetRecipePacket;
import thelm.packagedauto.packet.SyncEnergyPacket;
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

	public void onConstruct(IEventBus modEventBus) {
		modEventBus.register(this);
		NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
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

		DeferredRegister<CreativeModeTab> creativeTabRegister = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "packagedauto");
		creativeTabRegister.register(modEventBus);
		creativeTabRegister.register("tab", ()->CreativeModeTab.builder().
				title(Component.translatable("itemGroup.packagedauto")).
				icon(()->new ItemStack(PackageItem.INSTANCE)).
				displayItems((parameters, output)->{
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
	public void onCommonSetup(FMLCommonSetupEvent event) {
		ApiImpl.INSTANCE.registerVolumeType(FluidVolumeType.INSTANCE);

		ApiImpl.INSTANCE.registerRecipeType(ProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(OrderedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(CraftingPackageRecipeType.INSTANCE);
	}

	@SubscribeEvent
	public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EncoderBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagerExtensionBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, UnpackagerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CrafterBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FluidPackageFillerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getItemHandler);

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagerExtensionBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, UnpackagerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CrafterBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FluidPackageFillerBlockEntity.TYPE_INSTANCE, BaseBlockEntity::getEnergyStorage);

		MiscHelper.INSTANCE.conditionalRunnable(()->ModList.get().isLoaded("ae2"), ()->()->{
			event.registerBlockEntity(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, PackagerBlockEntity.TYPE_INSTANCE, AppEngUtil::getAsInWorldGridNodeHost);
			event.registerBlockEntity(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, PackagerExtensionBlockEntity.TYPE_INSTANCE, AppEngUtil::getAsInWorldGridNodeHost);
			event.registerBlockEntity(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, UnpackagerBlockEntity.TYPE_INSTANCE, AppEngUtil::getAsInWorldGridNodeHost);
			event.registerBlockEntity(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, CrafterBlockEntity.TYPE_INSTANCE, AppEngUtil::getAsInWorldGridNodeHost);
		}, ()->()->{}).run();

		for(IVolumeType volumeType : ApiImpl.INSTANCE.getVolumeTypeRegistry().values()) {
			event.registerItem(volumeType.getItemCapability(), (stack, ctx)->{
				if(stack.getItem() instanceof IVolumePackageItem volumePackage && volumeType == volumePackage.getVolumeType(stack)) {
					return volumeType.makeItemCapability(stack);
				}
				return null;
			}, VolumePackageItem.INSTANCE);
		}
	}

	@SubscribeEvent
	public void onRegisterPayloadHandler(RegisterPayloadHandlerEvent event) {
		IPayloadRegistrar registrar = event.registrar("packagedauto");
		registrar.play(SyncEnergyPacket.ID, SyncEnergyPacket::read, builder->builder.client(SyncEnergyPacket::handle));
		registrar.play(SetItemStackPacket.ID, SetItemStackPacket::read, builder->builder.server(SetItemStackPacket::handle));
		registrar.play(SetPatternIndexPacket.ID, SetPatternIndexPacket::read, builder->builder.server(SetPatternIndexPacket::handle));
		registrar.play(CycleRecipeTypePacket.ID, CycleRecipeTypePacket::read, builder->builder.server(CycleRecipeTypePacket::handle));
		registrar.play(SaveRecipeListPacket.ID, SaveRecipeListPacket::read, builder->builder.server(SaveRecipeListPacket::handle));
		registrar.play(SetRecipePacket.ID, SetRecipePacket::read, builder->builder.server(SetRecipePacket::handle));
		registrar.play(LoadRecipeListPacket.ID, LoadRecipeListPacket::read, builder->builder.server(LoadRecipeListPacket::handle));
		registrar.play(ChangeBlockingPacket.ID, ChangeBlockingPacket::read, builder->builder.server(ChangeBlockingPacket::handle));
		registrar.play(SetFluidAmountPacket.ID, SetFluidAmountPacket::read, builder->builder.server(SetFluidAmountPacket::handle));
		registrar.play(ChangePackagingPacket.ID, ChangePackagingPacket::read, builder->builder.server(ChangePackagingPacket::handle));
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
