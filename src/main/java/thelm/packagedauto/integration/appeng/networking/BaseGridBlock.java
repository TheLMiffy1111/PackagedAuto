package thelm.packagedauto.integration.appeng.networking;

import java.util.EnumSet;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import thelm.packagedauto.tile.BaseTile;

public class BaseGridBlock<T extends BaseTile & IGridHost> implements IGridBlock {

	public final T tile;
	public double idlePower = 1;
	public final EnumSet<GridFlags> flags = EnumSet.of(GridFlags.REQUIRE_CHANNEL);

	public BaseGridBlock(T tile) {
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
	public EnumSet<Direction> getConnectableSides() {
		return EnumSet.allOf(Direction.class);
	}

	@Override
	public IGridHost getMachine() {
		return tile;
	}

	@Override
	public void gridChanged() {
		if(tile.getLevel() != null) {
			tile.getLevel().updateNeighborsAt(tile.getBlockPos(), Blocks.AIR);
		}
	}

	@Override
	public ItemStack getMachineRepresentation() {
		if(tile != null) {
			return new ItemStack(tile.getBlockState().getBlock(), 1);
		}
		return ItemStack.EMPTY;
	}
}
