package thelm.packagedauto.integration.appeng.networking;

import java.util.EnumSet;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import thelm.packagedauto.tile.TileBase;

public class GridBlockTileBase<TILE extends TileBase & IGridHost> implements IGridBlock {

	public final TILE tile;
	public double idlePower = 1;
	public final EnumSet<GridFlags> flags = EnumSet.of(GridFlags.REQUIRE_CHANNEL);

	public GridBlockTileBase(TILE tile) {
		this.tile = tile;
	}

	@Override
	public double getIdlePowerUsage() {
		return idlePower;
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return flags;
	}

	@Override
	public boolean isWorldAccessible() {
		return true;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(tile);
	}

	@Override
	public AEColor getGridColor() {
		return AEColor.TRANSPARENT;
	}

	@Override
	public void onGridNotification(GridNotification gridNotification) {}

	@Override
	public void setNetworkStatus(IGrid grid, int usedChannels) {}

	@Override
	public EnumSet<EnumFacing> getConnectableSides() {
		return EnumSet.allOf(EnumFacing.class);
	}

	@Override
	public IGridHost getMachine() {
		return tile;
	}

	@Override
	public void gridChanged() {
		if(tile.getWorld() != null) {
			tile.getWorld().notifyNeighborsOfStateChange(tile.getPos(), Blocks.AIR, true);
		}
	}

	@Override
	public ItemStack getMachineRepresentation() {
		if(tile != null) {
			return new ItemStack(tile.getBlockType(), 1, tile.getBlockMetadata());
		}
		return ItemStack.EMPTY;
	}
}
