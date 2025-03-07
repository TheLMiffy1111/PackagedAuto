package thelm.packagedauto.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
import thelm.packagedauto.tile.TileCraftingProxy;

public class BlockCraftingProxy extends BlockBase {

	public static final BlockCraftingProxy INSTANCE = new BlockCraftingProxy();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packagedauto:crafting_proxy");
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:crafting_proxy#normal");

	protected BlockCraftingProxy() {
		super(Material.IRON);
		setHardness(15F);
		setResistance(25F);
		setSoundType(SoundType.METAL);
		setTranslationKey("packagedauto.crafting_proxy");
		setRegistryName("packagedauto:crafting_proxy");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TileCraftingProxy();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(playerIn.isSneaking()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if(tileentity instanceof TileCraftingProxy) {
				if(!worldIn.isRemote) {
					((TileCraftingProxy)tileentity).sendPreview((EntityPlayerMP)playerIn);
				}
				return true;
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
	}
}
