package thelm.packagedauto.energy;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.packet.SyncEnergyPacket;

public class EnergyStorage extends SimpleEnergyHandler {

	public final BaseBlockEntity blockEntity;
	public boolean requireSync;

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity) {
		this(blockEntity, capacity, capacity, capacity, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxTransfer) {
		this(blockEntity, capacity, maxTransfer, maxTransfer, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxInsert, int maxExtract) {
		this(blockEntity, capacity, maxInsert, maxExtract, 0);
	}

	public EnergyStorage(BaseBlockEntity blockEntity, int capacity, int maxInsert, int maxExtract, int energy) {
		super(capacity, maxInsert, maxExtract, energy);
		this.blockEntity = blockEntity;
	}

	public EnergyStorage setCapacity(int capacity) {
		this.capacity = capacity;
		if(energy > capacity) {
			energy = capacity;
		}
		return this;
	}

	public EnergyStorage setMaxTransfer(int maxTransfer) {
		setMaxInsert(maxTransfer);
		setMaxExtract(maxTransfer);
		return this;
	}

	public EnergyStorage setMaxInsert(int maxInsert) {
		this.maxInsert = maxInsert;
		return this;
	}

	public EnergyStorage setMaxExtract(int maxExtract) {
		this.maxExtract = maxExtract;
		return this;
	}

	public int getMaxInsert() {
		return maxInsert;
	}

	public int getMaxExtract() {
		return maxExtract;
	}

	public void setEnergyStored(int energy) {
		if(energy != this.energy) {
			int prevEnergy = this.energy;
			this.energy = energy;
			if(this.energy > capacity) {
				this.energy = capacity;
			}
			else if(this.energy < 0) {
				this.energy = 0;
			}
			onEnergyChanged(prevEnergy);
		}
	}

	public void modifyEnergyStored(int energy) {
		if(energy != 0) {
			int prevEnergy = this.energy;
			this.energy += energy;
			if(this.energy > capacity) {
				this.energy = capacity;
			}
			else if(this.energy < 0) {
				this.energy = 0;
			}
			onEnergyChanged(prevEnergy);
		}
	}

	@Override
	protected void onEnergyChanged(int previousAmount) {
		requireSync = true;
		if(!blockEntity.getLevel().isClientSide()) {
			blockEntity.setChanged();
		}
	}

	public void syncEnergy() {
		if(requireSync && !blockEntity.getLevel().isClientSide()) {
			SyncEnergyPacket.syncEnergy((ServerLevel)blockEntity.getLevel(), blockEntity.getBlockPos(), energy, 8);
		}
		requireSync = false;
	}
}
