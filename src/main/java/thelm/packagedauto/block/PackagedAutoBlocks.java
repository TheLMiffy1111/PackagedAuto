package thelm.packagedauto.block;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PackagedAutoBlocks {

	private PackagedAutoBlocks() {}

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("packagedauto");

	public static final DeferredBlock<Block> ENCODER = BLOCKS.register("encoder", EncoderBlock::new);
	public static final DeferredBlock<Block> PACKAGER = BLOCKS.register("packager", PackagerBlock::new);
	public static final DeferredBlock<Block> PACKAGER_EXTENSION = BLOCKS.register("packager_extension", PackagerExtensionBlock::new);
	public static final DeferredBlock<Block> UNPACKAGER = BLOCKS.register("unpackager", UnpackagerBlock::new);
	public static final DeferredBlock<Block> DISTRIBUTOR = BLOCKS.register("distributor", DistributorBlock::new);
	public static final DeferredBlock<Block> CRAFTING_PROXY = BLOCKS.register("crafting_proxy", CraftingProxyBlock::new);
	public static final DeferredBlock<Block> CRAFTER = BLOCKS.register("crafter", CrafterBlock::new);
	public static final DeferredBlock<Block> FLUID_PACKAGE_FILLER = BLOCKS.register("fluid_package_filler", FluidPackageFillerBlock::new);
	public static final DeferredBlock<Block> PACKAGING_PROVIDER = BLOCKS.register("packaging_provider", PackagingProviderBlock::new);
}
