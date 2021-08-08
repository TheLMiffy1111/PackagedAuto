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
	public GridBlockTileBase<TILE> gridBlock;
	public MachineSource source;
	public IGridNode gridNode;
	private NBTTagCompound data = null;

	public HostHelperTile(TILE tile) {
		this.tile = tile;
		source = new MachineSource(tile);
		gridBlock = new GridBlockTileBase<>(tile);
	}

	public IGridNode getNode() {
		if(gridNode == null && tile.hasWorld() && !tile.getWorld().isRemote) {
			gridNode = AEApi.instance().grid().createGridNode(gridBlock);
			this.readFromNBT( this.data );
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
		return tile.hasWorld() && getNode().isActive();
	}

	public void readFromNBT(NBTTagCompound nbt) {
		this.data = nbt;
		if (tile.hasWorld() && data != null && data.hasKey("Node")) {
			getNode().loadFromNBT("Node", data);
			data = null;
		} else if (this.gridNode != null && this.tile.getPlacerID() != -1) {
			this.gridNode.setPlayerID(this.tile.getPlacerID());
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if(gridNode != null) {
			gridNode.saveToNBT("Node", nbt);
		}
		return nbt;
	}
}
