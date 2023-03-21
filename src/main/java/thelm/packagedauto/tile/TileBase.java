package thelm.packagedauto.tile;

import java.util.UUID;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.client.gui.IGuiProvider;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.InventoryBase;

public abstract class TileBase extends TileEntity implements ISidedInventory, IEnergyReceiver, IGuiProvider {

	protected InventoryBase inventory = new InventoryBase(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public String customName = "";
	protected UUID ownerUUID = null;

	public InventoryBase getInventory() {
		return inventory;
	}

	public void setInventory(InventoryBase inventory) {
		this.inventory = inventory;
	}

	public EnergyStorage getEnergyStorage() {
		return energyStorage;
	}

	public void setEnergyStorage(EnergyStorage energyStorage) {
		this.energyStorage = energyStorage;
	}

	public void setOwner(EntityPlayer owner) {
		ownerUUID = owner.getUniqueID();
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	protected abstract String getLocalizedName();

	@Override
	public String getInventoryName() {
		return customName.isEmpty() ? getLocalizedName() : customName;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return !customName.isEmpty();
	}

	public void setCustomName(String name) {
		if(!name.isEmpty()) {
			customName = name;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readSyncNBT(nbt);
		inventory.readFromNBT(nbt);
		ownerUUID = null;
		if(nbt.hasKey("OwnerUUIDMost") && nbt.hasKey("OwnerUUIDLeast")) {
			ownerUUID = new UUID(nbt.getLong("OwnerUUIDMost"), nbt.getLong("OwnerUUIDLeast"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeSyncNBT(nbt);
		inventory.writeToNBT(nbt);
		if(ownerUUID != null) {
			nbt.setLong("OwnerUUIDMost", ownerUUID.getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", ownerUUID.getLeastSignificantBits());
		}
	}

	public void readSyncNBT(NBTTagCompound nbt) {
		if(nbt.hasKey("Name")) {
			customName = nbt.getString("Name");
		}
		energyStorage.readFromNBT(nbt);
	}

	public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
		if(!customName.isEmpty()) {
			nbt.setString("Name", customName);
		}
		energyStorage.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readSyncNBT(pkt.func_148857_g());
	}

	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket() {
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -10, writeSyncNBT(new NBTTagCompound()));
	}

	public void syncTile() {
		if(worldObj != null && worldObj.blockExists(xCoord, yCoord, zCoord)) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inventory.decrStackSize(index, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		return inventory.getStackInSlotOnClosing(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventory.setInventorySlotContents(index, stack);		
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inventory.isItemValidForSlot(index, stack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return inventory.getAccessibleSlotsFromSide(side);
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return inventory.canInsertItem(index, stack, side);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return inventory.canExtractItem(index, stack, side);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection side) {
		return energyStorage.getMaxEnergyStored() > 0;
	}

	@Override
	public int receiveEnergy(ForgeDirection side, int maxReceive, boolean simulate) {
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection side) {
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection side) {
		return energyStorage.getMaxEnergyStored();
	}
}
