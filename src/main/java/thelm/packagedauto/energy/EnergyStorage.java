package thelm.packagedauto.energy;

import net.minecraft.nbt.CompoundNBT;
import thelm.packagedauto.network.packet.SyncEnergyPacket;
import thelm.packagedauto.tile.BaseTile;

public class EnergyStorage extends net.minecraftforge.energy.EnergyStorage {

	public final BaseTile tile;
	public int prevEnergy;

	public EnergyStorage(BaseTile tile, int capacity) {
		this(tile, capacity, capacity, capacity, 0);
	}

	public EnergyStorage(BaseTile tile, int capacity, int maxTransfer) {
		this(tile, capacity, maxTransfer, maxTransfer, 0);
	}

	public EnergyStorage(BaseTile tile, int capacity, int maxReceive, int maxExtract) {
		this(tile, capacity, maxReceive, maxExtract, 0);
	}

	public EnergyStorage(BaseTile tile, int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
		this.tile = tile;
	}

	public EnergyStorage read(CompoundNBT nbt) {
		energy = nbt.getInt("Energy");
		if(energy > capacity) {
			energy = capacity;
		}
		return this;
	}

	public CompoundNBT write(CompoundNBT nbt) {
		if(energy < 0) {
			energy = 0;
		}
		nbt.putInt("Energy", energy);
		return nbt;
	}

	public EnergyStorage setCapacity(int capacity) {
		this.capacity = capacity;
		if(energy > capacity) {
			energy = capacity;
		}
		return this;
	}

	public EnergyStorage setMaxTransfer(int maxTransfer) {
		setMaxReceive(maxTransfer);
		setMaxExtract(maxTransfer);
		return this;
	}

	public EnergyStorage setMaxReceive(int maxReceive) {
		this.maxReceive = maxReceive;
		return this;
	}

	public EnergyStorage setMaxExtract(int maxExtract) {
		this.maxExtract = maxExtract;
		return this;
	}

	public int getMaxReceive() {
		return maxReceive;
	}

	public int getMaxExtract() {
		return maxExtract;
	}

	public void setEnergyStored(int energy) {
		boolean flag = !tile.getWorld().isRemote && this.energy != energy;
		this.energy = energy;
		if(this.energy > capacity) {
			this.energy = capacity;
		}
		else if(this.energy < 0) {
			this.energy = 0;
		}
	}

	public void modifyEnergyStored(int energy) {
		this.energy += energy;
		if(this.energy > capacity) {
			this.energy = capacity;
		}
		else if(this.energy < 0) {
			this.energy = 0;
		}
	}

	public void updateIfChanged() {
		int currentEnergy = getEnergyStored();
		if(!tile.getWorld().isRemote && prevEnergy != currentEnergy) {
			SyncEnergyPacket.syncEnergy(tile.getPos(), currentEnergy, tile.getWorld().getDimensionKey(), 8);
			tile.markDirty();
		}
		prevEnergy = currentEnergy;
	}
}
