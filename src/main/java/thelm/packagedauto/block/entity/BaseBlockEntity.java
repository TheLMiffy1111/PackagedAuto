package thelm.packagedauto.block.entity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.BaseItemHandler;

public abstract class BaseBlockEntity extends BlockEntity implements Nameable, MenuProvider {

	protected BaseItemHandler<?> itemHandler = new BaseItemHandler<>(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public Component customName = null;
	protected UUID ownerUUID = null;
	@Deprecated
	protected int placerID = -1;

	public BaseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
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

	public void setOwner(Player owner) {
		ownerUUID = owner.getUUID();
	}

	@Override
	public Component getName() {
		return customName != null ? customName : getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	public void setCustomName(Component name) {
		customName = name;
	}

	protected abstract Component getDefaultName();

	public void tick() {

	}

	public int getComparatorSignal() {
		return ItemHandlerHelper.calcRedstoneFromInventory(itemHandler.getWrapperForDirection(null));
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		loadSync(nbt);
		itemHandler.load(nbt);
		ownerUUID = null;
		if(nbt.hasUUID("OwnerUUID")) {
			ownerUUID = nbt.getUUID("OwnerUUID");
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		saveSync(nbt);
		itemHandler.save(nbt);
		if(ownerUUID != null) {
			nbt.putUUID("OwnerUUID", ownerUUID);
		}
	}

	public void loadSync(CompoundTag nbt) {
		if(nbt.contains("Name")) {
			customName = Component.Serializer.fromJson(nbt.getString("Name"));
		}
		energyStorage.read(nbt);
	}

	public CompoundTag saveSync(CompoundTag nbt) {
		if(customName != null) {
			nbt.putString("Name", Component.Serializer.toJson(customName));
		}
		energyStorage.save(nbt);
		return nbt;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		loadSync(pkt.getTag());
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		loadSync(tag);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = super.getUpdateTag();
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		saveSync(nbt);
		return nbt;
	}

	public void sync(boolean rerender) {
		if(level != null && level.isLoaded(worldPosition)) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2 + (rerender ? 4 : 0));
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction direction) {
		if(capability == ForgeCapabilities.ITEM_HANDLER) {
			return LazyOptional.of(()->(T)itemHandler.getWrapperForDirection(direction));
		}
		else if(capability == ForgeCapabilities.ENERGY && energyStorage.getMaxEnergyStored() > 0) {
			return LazyOptional.of(()->(T)energyStorage);
		}
		return super.getCapability(capability, direction);
	}

	public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
			baseBlockEntity.tick();
		}
	}
}
