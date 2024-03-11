package thelm.packagedauto.integration.appeng.tile;

import com.mojang.authlib.GameProfile;

import appeng.api.IAppEngApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
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
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % 8 == 0) {
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
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(gridNode != null) {
			gridNode.destroy();
		}
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
			IAppEngApi api = Api.instance();
			gridNode = api.grid().createGridNode(gridBlock);
			if(ownerUUID != null) {
				gridNode.setPlayerID(api.registries().players().getID(new GameProfile(ownerUUID, "[UNKNOWN]")));
			}
			gridNode.updateState();
		}
		return gridNode;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
		if(!isBusy() && patternDetails instanceof PackageCraftingPatternDetails) {
			PackageCraftingPatternDetails pattern = (PackageCraftingPatternDetails)patternDetails;
			ItemStack slotStack = itemHandler.getStackInSlot(9);
			ItemStack outputStack = pattern.pattern.getOutput();
			if(slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && ItemStack.tagMatches(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize()) {
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
			ItemStack outputStack = patternDetails.getOutputs().get(0).createItemStack();
			if(outputStack.getItem() instanceof IPackageItem && (slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && ItemStack.tagMatches(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize())) {
				IPackageItem packageItem = (IPackageItem)outputStack.getItem();
				IPackageRecipeInfo recipe = packageItem.getRecipeInfo(outputStack);
				int index = packageItem.getIndex(outputStack);
				if(recipe != null && recipe.validPatternIndex(index)) {
					currentPattern = recipe.getPatterns().get(index);
					lockPattern = true;
					for(int i = 0; i < table.getContainerSize() && i < 9; ++i) {
						itemHandler.setStackInSlot(i, table.getItem(i).copy());
					}
					return true;
				}
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
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
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
		if(getActionableNode().isActive()) {
			IGrid grid = getActionableNode().getGrid();
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			double conversion = PowerUnits.RF.convertTo(PowerUnits.AE, 1);
			int request = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());
			double available = energyGrid.extractAEPower((request+0.5)*conversion, Actionable.SIMULATE, PowerMultiplier.CONFIG);
			int extract = (int)(available/conversion);
			energyGrid.extractAEPower(extract*conversion, Actionable.MODULATE, PowerMultiplier.CONFIG);
			energyStorage.receiveEnergy(extract, false);
		}
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
