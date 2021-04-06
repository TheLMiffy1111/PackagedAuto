package thelm.packagedauto.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.BaseItemHandler;

public abstract class BaseTile extends TileEntity implements INamedContainerProvider, INameable {

	protected BaseItemHandler<?> itemHandler = new BaseItemHandler<>(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public ITextComponent customName = null;
	protected int placerID = -1;

	public BaseTile(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}

	public BaseItemHandler<?> getItemHandler() {
		return itemHandler;
	}

	public void setItemHandler(BaseItemHandler<?> itemHandler) {
		this.itemHandler = itemHandler;
	}

	public EnergyStorage getEnergyStorage() {
		return energyStorage;
	}

	public void setEnergyStorage(EnergyStorage energyStorage) {
		this.energyStorage = energyStorage;
	}

	public void setPlacer(PlayerEntity placer) {

	}

	public int getPlacerID() {
		return placerID;
	}

	@Override
	public ITextComponent getName() {
		return customName != null ? customName : getDefaultName();
	}

	@Override
	public ITextComponent getDisplayName() {
		return getName();
	}

	public void setCustomName(ITextComponent name) {
		customName = name;
	}

	protected abstract ITextComponent getDefaultName();

	@Override
	public void read(BlockState blockState, CompoundNBT nbt) {
		super.read(blockState, nbt);
		readSync(nbt);
		itemHandler.read(nbt);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		writeSync(nbt);
		itemHandler.write(nbt);
		return nbt;
	}

	public void readSync(CompoundNBT nbt) {
		if(nbt.contains("Name")) {
			customName = ITextComponent.Serializer.getComponentFromJson(nbt.getString("Name"));
		}
		energyStorage.read(nbt);
	}

	public CompoundNBT writeSync(CompoundNBT nbt) {
		if(customName != null) {
			nbt.putString("Name", ITextComponent.Serializer.toJson(customName));
		}
		energyStorage.write(nbt);
		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readSync(pkt.getNbtCompound());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, -10, getUpdateTag());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		readSync(tag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbt = super.getUpdateTag();
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		writeSync(nbt);
		return nbt;
	}

	public void syncTile(boolean rerender) {
		if(world != null && world.isBlockLoaded(pos)) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2 + (rerender ? 4 : 0));
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction direction) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return LazyOptional.of(()->(T)itemHandler.getWrapperForDirection(direction));
		}
		return super.getCapability(capability, direction);
	}
}
