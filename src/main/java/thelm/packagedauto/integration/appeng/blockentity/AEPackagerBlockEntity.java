package thelm.packagedauto.integration.appeng.blockentity;

import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternDetails;

public class AEPackagerBlockEntity extends PackagerBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AEPackagerBlockEntity>, IActionHost, ICraftingProvider {

	public IActionSource source;
	public IManagedGridNode gridNode;

	public AEPackagerBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = IActionSource.ofMachine(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			getMainNode().create(level, worldPosition);
		}
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % refreshInterval == 0) {
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
	public IGridNode getGridNode(Direction dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(Direction dir) {
		return AECableType.SMART;
	}

	@Override
	public void onSaveChanges(AEPackagerBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	@Override
	public void onStateChanged(AEPackagerBlockEntity nodeOwner, IGridNode node, State state) {
		if(state == State.POWER || state == State.CHANNEL) {
			postPatternChange();
		}
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("node");
			gridNode.setVisualRepresentation(PackagedAutoBlocks.PACKAGER);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setFlags(GridFlags.REQUIRE_CHANNEL);
			gridNode.addService(ICraftingProvider.class, this);
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
	public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
		if(getMainNode().isActive() && !isBusy() && patternDetails instanceof PackageCraftingPatternDetails pattern) {
			ItemStack outputStack = pattern.pattern.getOutput();
			List<ItemStack> inputs = pattern.pattern.getInputs();
			if(canPushPattern()) {
				ItemStack slotStack = itemHandler.getStackInSlot(9);
				if(slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize()) {
					currentPattern = pattern.pattern;
					lockPattern = true;
					for(int i = 0; i < inputs.size(); ++i) {
						itemHandler.setStackInSlot(i, inputs.get(i).copy());
					}
					return true;
				}
			}
			for(BlockPos posP : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
				BlockEntity blockEntity = level.getBlockEntity(posP);
				if(blockEntity instanceof AEPackagerExtensionBlockEntity extension) {
					if(extension.packager == this && extension.getMainNode().isActive() && getMainNode().getGrid() == extension.getMainNode().getGrid() && extension.canPushPattern()) {
						ItemStack slotStack = extension.getItemHandler().getStackInSlot(9);
						if(slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize()) {
							extension.currentPattern = pattern.pattern;
							extension.lockPattern = true;
							for(int i = 0; i < inputs.size(); ++i) {
								extension.getItemHandler().setStackInSlot(i, inputs.get(i).copy());
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		if(canPushPattern()) {
			return false;
		}
		for(BlockPos posP : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
			BlockEntity blockEntity = level.getBlockEntity(posP);
			if(blockEntity instanceof AEPackagerExtensionBlockEntity extension) {
				if(extension.packager == this && getMainNode().getGrid() == extension.getMainNode().getGrid() && extension.canPushPattern()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public List<IPatternDetails> getAvailablePatterns() {
		if(getMainNode().isActive()) {
			RegistryAccess registry = level.registryAccess();
			return patternList.stream().<IPatternDetails>map(pattern->new PackageCraftingPatternDetails(pattern, registry)).toList();
		}
		else {
			return List.of();
		}
	}

	@Override
	protected void ejectItem() {
		if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IStorageService storageService = grid.getStorageService();
			IEnergyService energyService = grid.getEnergyService();
			MEStorage inventory = storageService.getInventory();
			ItemStack is = itemHandler.getStackInSlot(9);
			AEItemKey key = AEItemKey.of(is);
			int count = is.getCount();
			int inserted = (int)StorageHelper.poweredInsert(energyService, inventory, key, count, source, Actionable.MODULATE);
			if(inserted == count) {
				itemHandler.setStackInSlot(9, ItemStack.EMPTY);
			}
			else {
				itemHandler.setStackInSlot(9, key.toStack(count-inserted));
			}
		}
		else {
			super.ejectItem();
		}
	}

	@Override
	public void postPatternChange() {
		ICraftingProvider.requestUpdate(getMainNode());
	}

	protected void chargeMEEnergy() {
		if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IEnergyService energyService = grid.getEnergyService();
			double conversion = PowerUnit.FE.convertTo(PowerUnit.AE, 1);
			int request = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());
			double available = energyService.extractAEPower((request+0.5)*conversion, Actionable.SIMULATE, PowerMultiplier.CONFIG);
			int extract = (int)(available/conversion);
			energyService.extractAEPower(extract*conversion, Actionable.MODULATE, PowerMultiplier.CONFIG);
			energyStorage.receiveEnergy(extract, false);
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		if(nbt.contains("node")) {
			getMainNode().loadFromNBT(nbt);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		if(gridNode != null) {
			gridNode.saveToNBT(nbt);
		}
	}
}
