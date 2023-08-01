package thelm.packagedauto.energy;

import net.minecraft.nbt.CompoundTag;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.network.packet.SyncEnergyPacket;

public class EnergyStorage extends net.minecraftforge.energy.EnergyStorage {

	public final BaseBlockEntity blockEntity;
	public int prevEnergy;

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity) {
		this(blockEntity, capacity, capacity, capacity, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxTransfer) {
		this(blockEntity, capacity, maxTransfer, maxTransfer, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxReceive, int maxExtract) {
		this(blockEntity, capacity, maxReceive, maxExtract, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
		this.blockEntity = blockEntity;
	}

	public EnergyStorage read(CompoundTag nbt) {
		energy = nbt.getInt("Energy");
		if(energy > capacity) {
			energy = capacity;
		}
		return this;
	}

	public void save(CompoundTag nbt) {
		if(energy < 0) {
			energy = 0;
		}
		nbt.putInt("Energy", energy);
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
		if(!blockEntity.getLevel().isClientSide && prevEnergy != currentEnergy) {
			SyncEnergyPacket.syncEnergy(blockEntity.getBlockPos(), currentEnergy, blockEntity.getLevel().dimension(), 8);
			blockEntity.setChanged();
		}
		prevEnergy = currentEnergy;
	}
}
