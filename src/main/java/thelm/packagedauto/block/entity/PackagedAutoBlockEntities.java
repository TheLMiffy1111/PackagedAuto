package thelm.packagedauto.block.entity;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.integration.appeng.blockentity.AECrafterBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AECraftingProxyBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AEDistributorBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagerBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagerExtensionBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagingProviderBlockEntity;
import thelm.packagedauto.integration.appeng.blockentity.AEUnpackagerBlockEntity;
import thelm.packagedauto.util.MiscHelper;

public class PackagedAutoBlockEntities {

	private PackagedAutoBlockEntities() {}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> of(BlockEntityType.BlockEntitySupplier<? extends T> factory, Supplier<Block>... validBlocks) {
		return ()->new BlockEntityType<>(factory, Arrays.stream(validBlocks).map(Supplier::get).filter(Objects::nonNull).collect(Collectors.toSet()), null);
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> of(BooleanSupplier condition, Supplier<Supplier<BlockEntityType.BlockEntitySupplier<? extends T>>> trueSupplier, Supplier<Supplier<BlockEntityType.BlockEntitySupplier<? extends T>>> falseSupplier, Supplier<Block>... validBlocks) {
		return ()->new BlockEntityType<>(MiscHelper.INSTANCE.conditionalSupplier(condition, trueSupplier, falseSupplier).get(), Arrays.stream(validBlocks).map(Supplier::get).filter(Objects::nonNull).collect(Collectors.toSet()), null);
	}

	public static final BooleanSupplier AE2_LOADED = ()->ModList.get().isLoaded("ae2");

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "packagedauto");

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EncoderBlockEntity>> ENCODER = BLOCK_ENTITIES.register(
			"encoder", of(EncoderBlockEntity::new, PackagedAutoBlocks.ENCODER));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PackagerBlockEntity>> PACKAGER = BLOCK_ENTITIES.register(
			"packager", of(AE2_LOADED, ()->()->AEPackagerBlockEntity::new, ()->()->PackagerBlockEntity::new, PackagedAutoBlocks.PACKAGER));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PackagerExtensionBlockEntity>> PACKAGER_EXTENSION = BLOCK_ENTITIES.register(
			"packager_extension", of(AE2_LOADED, ()->()->AEPackagerExtensionBlockEntity::new, ()->()->PackagerExtensionBlockEntity::new, PackagedAutoBlocks.PACKAGER_EXTENSION));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnpackagerBlockEntity>> UNPACKAGER = BLOCK_ENTITIES.register(
			"unpackager", of(AE2_LOADED, ()->()->AEUnpackagerBlockEntity::new, ()->()->UnpackagerBlockEntity::new, PackagedAutoBlocks.UNPACKAGER));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR = BLOCK_ENTITIES.register(
			"distributor", of(AE2_LOADED, ()->()->AEDistributorBlockEntity::new, ()->()->DistributorBlockEntity::new, PackagedAutoBlocks.DISTRIBUTOR));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingProxyBlockEntity>> CRAFTING_PROXY = BLOCK_ENTITIES.register(
			"crafting_proxy", of(AE2_LOADED, ()->()->AECraftingProxyBlockEntity::new, ()->()->CraftingProxyBlockEntity::new, PackagedAutoBlocks.CRAFTING_PROXY));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrafterBlockEntity>> CRAFTER = BLOCK_ENTITIES.register(
			"crafter", of(AE2_LOADED, ()->()->AECrafterBlockEntity::new, ()->()->CrafterBlockEntity::new, PackagedAutoBlocks.CRAFTER));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPackageFillerBlockEntity>> FLUID_PACKAGE_FILLER = BLOCK_ENTITIES.register(
			"fluid_package_filler", of(FluidPackageFillerBlockEntity::new, PackagedAutoBlocks.FLUID_PACKAGE_FILLER));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PackagingProviderBlockEntity>> PACKAGING_PROVIDER = BLOCK_ENTITIES.register(
			"packaging_provider", of(AE2_LOADED, ()->()->AEPackagingProviderBlockEntity::new, ()->()->PackagingProviderBlockEntity::new, PackagedAutoBlocks.PACKAGING_PROVIDER));
}
