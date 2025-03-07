package thelm.packagedauto.integration.appeng.blockentity;

import appeng.api.config.Actionable;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.block.entity.DistributorBlockEntity;

public class AEDistributorBlockEntity extends DistributorBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AEDistributorBlockEntity>, IActionHost {

	public boolean firstTick = true;
	public IActionSource source;
	public IManagedGridNode gridNode;

	public AEDistributorBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = IActionSource.ofMachine(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			getMainNode().create(level, worldPosition);
		}
		super.tick();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	@Override
	public IGridNode getGridNode(Direction dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(Direction dir) {
		return AECableType.SMART;
	}

	@Override
	public void onSecurityBreak(AEDistributorBlockEntity nodeOwner, IGridNode node) {
		level.destroyBlock(worldPosition, true);
	}

	@Override
	public void onSaveChanges(AEDistributorBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("Node");
			gridNode.setVisualRepresentation(DistributorBlock.INSTANCE);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setIdlePowerUsage(1);
			gridNode.setInWorldNode(true);
			if(ownerUUID != null && level instanceof ServerLevel) {
				gridNode.setOwningPlayerId(IPlayerRegistry.getMapping(level).getPlayerId(ownerUUID));
			}
		}
		return gridNode;
	}

	@Override
	public IGridNode getActionableNode() {
		return getMainNode().getNode();
	}

	@Override
	public void ejectItems() {
		if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IStorageService storageService = grid.getStorageService();
			IEnergyService energyService = grid.getEnergyService();
			MEStorage inventory = storageService.getInventory();
			for(int i = 0; i < 81; ++i) {
				if(pending.containsKey(i)) {
					ItemStack is = pending.get(i);
					AEItemKey key = AEItemKey.of(is);
					int count = is.getCount();
					int inserted = (int)StorageHelper.poweredInsert(energyService, inventory, key, count, source, Actionable.MODULATE);
					if(inserted == count) {
						pending.remove(i);
					}
					else {
						pending.put(i, key.toStack(count-inserted));
					}
				}
			}
		}
		super.ejectItems();
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if(nbt.contains("Node")) {
			getMainNode().loadFromNBT(nbt);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if(gridNode != null) {
			gridNode.saveToNBT(nbt);
		}
	}
}
