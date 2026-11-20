package thelm.packagedauto.block;

import java.util.function.UnaryOperator;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PackagedAutoBlocks {

	private PackagedAutoBlocks() {}

	public static final UnaryOperator<BlockBehaviour.Properties> MACHINE = p->p.strength(10F, 15F).mapColor(MapColor.METAL).sound(SoundType.METAL);

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("packagedauto");

	public static final DeferredBlock<Block> ENCODER = BLOCKS.registerBlock("encoder", EncoderBlock::new);
	public static final DeferredBlock<Block> PACKAGER = BLOCKS.registerBlock("packager", PackagerBlock::new);
	public static final DeferredBlock<Block> PACKAGER_EXTENSION = BLOCKS.registerBlock("packager_extension", PackagerExtensionBlock::new);
	public static final DeferredBlock<Block> UNPACKAGER = BLOCKS.registerBlock("unpackager", UnpackagerBlock::new);
	public static final DeferredBlock<Block> DISTRIBUTOR = BLOCKS.registerBlock("distributor", DistributorBlock::new);
	public static final DeferredBlock<Block> CRAFTING_PROXY = BLOCKS.registerBlock("crafting_proxy", CraftingProxyBlock::new);
	public static final DeferredBlock<Block> CRAFTER = BLOCKS.registerBlock("crafter", CrafterBlock::new);
	public static final DeferredBlock<Block> FLUID_PACKAGE_FILLER = BLOCKS.registerBlock("fluid_package_filler", FluidPackageFillerBlock::new);
	public static final DeferredBlock<Block> PACKAGING_PROVIDER = BLOCKS.registerBlock("packaging_provider", PackagingProviderBlock::new);
}
