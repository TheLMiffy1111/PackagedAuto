package thelm.packagedauto.integration.appeng.tile;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.appeng.networking.BaseGridBlock;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternDetails;
import thelm.packagedauto.tile.PackagerExtensionTile;

public class AEPackagerExtensionTile extends PackagerExtensionTile implements IGridHost, IActionHost, ICraftingProvider {

	public BaseGridBlock<AEPackagerExtensionTile> gridBlock;
	public MachineSource source;
	public IGridNode gridNode;

	public AEPackagerExtensionTile() {
		super();
		gridBlock = new BaseGridBlock<>(this);
		source = new MachineSource(this);
		gridBlock.flags.remove(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public void tick() {
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % 8 == 0 && getActionableNode().isActive()) {
			chargeMEEnergy();
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
		level.destroyBlock(worldPosition, true);
	}

	@Override
	public IGridNode getActionableNode() {
		if(gridNode == null && level != null && !level.isClientSide) {
			gridNode = Api.instance().grid().createGridNode(gridBlock);
			gridNode.setPlayerID(placerID);
			gridNode.updateState();
		}
		return gridNode;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
		if(!isBusy() && patternDetails instanceof PackageCraftingPatternDetails) {
			PackageCraftingPatternDetails pattern = (PackageCraftingPatternDetails)patternDetails;
			ItemStack slotStack = itemHandler.getStackInSlot(9);
			ItemStack outset = pattern.pattern.getOutput();
			if(slotStack.isEmpty() || slotStack.getItem() == outset.getItem() && ItemStack.tagMatches(slotStack, outset) && slotStack.getCount()+1 <= outset.getMaxStackSize()) {
				currentPattern = pattern.pattern;
				lockPattern = true;
				for(int i = 0; i < table.getContainerSize() && i < 9; ++i) {
					itemHandler.setStackInSlot(i, table.getItem(i).copy());
				}
				return true;
			}
		}
		else if(!isBusy()) {
			ItemStack slotStack = itemHandler.getStackInSlot(9);
			ItemStack outset = patternDetails.getOutputs().get(0).createItemStack();
			if(outset.getItem() instanceof IPackageItem && (slotStack.isEmpty() || slotStack.getItem() == outset.getItem() && ItemStack.tagMatches(slotStack, outset) && slotStack.getCount()+1 <= outset.getMaxStackSize())) {
				IPackageItem packageItem = (IPackageItem)outset.getItem();
				currentPattern = packageItem.getRecipeInfo(outset).getPatterns().get(packageItem.getIndex(outset));
				lockPattern = true;
				for(int i = 0; i < table.getContainerSize() && i < 9; ++i) {
					itemHandler.setStackInSlot(i, table.getItem(i).copy());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return isWorking || !itemHandler.getStacks().subList(0, 9).stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ItemStack listStack = itemHandler.getStackInSlot(10);
		for(IPackagePattern pattern : patternList) {
			craftingTracker.addCraftingOption(this, new PackageCraftingPatternDetails(listStack, pattern).toAEInternal(level));
		}
	}

	@Override
	protected void ejectItem() {
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
			IAEItemStack stack = storageChannel.createStack(itemHandler.getStackInSlot(9));
			IAEItemStack rem = Api.instance().storage().poweredInsert(energyGrid, inventory, stack, source, Actionable.MODULATE);
			if(rem == null || rem.getStackSize() == 0) {
				itemHandler.setStackInSlot(9, ItemStack.EMPTY);
			}
			else if(rem.getStackSize() < stack.getStackSize()) {
				itemHandler.setStackInSlot(9, rem.createItemStack());
			}
		}
		else {
			super.ejectItem();
		}
	}

	@Override
	public void postPatternChange() {
		if(getActionableNode() != null) {
			IGrid grid = getActionableNode().getGrid();
			if(grid == null) {
				return;
			}
			grid.postEvent(new MENetworkCraftingPatternChange(this, getActionableNode()));
		}
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
	public void load(BlockState blockState, CompoundNBT nbt) {
		super.load(blockState, nbt);
		if(level != null && nbt.contains("Node")) {
			getActionableNode().loadFromNBT("Node", nbt);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		super.save(nbt);
		if(gridNode != null) {
			gridNode.saveToNBT("Node", nbt);
		}
		return nbt;
	}
}
