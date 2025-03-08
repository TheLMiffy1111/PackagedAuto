package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.blockentity.AEUnpackagerBlockEntity;
import thelm.packagedauto.inventory.UnpackagerItemHandler;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerBlockEntity extends BaseBlockEntity implements ISettingsCloneable {

	public static final BlockEntityType<UnpackagerBlockEntity> TYPE_INSTANCE = BlockEntityType.Builder.
			of(MiscHelper.INSTANCE.<BlockEntityType.BlockEntitySupplier<UnpackagerBlockEntity>>conditionalSupplier(
					()->ModList.get().isLoaded("ae2"),
					()->()->AEUnpackagerBlockEntity::new, ()->()->UnpackagerBlockEntity::new).get(),
					UnpackagerBlock.INSTANCE).build(null);

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static int refreshInterval = 4;
	public static boolean drawMEEnergy = true;

	public boolean firstTick = true;
	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;
	public int trackerCount = 6;
	public int roundRobinIndex = 0;

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
		return Component.translatable("block.packagedauto.unpackager");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.unpackager";
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
			if(level.getGameTime() % refreshInterval == 0) {
				fillTrackers();
				emptyTrackers();
			}
		}
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).limit(trackerCount).filter(t->t.isEmpty()).toList();
		List<PackageTracker> nonEmptyTrackers = Arrays.stream(trackers).filter(t->!t.isEmpty()).filter(t->!t.isFilled()).toList();
		for(int i = 0; i < 9; ++i) {
			if(energyStorage.getEnergyStored() >= energyUsage) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() instanceof IPackageItem packageItem) {
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
		List<Direction> directions = Lists.newArrayList(Direction.values());
		Collections.rotate(directions, roundRobinIndex);
		for(Direction direction : directions) {
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof IPackageCraftingMachine machine) {
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), direction.getOpposite())) {
							tracker.clearRecipe();
							roundRobinIndex = (roundRobinIndex+1) % 6;
							setChanged();
							break;
						}
					}
				}
				continue;
			}
		}
		if(!powered) {
			directions = Lists.newArrayList(Direction.values());
			Collections.rotate(directions, roundRobinIndex);
			dir:for(Direction direction : directions) {
				PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled() && t.direction == null && t.recipe != null && !t.recipe.getRecipeType().hasMachine()).findFirst().orElse(null);
				if(trackerToEmpty == null) {
					continue;
				}
				BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
				if(!validSendTarget(blockEntity, direction.getOpposite())) {
					continue;
				}
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.setupToSend();
				}
				IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(null);
				if(blocking) {
					for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
						ItemStack stack = trackerToEmpty.toSend.get(i);
						if(stack.getItem() instanceof IVolumePackageItem vPackage &&
								vPackage.getVolumeType(stack) != null &&
								vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction.getOpposite())) {
							if(!vPackage.getVolumeType(stack).isEmpty(blockEntity, direction.getOpposite())) {
								continue dir;
							}
						}
						else if(itemHandler != null && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
							continue dir;
						}
					}
				}
				boolean acceptsAll = true;
				for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
					ItemStack stack = trackerToEmpty.toSend.get(i);
					ItemStack stackRem = stack;
					if(stack.getItem() instanceof IVolumePackageItem vPackage &&
							vPackage.getVolumeType(stack) != null &&
							vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction.getOpposite())) {
						stackRem = MiscHelper.INSTANCE.fillVolume(blockEntity, direction.getOpposite(), stack, true);
					}
					else if(itemHandler != null) {
						stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, false, true);
					}
					acceptsAll &= stackRem.getCount() < stack.getCount();
				}
				if(acceptsAll) {
					trackerToEmpty.direction = direction;
					roundRobinIndex = (roundRobinIndex+1) % 6;
				}
				setChanged();
			}
		}
		for(Direction direction : Direction.values()) {
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.direction == direction).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			boolean ordered = false;
			if(trackerToEmpty.recipe != null) {
				ordered = trackerToEmpty.recipe.getRecipeType().isOrdered();
			}
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			if(!validSendTarget(blockEntity, direction.getOpposite())) {
				trackerToEmpty.direction = null;
				continue;
			}
			IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(null);
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				ItemStack stackRem = stack;
				if(stack.getItem() instanceof IVolumePackageItem vPackage &&
						vPackage.getVolumeType(stack) != null &&
						vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction.getOpposite())) {
					stackRem = MiscHelper.INSTANCE.fillVolume(blockEntity, direction.getOpposite(), stack, false);
				}
				else if(itemHandler != null) {
					stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, ordered, false);
				}
				trackerToEmpty.toSend.set(i, stackRem);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			setChanged();
		}
	}

	protected boolean validSendTarget(BlockEntity blockEntity, Direction direction) {
		return blockEntity != null &&
				!(blockEntity instanceof IPackageCraftingMachine) &&
				!(blockEntity instanceof PackagerBlockEntity) &&
				!(blockEntity instanceof PackagerExtensionBlockEntity) &&
				!(blockEntity instanceof UnpackagerBlockEntity);
	}

	protected void chargeEnergy() {
		ItemStack energyStack = itemHandler.getStackInSlot(10);
		if(energyStack.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(ForgeCapabilities.ENERGY).resolve().get().extractEnergy(energyRequest, false), false);
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

	@Override
	public int getComparatorSignal() {
		return Math.min((int)Arrays.stream(trackers).filter(t->t.isFilled()).count(), 15);
	}

	public void postPatternChange() {}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(), scale);
	}

	@Override
	public ISettingsCloneable.Result loadConfig(CompoundTag nbt, Player player) {
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.getByte("Trackers");
		Component message = null;
		if(nbt.contains("Recipes")) {
			f:if(itemHandler.getStackInSlot(9).isEmpty()) {
				Inventory playerInventory = player.getInventory();
				for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
					ItemStack stack = playerInventory.getItem(i);
					if(!stack.isEmpty() && stack.is(RecipeHolderItem.INSTANCE) && !stack.hasTag()) {
						ItemStack stackCopy = stack.split(1);
						IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stackCopy);
						List<IPackageRecipeInfo> recipeList = MiscHelper.INSTANCE.loadRecipeList(nbt.getList("Recipes", 10));
						recipeListObj.setRecipeList(recipeList);
						RecipeHolderItem.INSTANCE.setRecipeList(stackCopy, recipeListObj);
						itemHandler.setStackInSlot(9, stackCopy);
						break f;
					}
				}
				message = Component.translatable("block.packagedauto.unpackager.no_holders");
			}
			else {
				message = Component.translatable("block.packagedauto.unpackager.holder_present");
			}
		}
		if(message != null) {
			return ISettingsCloneable.Result.partial(message);
		}
		else {
			return ISettingsCloneable.Result.success();
		}
	}

	@Override
	public ISettingsCloneable.Result saveConfig(CompoundTag nbt, Player player) {
		nbt.putBoolean("Blocking", blocking);
		nbt.putByte("Trackers", (byte)trackerCount);
		if(!recipeList.isEmpty()) {
			nbt.put("Recipes", MiscHelper.INSTANCE.saveRecipeList(new ListTag(), recipeList));
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.contains("Trackers") ? nbt.getByte("Trackers") : 6;
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].load(nbt.getCompound(String.format("Tracker%02d", i)));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putBoolean("Blocking", blocking);
		nbt.putByte("Trackers", (byte)trackerCount);
		nbt.putBoolean("Powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			CompoundTag subNBT = new CompoundTag();
			trackers[i].save(subNBT);
			nbt.put(String.format("Tracker%02d", i), subNBT);
		}
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		setChanged();
	}

	public void changeTrackerCount(boolean decrease) {
		trackerCount = Mth.clamp(trackerCount + (decrease ? -1 : 1), 1, 10);
		setChanged();
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

		public void clearRecipe() {
			clearRejectedIndexes();
			recipe = null;
			amount = 0;
			received.clear();
			direction = null;
			if(level != null && !level.isClientSide) {
				setChanged();
			}
		}

		public void fillRecipe(IPackageRecipeInfo recipe) {
			this.recipe = recipe;
			amount = recipe.getPatterns().size();
			received.clear();
			received.size(amount);
			for(int i = 0; i < received.size(); ++i) {
				received.set(i, true);
			}
			if(level != null && !level.isClientSide) {
				setChanged();
			}
		}

		public void ejectItems() {
			if(level != null && !isEmpty()) {
				if(!toSend.isEmpty()) {
					for(ItemStack stack : toSend) {
						if(!stack.isEmpty()) {
							double dx = level.random.nextFloat()/2+0.25;
							double dy = level.random.nextFloat()/2+0.75;
							double dz = level.random.nextFloat()/2+0.25;
							ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX()+dx, worldPosition.getY()+dy, worldPosition.getZ()+dz, stack);
							itemEntity.setDefaultPickUpDelay();
							level.addFreshEntity(itemEntity);
						}
					}
				}
				else {
					List<IPackagePattern> patterns = recipe.getPatterns();
					for(int i = 0; i < received.size() && i < patterns.size(); ++i) {
						if(received.getBoolean(i)) {
							double dx = level.random.nextFloat()/2+0.25;
							double dy = level.random.nextFloat()/2+0.75;
							double dz = level.random.nextFloat()/2+0.25;
							ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX()+dx, worldPosition.getY()+dy, worldPosition.getZ()+dz, patterns.get(i).getOutput());
							itemEntity.setDefaultPickUpDelay();
							level.addFreshEntity(itemEntity);
						}
					}
				}
				clearRecipe();
			}
		}

		public boolean tryAcceptPackage(IPackageItem packageItem, ItemStack stack, int invIndex) {
			if(rejectedIndexes[invIndex]) {
				return false;
			}
			IPackageRecipeInfo recipe = packageItem.getRecipeInfo(stack);
			int index = packageItem.getIndex(stack);
			if(recipe != null && recipe.isValid() && recipe.validPatternIndex(index)) {
				if(this.recipe == null) {
					this.recipe = recipe;
					amount = recipe.getPatterns().size();
					received.size(amount);
					received.set(index, true);
					setChanged();
					return true;
				}
				else if(this.recipe.equals(recipe)) {
					if(!received.getBoolean(index)) {
						received.set(index, true);
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
				nbt.putByte("Direction", (byte)direction.get3DDataValue());
			}
		}

		public int getSyncValue() {
			int val = 0;
			for(int i = 0; i < received.size(); ++i) {
				if(received.getBoolean(i)) {
					val |= 1 << i;
				}
			}
			val <<= 4;
			val |= amount;
			return val;
		}

		public void setSyncValue(int val) {
			amount = val & 15;
			received.size(amount);
			val >>>= 4;
			for(int i = 0; i < received.size(); ++i) {
				received.set(i, ((val >>> i) & 1) != 0);
			}
		}
	}
}
