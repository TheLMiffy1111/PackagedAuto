package thelm.packagedauto.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.FluidPackageFillerItemHandler;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.util.MiscHelper;

public class FluidPackageFillerBlockEntity extends BaseBlockEntity implements ISettingsCloneable {

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static int refreshInterval = 4;

	public final ResourceHandler<ItemResource> packageSlot;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public FluidResource currentFluid = FluidResource.EMPTY;
	public int requiredAmount = 100;
	public int amount = 0;
	public int remainingProgress = 0;
	public boolean powered = false;
	public boolean activated = false;

	public FluidPackageFillerBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.FLUID_PACKAGE_FILLER.get(), pos, state);
		setItemHandler(new FluidPackageFillerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		packageSlot = RangedResourceHandler.ofSingleIndex(this::getItemHandler, 1);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.fluid_package_filler");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.fluid_package_filler";
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			updatePowered();
		}
		if(!level.isClientSide()) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isTemplateValid()) {
					finishProcess();
					if(!itemHandler.getStack(1).isEmpty()) {
						ejectItem();
					}
					if(!canStart()) {
						endProcess();
					}
					else {
						startProcess();
					}
				}
			}
			else if(activated) {
				if(canStart()) {
					startProcess();
					tickProcess();
					activated = false;
					isWorking = true;
				}
			}
			chargeEnergy();
			if(level.getGameTime() % refreshInterval == 0) {
				if(!itemHandler.getStack(1).isEmpty()) {
					ejectItem();
				}
			}
		}
	}

	public boolean isTemplateValid() {
		if(currentFluid.isEmpty()) {
			getFluid();
		}
		if(currentFluid.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean canStart() {
		getFluid();
		if(currentFluid.isEmpty()) {
			return false;
		}
		if(!isTemplateValid()) {
			return false;
		}
		ItemStack slotStack = itemHandler.getStack(1);
		ItemStack outputStack = MiscHelper.INSTANCE.tryMakeVolumePackage(currentFluid.toStack(requiredAmount));
		return slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
	}

	protected boolean canFinish() {
		return remainingProgress <= 0 && isTemplateValid();
	}

	protected void getFluid() {
		currentFluid = FluidResource.EMPTY;
		ItemStack template = itemHandler.getStack(0);
		if(template.isEmpty()) {
			return;
		}
		FluidStack stack = FluidUtil.getFirstStackContained(template);
		if(!stack.isEmpty()) {
			currentFluid = FluidResource.of(stack);
		}
	}

	protected void tickProcess() {
		if(amount < requiredAmount) {
			for(Direction direction : Direction.values()) {
				ResourceHandler<FluidResource> fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, worldPosition.relative(direction), direction.getOpposite());
				if(fluidHandler != null) {
					try(Transaction transaction = Transaction.openRoot()) {
						int drained = fluidHandler.extract(currentFluid, requiredAmount-amount, transaction);
						transaction.commit();
						amount += drained;
					}
				}
			}
		}
		if(amount >= requiredAmount) {
			try(Transaction transaction = Transaction.openRoot()) {
				int energy = energyStorage.extract(Math.min(energyUsage, remainingProgress), transaction);
				transaction.commit();
				remainingProgress -= energy;
			}
		}
	}

	protected void finishProcess() {
		if(currentFluid.isEmpty()) {
			getFluid();
		}
		if(currentFluid.isEmpty()) {
			endProcess();
			return;
		}
		if(itemHandler.getStack(1).isEmpty()) {
			itemHandler.setStack(1, MiscHelper.INSTANCE.tryMakeVolumePackage(currentFluid.toStack(requiredAmount)));
		}
		else if(itemHandler.getStack(1).has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
			itemHandler.getStack(1).grow(1);
		}
		endProcess();
	}

	public void startProcess() {
		remainingProgress = energyReq;
		amount = 0;
		sync(false);
		setChanged();
	}

	public void endProcess() {
		remainingProgress = 0;
		amount = 0;
		isWorking = false;
		setChanged();
	}

	protected void ejectItem() {
		for(Direction direction : Direction.values()) {
			BlockPos offsetPos = worldPosition.relative(direction);
			Block block = level.getBlockState(offsetPos).getBlock();
			ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, offsetPos, direction.getOpposite());
			ResourceHandler<FluidResource> fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, offsetPos, direction.getOpposite());
			if(block != PackagedAutoBlocks.UNPACKAGER.get() && itemHandler != null && fluidHandler == null) {
				int moveCount = packageSlot.getAmountAsInt(0);
				if(moveCount > 0) {
					ResourceHandlerUtil.moveStacking(packageSlot, itemHandler, _->true, moveCount, null);
				}
			}
		}
	}

	protected void chargeEnergy() {
		EnergyHandler itemEnergyStorage = ItemAccess.forHandlerIndex(itemHandler, 2).getCapability(Capabilities.Energy.ITEM);
		if(itemEnergyStorage != null) {
			EnergyHandlerUtil.move(itemEnergyStorage, energyStorage, energyStorage.getMaxInsert(), null);
		}
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			if(powered && !isWorking) {
				activated = true;
			}
			sync(false);
			setChanged();
		}
	}

	@Override
	public int getComparatorSignal() {
		if(isWorking) {
			return 1;
		}
		if(!itemHandler.getStack(1).isEmpty()) {
			return 15;
		}
		return 0;
	}

	@Override
	public ISettingsCloneable.Result loadConfig(ValueInput input, Player player) {
		requiredAmount = input.getIntOr("amount_req", 100);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(ValueOutput output, Player player) {
		output.putInt("amount_req", requiredAmount);
		return ISettingsCloneable.Result.success();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		isWorking = input.getBooleanOr("working", false);
		amount = input.getIntOr("amount", 0);
		remainingProgress = input.getIntOr("progress", 0);
		powered = input.getBooleanOr("powered", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("working", isWorking);
		output.putInt("amount", amount);
		output.putInt("progress", remainingProgress);
		output.putBoolean("powered", powered);
	}

	@Override
	protected void loadSync(ValueInput input) {
		super.loadSync(input);
		currentFluid = input.read("fluid", FluidResource.OPTIONAL_CODEC).orElse(FluidResource.EMPTY);
		requiredAmount = input.getIntOr("amount_req", 100);
	}

	@Override
	protected void saveSync(ValueOutput output) {
		super.saveSync(output);
		output.store("fluid", FluidResource.OPTIONAL_CODEC, currentFluid);
		output.putInt("amount_req", requiredAmount);
	}

	@Override
	public void setChanged() {
		if(isWorking && !isTemplateValid()) {
			endProcess();
		}
		super.setChanged();
	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getCapacityAsInt() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getAmountAsInt() / energyStorage.getCapacityAsInt(), scale);
	}

	public int getScaledProgress(int scale) {
		if(remainingProgress <= 0 || energyReq <= 0) {
			return 0;
		}
		return scale * (energyReq-remainingProgress) / energyReq;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new FluidPackageFillerMenu(windowId, inventory, this);
	}
}
