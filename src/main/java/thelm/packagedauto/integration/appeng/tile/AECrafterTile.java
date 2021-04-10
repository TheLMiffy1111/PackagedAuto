package thelm.packagedauto.integration.appeng.tile;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.me.helpers.MachineSource;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import thelm.packagedauto.integration.appeng.networking.BaseGridBlock;
import thelm.packagedauto.tile.CrafterTile;

public class AECrafterTile extends CrafterTile implements IGridHost, IActionHost {

	public BaseGridBlock<AECrafterTile> gridBlock;
	public MachineSource source;
	public IGridNode gridNode;

	public AECrafterTile() {
		super();
		gridBlock = new BaseGridBlock<>(this);
		source = new MachineSource(this);
	}

	@Override
	public void tick() {
		super.tick();
		if(drawMEEnergy && !world.isRemote && world.getGameTime() % 8 == 0 && getActionableNode().isActive()) {
			chargeMEEnergy();
		}
	}

	@Override
	public void remove() {
		super.remove();
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	@Override
	public void setPlacer(PlayerEntity placer) {
		placerID = Api.instance().registries().players().getID(placer);
	}

	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
		world.destroyBlock(pos, true);
	}

	@Override
	public IGridNode getActionableNode() {
		if(gridNode == null && world != null && !world.isRemote) {
			gridNode = Api.instance().grid().createGridNode(gridBlock);
			gridNode.setPlayerID(placerID);
			gridNode.updateState();
		}
		return gridNode;
	}

	protected void chargeMEEnergy() {
		IGrid grid = getActionableNode().getGrid();
		if(grid == null) {
			return;
		}
		IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
		if(energyGrid == null) {
			return;
		}
		double energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored())/2D;
		double canExtract = energyGrid.extractAEPower(energyRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		double extract = Math.round(canExtract*2)/2D;
		energyStorage.receiveEnergy((int)Math.round(energyGrid.extractAEPower(extract, Actionable.MODULATE, PowerMultiplier.CONFIG)*2), false);
	}

	@Override
	protected void ejectItems() {
		if(getActionableNode().isActive()) {
			IGrid grid = getActionableNode().getGrid();
			if(grid == null) {
				return;
			}
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			if(storageGrid == null) {
				return;
			}
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			if(energyGrid == null) {
				return;
			}
			IItemStorageChannel storageChannel = Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(storageChannel);
			int endIndex = isWorking ? 9 : 0;
			for(int i = 9; i >= endIndex; --i) {
				ItemStack is = itemHandler.getStackInSlot(i);
				if(is.isEmpty()) {
					continue;
				}
				IAEItemStack stack = storageChannel.createStack(is);
				IAEItemStack rem = Api.instance().storage().poweredInsert(energyGrid, inventory, stack, source, Actionable.MODULATE);
				if(rem == null || rem.getStackSize() == 0) {
					itemHandler.setStackInSlot(i, ItemStack.EMPTY);
				}
				else if(rem.getStackSize() < stack.getStackSize()) {
					itemHandler.setStackInSlot(i, rem.createItemStack());
				}
			}
		}
		super.ejectItems();
	}

	@Override
	public void read(BlockState blockState, CompoundNBT nbt) {
		super.read(blockState, nbt);
		if(world != null && nbt.contains("Node")) {
			getActionableNode().loadFromNBT("Node", nbt);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		if(gridNode != null) {
			gridNode.saveToNBT("Node", nbt);
		}
		return nbt;
	}
}
