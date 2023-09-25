package thelm.packagedauto.integration.appeng.networking;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import thelm.packagedauto.tile.TileUnpackager;

public class HostHelperTileUnpackager extends HostHelperTile<TileUnpackager> {

	public HostHelperTileUnpackager(TileUnpackager tile) {
		super(tile);
	}

	public void chargeEnergy() {
		if(isActive()) {
			IGrid grid = getNode().getGrid();
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			double conversion = PowerUnits.RF.convertTo(PowerUnits.AE, 1);
			int request = Math.min(tile.getEnergyStorage().getMaxReceive(), tile.getEnergyStorage().getMaxEnergyStored()-tile.getEnergyStorage().getEnergyStored());
			double available = energyGrid.extractAEPower((request+0.5)*conversion, Actionable.SIMULATE, PowerMultiplier.CONFIG);
			int extract = (int)(available/conversion);
			energyGrid.extractAEPower(extract*conversion, Actionable.MODULATE, PowerMultiplier.CONFIG);
			tile.getEnergyStorage().receiveEnergy(extract, false);
		}
	}

	public void postPatternChange() {
		if(isActive()) {
			IGrid grid = getNode().getGrid();
			grid.postEvent(new MENetworkCraftingPatternChange(tile, getNode()));
		}
	}
}
