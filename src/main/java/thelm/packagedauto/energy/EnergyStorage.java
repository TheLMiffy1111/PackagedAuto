package thelm.packagedauto.energy;

import thelm.packagedauto.network.packet.PacketSyncEnergy;
import thelm.packagedauto.tile.TileBase;

public class EnergyStorage extends cofh.api.energy.EnergyStorage {

	public final TileBase tile;
	public int prevEnergy;

	public EnergyStorage(TileBase tile, int capacity) {
		this(tile, capacity, capacity, capacity, 0);
	}

	public EnergyStorage(TileBase tile, int capacity, int maxTransfer) {
		this(tile, capacity, maxTransfer, maxTransfer, 0);
	}

	public EnergyStorage(TileBase tile, int capacity, int maxReceive, int maxExtract) {
		this(tile, capacity, maxReceive, maxExtract, 0);
	}

	public EnergyStorage(TileBase tile, int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract);
		this.energy = energy;
		this.tile = tile;
	}

	public void updateIfChanged() {
		int currentEnergy = getEnergyStored();
		if(!tile.getWorldObj().isRemote && prevEnergy != currentEnergy) {
			PacketSyncEnergy.syncEnergy(tile.xCoord, tile.yCoord, tile.zCoord, currentEnergy, tile.getWorldObj().provider.dimensionId, 8);
			tile.markDirty();
		}
		prevEnergy = currentEnergy;
	}
}
