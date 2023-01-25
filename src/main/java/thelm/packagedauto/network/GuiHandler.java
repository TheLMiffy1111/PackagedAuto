package thelm.packagedauto.network;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.client.gui.IGuiProvider;

public class GuiHandler implements IGuiHandler {

	public static final GuiHandler INSTANCE = new GuiHandler();

	protected GuiHandler() {

	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile == null) {
			return null;
		}
		else if(tile instanceof IGuiProvider) {
			return ((IGuiProvider)tile).getClientGuiElement(player);
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile == null) {
			return null;
		}
		else if(tile instanceof IGuiProvider) {
			return ((IGuiProvider)tile).getServerGuiElement(player);
		}
		return null;
	}

	public void launchGui(int ID, EntityPlayer player, World world, int x, int y, int z) {
		player.openGui(PackagedAuto.instance, ID, world, x, y, z);
	}
}
