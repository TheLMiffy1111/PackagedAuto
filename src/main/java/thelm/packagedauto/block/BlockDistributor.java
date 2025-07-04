package thelm.packagedauto.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TileDistributor;

public class BlockDistributor extends BlockBase {

	public static final BlockDistributor INSTANCE = new BlockDistributor();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packagedauto:distributor");
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:distributor#normal");

	protected BlockDistributor() {
		super(Material.IRON);
		setHardness(10F);
		setResistance(25F);
		setSoundType(SoundType.METAL);
		setTranslationKey("packagedauto.distributor");
		setRegistryName("packagedauto:distributor");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TileDistributor();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(playerIn.isSneaking()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if(tileentity instanceof TileDistributor) {
				if(!worldIn.isRemote) {
					((TileDistributor)tileentity).sendPreview((EntityPlayerMP)playerIn);
				}
				return true;
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileDistributor) {
			for(Int2ObjectMap.Entry<ItemStack> entry : ((TileDistributor)tileentity).pending.int2ObjectEntrySet()) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), entry.getValue());
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
	}
}
