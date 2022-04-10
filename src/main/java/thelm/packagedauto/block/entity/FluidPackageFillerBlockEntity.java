package thelm.packagedauto.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.FluidPackageFillerItemHandler;
import thelm.packagedauto.item.VolumePackageItem;

public class FluidPackageFillerBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<FluidPackageFillerBlockEntity> TYPE_INSTANCE = (BlockEntityType<FluidPackageFillerBlockEntity>)BlockEntityType.Builder.
			of(EncoderBlockEntity::new, FluidPackageFillerBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:fluid_package_filler");

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public FluidStack currentFluid = FluidStack.EMPTY;
	public int requiredAmount = 0;
	public int amount = 0;
	public int remainingProgress = 0;
	public boolean powered = false;

	public FluidPackageFillerBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new FluidPackageFillerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("block.packagedauto.fluid_package_filler");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			updatePowered();
		}
		if(!level.isClientSide) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isTemplateValid()) {
					energyStorage.receiveEnergy(Math.abs(remainingProgress), false);
					finishProcess();
					if(!itemHandler.getStackInSlot(9).isEmpty()) {
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
			else if(level.getGameTime() % 8 == 0) {
				if(canStart()) {
					startProcess();
					tickProcess();
					isWorking = true;
				}
			}
			chargeEnergy();
			if(level.getGameTime() % 8 == 0) {
				if(!itemHandler.getStackInSlot(9).isEmpty()) {
					ejectItem();
				}
			}
			energyStorage.updateIfChanged();
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
		ItemStack slotStack = itemHandler.getStackInSlot(1);
		ItemStack outputStack = VolumePackageItem.tryMakeVolumePackage(currentFluid);
		return slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && ItemStack.isSameItemSameTags(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
	}

	protected boolean canFinish() {
		return remainingProgress <= 0 && isTemplateValid();
	}

	protected void getFluid() {
		if(!powered) {
			return;
		}
		ItemStack template = itemHandler.getStackInSlot(0);
		if(template.isEmpty()) {
			return;
		}
		FluidUtil.getFluidContained(template).filter(s->!s.isEmpty()).ifPresent(s->{
			(currentFluid = s.copy()).setAmount(requiredAmount);
		});
	}

	protected void tickProcess() {
		if(amount < requiredAmount) {
			for(Direction direction : Direction.values()) {
				BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
				if(blockEntity != null && blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
					IFluidHandler fluidHandler = blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).resolve().get();
					FluidStack toDrain = currentFluid.copy();
					toDrain.setAmount(requiredAmount-amount);
					amount += fluidHandler.drain(toDrain, FluidAction.EXECUTE).getAmount();
				}
			}
		}
		else {
			int energy = energyStorage.extractEnergy(energyUsage, false);
			remainingProgress -= energy;
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
		if(itemHandler.getStackInSlot(1).isEmpty()) {
			itemHandler.setStackInSlot(1, VolumePackageItem.tryMakeVolumePackage(currentFluid));
		}
		else if(itemHandler.getStackInSlot(9).getItem() instanceof IVolumePackageItem) {
			itemHandler.getStackInSlot(1).grow(1);
		}
		endProcess();
	}

	public void startProcess() {
		remainingProgress = energyReq;
		amount = 0;
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
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			if(blockEntity != null && !(blockEntity instanceof UnpackagerBlockEntity) && blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				IItemHandler itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).resolve().get();
				ItemStack stack = this.itemHandler.getStackInSlot(1);
				if(stack.isEmpty()) {
					return;
				}
				for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
					ItemStack stackRem = itemHandler.insertItem(slot, stack, false);
					if(stackRem.getCount() < stack.getCount()) {
						stack = stackRem;
					}
					if(stack.isEmpty()) {
						break;
					}
				}
				this.itemHandler.setStackInSlot(1, stack);
			}
		}
	}

	protected void chargeEnergy() {
		int prevStored = energyStorage.getEnergyStored();
		ItemStack energyStack = itemHandler.getStackInSlot(2);
		if(energyStack.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(CapabilityEnergy.ENERGY).resolve().get().extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				itemHandler.setStackInSlot(2, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			sync(false);
			setChanged();
		}
	}

	@Override
	public void loadSync(CompoundTag nbt) {
		super.loadSync(nbt);
		isWorking = nbt.getBoolean("Working");
		currentFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("Fluid"));
		requiredAmount = nbt.getInt("AmountReq");
		amount = nbt.getInt("Amount");
		remainingProgress = nbt.getInt("Progress");
		powered = nbt.getBoolean("Powered");
	}

	@Override
	public CompoundTag saveSync(CompoundTag nbt) {
		super.saveSync(nbt);
		nbt.putBoolean("Working", isWorking);
		nbt.put("Fluid", currentFluid.writeToNBT(new CompoundTag()));
		nbt.putInt("Progress", remainingProgress);
		nbt.putBoolean("Powered", powered);
		return nbt;
	}

	@Override
	public void setChanged() {
		if(isWorking && !isTemplateValid()) {
			endProcess();
		}
		super.setChanged();
	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(), scale);
	}

	public int getScaledProgress(int scale) {
		if(remainingProgress <= 0) {
			return 0;
		}
		return scale * (energyReq-remainingProgress) / energyReq;
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return null;
	}
}
