package thelm.packagedauto.tile;

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import thelm.packagedauto.client.gui.IGuiProvider;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.InventoryTileBase;

public abstract class TileBase extends TileEntity implements IWorldNameable, IGuiProvider {

	protected InventoryTileBase inventory = new InventoryTileBase(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public String customName = "";
	protected UUID ownerUUID = null;
	@Deprecated
	protected int placerID = -1;

	public InventoryTileBase getInventory() {
		return inventory;
	}

	public void setInventory(InventoryTileBase inventory) {
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
	public String getName() {
		return customName.isEmpty() ? getLocalizedName() : customName;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public boolean hasCustomName() {
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
		if(nbt.hasUniqueId("OwnerUUID")) {
			ownerUUID = nbt.getUniqueId("OwnerUUID");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeSyncNBT(nbt);
		inventory.writeToNBT(nbt);
		if(ownerUUID != null) {
			nbt.setUniqueId("OwnerUUID", ownerUUID);
		}
		return nbt;
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
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readSyncNBT(pkt.getNbtCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -10, getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readSyncNBT(tag);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		nbt.removeTag("ForgeData");
		nbt.removeTag("ForgeCaps");
		writeSyncNBT(nbt);
		return nbt;
	}

	public void syncTile(boolean rerender) {
		if(world != null && world.isBlockLoaded(pos)) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2 + (rerender ? 4 : 0));
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
				capability == CapabilityEnergy.ENERGY && energyStorage.getMaxEnergyStored() > 0 ||
				super.hasCapability(capability, from);
	}

	protected IItemHandler unsidedItemHandler = null;
	protected EnumMap<EnumFacing, IItemHandler> sidedItemHandlers = new EnumMap<>(EnumFacing.class);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if(inventory instanceof ISidedInventory && facing != null) {
				if(sidedItemHandlers.containsKey(facing)) {
					return (T)sidedItemHandlers.get(facing);
				}
				IItemHandler handler = new SidedInvWrapper(inventory, facing);
				sidedItemHandlers.put(facing, handler);
				return (T)handler;
			}
			else {
				if(unsidedItemHandler != null) {
					return (T)unsidedItemHandler;
				}
				return (T)(unsidedItemHandler = new InvWrapper(inventory));
			}
		}
		else if(capability == CapabilityEnergy.ENERGY && energyStorage.getMaxEnergyStored() > 0) {
			return (T)energyStorage;
		}
		return super.getCapability(capability, facing);
	}
}
