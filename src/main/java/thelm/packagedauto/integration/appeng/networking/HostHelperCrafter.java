package thelm.packagedauto.integration.appeng.networking;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.tile.TileCrafter;

public class HostHelperCrafter extends HostHelperBase<TileCrafter> {

	public HostHelperCrafter(TileCrafter tile) {
		super(tile);
		gridBlock.flags.remove(GridFlags.REQUIRE_CHANNEL);
	}

	public void ejectItem() {
		IGrid grid = getNode().getGrid();
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
		IStorageHelper storageHelper = AEApi.instance().storage();
		IMEMonitor<IAEItemStack> inventory = storageGrid.getItemInventory();
		int endIndex = tile.isWorking ? 9 : 0;
		for(int i = 9; i >= endIndex; --i) {
			ItemStack is = tile.getInventory().getStackInSlot(i);
			if(is == null) {
				continue;
			}
			IAEItemStack stack = storageHelper.createItemStack(is);
			IAEItemStack rem = AEApi.instance().storage().poweredInsert(energyGrid, inventory, stack, source);
			if(rem == null || rem.getStackSize() == 0) {
				tile.getInventory().setInventorySlotContents(i, null);
			}
			else if(rem.getStackSize() < stack.getStackSize()) {
				tile.getInventory().setInventorySlotContents(i, rem.getItemStack());
			}
		}
	}

	public void chargeEnergy() {
		IGrid grid = getNode().getGrid();
		if(grid == null) {
			return;
		}
		IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
		if(energyGrid == null) {
			return;
		}
		double energyRequest = Math.min(tile.getEnergyStorage().getMaxReceive(), tile.getEnergyStorage().getMaxEnergyStored() - tile.getEnergyStorage().getEnergyStored()) / 2D;
		double canExtract = energyGrid.extractAEPower(energyRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		double extract = Math.round(canExtract*2) / 2D;
		tile.getEnergyStorage().receiveEnergy((int)Math.round(energyGrid.extractAEPower(extract, Actionable.MODULATE, PowerMultiplier.CONFIG)*2), false);
	}
}
