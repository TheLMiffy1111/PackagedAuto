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
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.tile.TileBase;

public class GridBlockBase<TILE extends TileBase & IGridHost> implements IGridBlock {

	public final TILE tile;
	public double idlePower = 1;
	public final EnumSet<GridFlags> flags = EnumSet.of(GridFlags.REQUIRE_CHANNEL);

	public GridBlockBase(TILE tile) {
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
		return AEColor.Transparent;
	}

	@Override
	public void onGridNotification(GridNotification gridNotification) {}

	@Override
	public void setNetworkStatus(IGrid grid, int usedChannels) {}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides() {
		return EnumSet.allOf(ForgeDirection.class);
	}

	@Override
	public IGridHost getMachine() {
		return tile;
	}

	@Override
	public void gridChanged() {
		if(tile.getWorldObj() != null) {
			tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air);
		}
	}

	@Override
	public ItemStack getMachineRepresentation() {
		if(tile != null) {
			return new ItemStack(tile.getBlockType(), 1, tile.getBlockMetadata());
		}
		return null;
	}
}
