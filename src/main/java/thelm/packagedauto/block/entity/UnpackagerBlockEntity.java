package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.blockentity.AEUnpackagerBlockEntity;
import thelm.packagedauto.inventory.UnpackagerItemHandler;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<UnpackagerBlockEntity> TYPE_INSTANCE = (BlockEntityType<UnpackagerBlockEntity>)BlockEntityType.Builder.
			of(MiscHelper.INSTANCE.<BlockEntityType.BlockEntitySupplier<UnpackagerBlockEntity>>conditionalSupplier(
					()->ModList.get().isLoaded("ae2"),
					()->()->AEUnpackagerBlockEntity::new, ()->()->UnpackagerBlockEntity::new).get(),
					UnpackagerBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:unpackager");

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static boolean drawMEEnergy = true;

	public boolean firstTick = true;
	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;

	public UnpackagerBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new UnpackagerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i] = new PackageTracker();
		}
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("block.packagedauto.unpackager");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			if(!level.isClientSide) {
				postPatternChange();
			}
			updatePowered();
		}
		if(!level.isClientSide) {
			chargeEnergy();
			if(level.getGameTime() % 8 == 0) {
				fillTrackers();
				emptyTrackers();
			}
			energyStorage.updateIfChanged();
		}
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).filter(t->t.isEmpty()).collect(Collectors.toList());
		List<PackageTracker> nonEmptyTrackers = Arrays.stream(trackers).filter(t->!t.isEmpty()).filter(t->!t.isFilled()).collect(Collectors.toList());
		for(int i = 0; i < 9; ++i) {
			if(energyStorage.getEnergyStored() >= energyUsage) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() instanceof IPackageItem) {
					IPackageItem packageItem = (IPackageItem)stack.getItem();
					boolean flag = false;
					for(PackageTracker tracker : nonEmptyTrackers) {
						if(tracker.tryAcceptPackage(packageItem, stack, i)) {
							flag = true;
							stack.shrink(1);
							if(stack.isEmpty()) {
								itemHandler.setStackInSlot(i, ItemStack.EMPTY);
							}
							else {
								tracker.setRejectedIndex(i, true);
							}
							energyStorage.extractEnergy(energyUsage, false);
							break;
						}
						else {
							tracker.setRejectedIndex(i, true);
						}
					}
					if(!flag) {
						for(PackageTracker tracker : emptyTrackers) {
							if(tracker.tryAcceptPackage(packageItem, stack, i)) {
								stack.shrink(1);
								if(stack.isEmpty()) {
									itemHandler.setStackInSlot(i, ItemStack.EMPTY);
								}
								else {
									tracker.setRejectedIndex(i, true);
								}
								energyStorage.extractEnergy(energyUsage, false);
								break;
							}
							else {
								tracker.setRejectedIndex(i, true);
							}
						}
					}
				}
			}
		}
	}

	protected void emptyTrackers() {
		for(Direction direction : Direction.values()) {
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof IPackageCraftingMachine machine) {
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), direction.getOpposite())) {
							tracker.clearRecipe();
							sync(false);
							setChanged();
							break;
						}
					}
				}
				continue;
			}
		}
		for(Direction direction : Direction.values()) {
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.direction == direction).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			if(trackerToEmpty.recipe != null && trackerToEmpty.recipe.getRecipeType().hasMachine()) {
				trackerToEmpty.direction = null;
				continue;
			}
			if(blockEntity == null || blockEntity instanceof PackagerBlockEntity || blockEntity instanceof UnpackagerBlockEntity || isPatternProvider(blockEntity, direction.getOpposite())) {
				trackerToEmpty.direction = null;
				continue;
			}
			IItemHandler itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				if(stack.getItem() instanceof IVolumePackageItem vPackage && vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction.getOpposite())) {
					ItemStack stackCopy = stack.copy();
					IVolumeType vType = vPackage.getVolumeType(stack);
					IVolumeStackWrapper vStack = vPackage.getVolumeStack(stack);
					while(!stackCopy.isEmpty()) {
						int simulateFilled = vType.fill(blockEntity, direction.getOpposite(), vStack, true);
						if(simulateFilled == vStack.getAmount()) {
							vType.fill(blockEntity, direction.getOpposite(), vStack, false);
							stackCopy.shrink(1);
						}
						else {
							break;
						}
					}
					stack = stackCopy;
				}
				else if(itemHandler != null) {
					for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
						ItemStack stackRem = itemHandler.insertItem(slot, stack, false);
						if(stackRem.getCount() < stack.getCount()) {
							stack = stackRem;
						}
						if(stack.isEmpty()) {
							break;
						}
					}
				}
				trackerToEmpty.toSend.set(i, stack);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			setChanged();
		}
		for(Direction direction : Direction.values()) {
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			if(blockEntity == null || blockEntity instanceof PackagerBlockEntity || blockEntity instanceof UnpackagerBlockEntity || isPatternProvider(blockEntity, direction.getOpposite())) {
				continue;
			}
			IItemHandler itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
			if(powered || blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
				continue;
			}
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled() && t.direction == null && t.recipe != null && !t.recipe.getRecipeType().hasMachine()).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			boolean inserted = false;
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				if(stack.getItem() instanceof IVolumePackageItem vPackage && vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction.getOpposite())) {
					ItemStack stackCopy = stack.copy();
					IVolumeType vType = vPackage.getVolumeType(stack);
					IVolumeStackWrapper vStack = vPackage.getVolumeStack(stack);
					while(!stackCopy.isEmpty()) {
						int simulateFilled = vType.fill(blockEntity, direction.getOpposite(), vStack, true);
						if(simulateFilled == vStack.getAmount()) {
							vType.fill(blockEntity, direction.getOpposite(), vStack, false);
							stackCopy.shrink(1);
						}
						else {
							break;
						}
					}
					stack = stackCopy;
				}
				else if(itemHandler != null) {
					for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
						ItemStack stackRem = itemHandler.insertItem(slot, stack, false);
						if(stackRem.getCount() < stack.getCount()) {
							stack = stackRem;
							inserted = true;
						}
						if(stack.isEmpty()) {
							break;
						}
					}
				}
				trackerToEmpty.toSend.set(i, stack);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(inserted) {
				trackerToEmpty.direction = direction;
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.clearRecipe();
				}
				setChanged();
			}
		}
	}

	protected void chargeEnergy() {
		int prevStored = energyStorage.getEnergyStored();
		ItemStack energyStack = itemHandler.getStackInSlot(10);
		if(energyStack.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(CapabilityEnergy.ENERGY).resolve().get().extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				itemHandler.setStackInSlot(10, ItemStack.EMPTY);
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

	protected boolean isPatternProvider(BlockEntity blockEntity, Direction facing) {
		return false;
	}

	public void postPatternChange() {

	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(), scale);
	}

	@Override
	public void loadSync(CompoundTag nbt) {
		super.loadSync(nbt);
		blocking = nbt.getBoolean("Blocking");
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].load(nbt.getCompound(String.format("Tracker%02d", i)));
		}
	}

	@Override
	public CompoundTag saveSync(CompoundTag nbt) {
		super.saveSync(nbt);
		nbt.putBoolean("Blocking", blocking);
		nbt.putBoolean("Powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			CompoundTag subNBT = new CompoundTag();
			trackers[i].save(subNBT);
			nbt.put(String.format("Tracker%02d", i), subNBT);
		}
		return nbt;
	}

	public void changeBlockingMode() {
		blocking = !blocking;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new UnpackagerMenu(windowId, inventory, this);
	}

	public class PackageTracker {

		public boolean[] rejectedIndexes = new boolean[9];
		public IPackageRecipeInfo recipe;
		public int amount;
		public BooleanList received = new BooleanArrayList(9);
		public List<ItemStack> toSend = new ArrayList<>();
		public Direction direction;

		public void setRecipe(IPackageRecipeInfo recipe) {
			this.recipe = recipe;
		}

		public void clearRecipe() {
			clearRejectedIndexes();
			recipe = null;
			amount = 0;
			received.clear();
			direction = null;
			if(level != null && !level.isClientSide) {
				sync(false);
				setChanged();
			}
		}

		public boolean tryAcceptPackage(IPackageItem packageItem, ItemStack stack, int invIndex) {
			if(rejectedIndexes[invIndex]) {
				return false;
			}
			IPackageRecipeInfo recipe = packageItem.getRecipeInfo(stack);
			if(recipe != null) {
				if(this.recipe == null) {
					this.recipe = recipe;
					amount = recipe.getPatterns().size();
					received.size(amount);
					received.set(packageItem.getIndex(stack), true);
					sync(false);
					setChanged();
					return true;
				}
				else if(this.recipe.equals(recipe)) {
					int index = packageItem.getIndex(stack);
					if(!received.getBoolean(index)) {
						received.set(index, true);
						sync(false);
						setChanged();
						return true;
					}
				}
			}
			return false;
		}

		public void setRejectedIndex(int index, boolean rejected) {
			rejectedIndexes[index] = rejected;
		}

		public void clearRejectedIndexes() {
			Arrays.fill(rejectedIndexes, false);
		}

		public boolean isFilled() {
			if(!toSend.isEmpty()) {
				return true;
			}
			if(received.isEmpty()) {
				return false;
			}
			for(boolean b : received) {
				if(!b) {
					return false;
				}
			}
			return true;
		}

		public boolean isEmpty() {
			return recipe == null || !recipe.isValid();
		}

		public void setupToSend() {
			if(isEmpty() || recipe.getRecipeType().hasMachine() || !toSend.isEmpty()) {
				return;
			}
			toSend.addAll(Lists.transform(recipe.getInputs(), ItemStack::copy));
		}

		public void load(CompoundTag nbt) {
			clearRecipe();
			CompoundTag tag = nbt.getCompound("Recipe");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag);
			if(recipe != null) {
				this.recipe = recipe;
				amount = nbt.getByte("Amount");
				received.size(amount);
				byte[] receivedArray = nbt.getByteArray("Received");
				for(int i = 0; i < received.size(); ++i) {
					received.set(i, receivedArray[i] != 0);
				}
			}
			MiscHelper.INSTANCE.loadAllItems(nbt.getList("ToSend", 10), toSend);
			if(nbt.contains("Direction")) {
				direction = Direction.from3DDataValue(nbt.getByte("Direction"));
			}
		}

		public void save(CompoundTag nbt) {
			if(recipe != null) {
				CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), recipe);
				nbt.put("Recipe", tag);
				nbt.putByte("Amount", (byte)amount);
				byte[] receivedArray = new byte[received.size()];
				for(int i = 0; i < received.size(); ++i) {
					receivedArray[i] = (byte)(received.getBoolean(i) ? 1 : 0);
				}
				nbt.putByteArray("Received", receivedArray);
			}
			nbt.put("ToSend", MiscHelper.INSTANCE.saveAllItems(new ListTag(), toSend));
			if(direction != null) {
				nbt.putByte("Facing", (byte)direction.get3DDataValue());
			}
		}
	}
}
