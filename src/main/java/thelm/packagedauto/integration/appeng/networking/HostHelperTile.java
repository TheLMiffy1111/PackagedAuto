package thelm.packagedauto.integration.appeng.networking;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.MachineSource;
import net.minecraft.nbt.NBTTagCompound;
import thelm.packagedauto.tile.TileBase;

public class HostHelperTile<TILE extends TileBase & IGridHost & IActionHost> {

	public final TILE tile;
	public GridBlockTileBase gridBlock;
	public MachineSource source;
	public IGridNode gridNode;

	public HostHelperTile(TILE tile) {
		this.tile = tile;
		source = new MachineSource(tile);
		gridBlock = new GridBlockTileBase(tile);
	}

	public IGridNode getNode() {
		if(gridNode == null && !tile.getWorld().isRemote) {
			gridNode = AEApi.instance().grid().createGridNode(gridBlock);
			gridNode.updateState();
		}
		return gridNode;
	}

	public void invalidate() {
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	public boolean isActive() {
		return getNode().isActive();
	}

	public void readFromNBT(NBTTagCompound nbt) {

	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return nbt;
	}
}
