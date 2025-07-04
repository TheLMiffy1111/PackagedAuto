package thelm.packagedauto.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.CraftingProxyTile;

public class CraftingProxyBlock extends BaseBlock {

	public static final CraftingProxyBlock INSTANCE = new CraftingProxyBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:crafting_proxy");

	protected CraftingProxyBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).strength(10F, 15F).sound(SoundType.METAL));
		setRegistryName("packagedauto:crafting_proxy");
	}

	@Override
	public CraftingProxyTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return CraftingProxyTile.TYPE_INSTANCE.create();
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult rayTraceResult) {
		if(playerIn.isShiftKeyDown()) {
			TileEntity tileentity = worldIn.getBlockEntity(pos);
			if(tileentity instanceof CraftingProxyTile) {
				if(!worldIn.isClientSide) {
					((CraftingProxyTile)tileentity).sendPreview((ServerPlayerEntity)playerIn);
				}
				return ActionResultType.SUCCESS;
			}
		}
		return super.use(state, worldIn, pos, playerIn, hand, rayTraceResult);
	}
}
