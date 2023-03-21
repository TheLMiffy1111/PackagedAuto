package thelm.packagedauto.integration.appeng.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.me.helpers.MachineSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.block.CrafterBlock;
import thelm.packagedauto.block.entity.CrafterBlockEntity;

public class AECrafterBlockEntity extends CrafterBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AECrafterBlockEntity>, IActionHost {

	public boolean firstTick = true;
	public MachineSource source;
	public IManagedGridNode gridNode;

	public AECrafterBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = new MachineSource(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			getMainNode().create(level, worldPosition);
		}
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % 8 == 0 && getActionableNode().isActive()) {
			chargeMEEnergy();
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
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
	public void onSecurityBreak(AECrafterBlockEntity nodeOwner, IGridNode node) {
		level.destroyBlock(worldPosition, true);
	}

	@Override
	public void onSaveChanges(AECrafterBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("Node");
			gridNode.setVisualRepresentation(CrafterBlock.INSTANCE);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setIdlePowerUsage(1);
			gridNode.setInWorldNode(true);
			if(ownerUUID != null) {
				gridNode.setOwningPlayerId(IPlayerRegistry.getMapping(level).getPlayerId(ownerUUID));
			}
		}
		return gridNode;
	}

	@Override
	public IGridNode getActionableNode() {
		return getMainNode().getNode();
	}

	protected void chargeMEEnergy() {
		IGrid grid = getActionableNode().getGrid();
		if(grid == null) {
			return;
		}
		IEnergyService energyService = grid.getService(IEnergyService.class);
		if(energyService == null) {
			return;
		}
		double energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored())/2D;
		double canExtract = energyService.extractAEPower(energyRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		double extract = Math.round(canExtract*2)/2D;
		energyStorage.receiveEnergy((int)Math.round(energyService.extractAEPower(extract, Actionable.MODULATE, PowerMultiplier.CONFIG)*2), false);
	}

	@Override
	protected void ejectItems() {
		if(getActionableNode().isActive()) {
			IGrid grid = getActionableNode().getGrid();
			if(grid == null) {
				return;
			}
			IStorageService storageService = grid.getService(IStorageService.class);
			if(storageService == null) {
				return;
			}
			IEnergyService energyService = grid.getService(IEnergyService.class);
			if(energyService == null) {
				return;
			}
			MEStorage inventory = storageService.getInventory();
			int endIndex = isWorking ? 9 : 0;
			for(int i = 9; i >= endIndex; --i) {
				ItemStack is = itemHandler.getStackInSlot(i);
				if(is.isEmpty()) {
					continue;
				}
				AEItemKey key = AEItemKey.of(is);
				int count = is.getCount();
				int inserted = (int)StorageHelper.poweredInsert(energyService, inventory, key, count, source, Actionable.MODULATE);
				if(inserted == count) {
					itemHandler.setStackInSlot(i, ItemStack.EMPTY);
				}
				else {
					itemHandler.setStackInSlot(i, key.toStack(count-inserted));
				}
			}
		}
		super.ejectItems();
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if(level != null && nbt.contains("Node")) {
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
