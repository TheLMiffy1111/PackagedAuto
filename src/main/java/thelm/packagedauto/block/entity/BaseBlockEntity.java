package thelm.packagedauto.block.entity;

import java.util.UUID;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.BaseItemResourceHandler;

public abstract class BaseBlockEntity extends BlockEntity implements Nameable, MenuProvider {

	protected BaseItemResourceHandler<?> itemHandler = new BaseItemResourceHandler<>(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public Component customName = null;
	protected UUID ownerUUID = null;

	public BaseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
	}

	public BaseItemResourceHandler<?> getItemHandler() {
		return itemHandler;
	}

	public ResourceHandler<ItemResource> getItemHandler(Direction direction) {
		return itemHandler.getWrapperForDirection(direction);
	}

	public void setItemHandler(BaseItemResourceHandler<?> itemHandler) {
		this.itemHandler = itemHandler;
	}

	public EnergyStorage getEnergyStorage() {
		return energyStorage;
	}

	public EnergyStorage getEnergyStorage(Direction direction) {
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

	@Override
	public Component getCustomName() {
		return customName;
	}

	protected abstract Component getDefaultName();

	public void tick() {}

	public int getComparatorSignal() {
		return ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(itemHandler.getWrapperForDirection(null));
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if(level != null) {
			for(int i = 0; i < itemHandler.size(); ++i) {
				ItemResource resource = itemHandler.getResource(i);
				if(!resource.isEmpty()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resource.toStack(itemHandler.getAmountAsInt(i)));
				}
			}
		}
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		customName = components.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(DataComponents.CUSTOM_NAME, customName);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ownerUUID = input.read("owner_uuid", UUIDUtil.CODEC).orElse(null);
		loadSync(input);
		itemHandler.deserialize(input);
		energyStorage.deserialize(input);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if(ownerUUID != null) {
			output.store("owner_uuid", UUIDUtil.CODEC, ownerUUID);
		}
		saveSync(output);
		itemHandler.serialize(output);
		energyStorage.serialize(output);
	}

	protected void loadSync(ValueInput input) {
		customName = input.read("name", ComponentSerialization.CODEC).orElse(null);
	}

	protected void saveSync(ValueOutput output) {
		if(customName != null) {
			output.store("name", ComponentSerialization.CODEC, customName);
		}
	}

	@Override
	public void onDataPacket(Connection net, ValueInput input) {
		loadSync(input);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		loadSync(input);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		try(ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), LogUtils.getLogger())) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
			saveSync(output);
			return output.buildResult();
		}
	}

	public void sync(boolean rerender) {
		if(level != null && level.isLoaded(worldPosition)) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2 + (rerender ? 4 : 0));
		}
	}

	public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
			baseBlockEntity.tick();
		}
	}
}
