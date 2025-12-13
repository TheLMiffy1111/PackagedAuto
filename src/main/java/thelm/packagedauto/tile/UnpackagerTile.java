package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.container.UnpackagerContainer;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.tile.AEUnpackagerTile;
import thelm.packagedauto.inventory.UnpackagerItemHandler;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerTile extends BaseTile implements ITickableTileEntity, ISettingsCloneable {

	public static final TileEntityType<UnpackagerTile> TYPE_INSTANCE = (TileEntityType<UnpackagerTile>)TileEntityType.Builder.
			of(MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("appliedenergistics2"),
					()->AEUnpackagerTile::new, ()->UnpackagerTile::new), UnpackagerBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:unpackager");

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

	public UnpackagerTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new UnpackagerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i] = new PackageTracker();
		}
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.unpackager");
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
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).limit(trackerCount).filter(t->t.isEmpty()).collect(Collectors.toList());
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
		List<Direction> directions = Lists.newArrayList(Direction.values());
		Collections.rotate(directions, roundRobinIndex);
		for(Direction direction : directions) {
			TileEntity tile = level.getBlockEntity(worldPosition.relative(direction));
			if(tile instanceof IPackageCraftingMachine) {
				IPackageCraftingMachine machine = (IPackageCraftingMachine)tile;
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), direction.getOpposite(), blocking)) {
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
			for(Direction direction : directions) {
				TileEntity tile = level.getBlockEntity(worldPosition.relative(direction));
				if(!validSendTarget(tile, direction.getOpposite())) {
					continue;
				}
				if(!tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
					continue;
				}
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).resolve().get();
				if(blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
					continue;
				}
				PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled() && t.direction == null && t.recipe != null && !t.recipe.getRecipeType().hasMachine()).findFirst().orElse(null);
				if(trackerToEmpty == null) {
					continue;
				}
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.setupToSend();
				}
				boolean acceptsAll = true;
				for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
					ItemStack stack = trackerToEmpty.toSend.get(i);
					ItemStack stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, false, true);
					acceptsAll &= stackRem.getCount() < stack.getCount();
				}
				trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
				if(acceptsAll) {
					trackerToEmpty.direction = direction;
					roundRobinIndex = (roundRobinIndex+1) % 6;
				}
				setChanged();
			}
		}
		for(Direction direction : Direction.values()) {
			TileEntity tile = level.getBlockEntity(worldPosition.relative(direction));
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
			if(!validSendTarget(tile, direction.getOpposite())) {
				trackerToEmpty.direction = null;
				continue;
			}
			if(!tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				trackerToEmpty.direction = null;
				continue;
			}
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).resolve().get();
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				ItemStack stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, ordered, false);
				trackerToEmpty.toSend.set(i, stackRem);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			setChanged();
		}
	}

	protected boolean validSendTarget(TileEntity tile, Direction direction) {
		return tile != null &&
				!(tile instanceof IPackageCraftingMachine) &&
				!(tile instanceof PackagerTile) &&
				!(tile instanceof PackagerExtensionTile) &&
				!(tile instanceof UnpackagerTile);
	}

	protected void chargeEnergy() {
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
	public ISettingsCloneable.Result loadConfig(CompoundNBT nbt, PlayerEntity player) {
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.getByte("Trackers");
		ITextComponent message = null;
		if(nbt.contains("Recipes")) {
			f:if(itemHandler.getStackInSlot(9).isEmpty()) {
				PlayerInventory playerInventory = player.inventory;
				for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
					ItemStack stack = playerInventory.getItem(i);
					if(!stack.isEmpty() && stack.getItem() == RecipeHolderItem.INSTANCE && !stack.hasTag()) {
						ItemStack stackCopy = stack.split(1);
						IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stackCopy);
						List<IPackageRecipeInfo> recipeList = MiscHelper.INSTANCE.readRecipeList(nbt.getList("Recipes", 10));
						recipeListObj.setRecipeList(recipeList);
						RecipeHolderItem.INSTANCE.setRecipeList(stackCopy, recipeListObj);
						itemHandler.setStackInSlot(9, stackCopy);
						break f;
					}
				}
				message = new TranslationTextComponent("block.packagedauto.unpackager.no_holders");
			}
			else {
				message = new TranslationTextComponent("block.packagedauto.unpackager.holder_present");
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
	public ISettingsCloneable.Result saveConfig(CompoundNBT nbt, PlayerEntity player) {
		nbt.putBoolean("Blocking", blocking);
		nbt.putByte("Trackers", (byte)trackerCount);
		if(!recipeList.isEmpty()) {
			nbt.put("Recipes", MiscHelper.INSTANCE.writeRecipeList(new ListNBT(), recipeList));
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	public void load(BlockState blockState, CompoundNBT nbt) {
		super.load(blockState, nbt);
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.contains("Trackers") ? nbt.getByte("Trackers") : 6;
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].read(nbt.getCompound(String.format("Tracker%02d", i)));
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		super.save(nbt);
		nbt.putBoolean("Blocking", blocking);
		nbt.putByte("Trackers", (byte)trackerCount);
		nbt.putBoolean("Powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			CompoundNBT subNBT = new CompoundNBT();
			trackers[i].write(subNBT);
			nbt.put(String.format("Tracker%02d", i), subNBT);
		}
		return nbt;
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		setChanged();
	}

	public void changeTrackerCount(boolean decrease) {
		trackerCount = MathHelper.clamp(trackerCount + (decrease ? -1 : 1), 1, 10);
		setChanged();
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new UnpackagerContainer(windowId, playerInventory, this);
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
			if(recipe != null && recipe.isPackageable() && recipe.validPatternIndex(index)) {
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
			return recipe == null;
		}

		public void setupToSend() {
			if(isEmpty() || !recipe.isPackageable() || recipe.getRecipeType().hasMachine() || !toSend.isEmpty()) {
				return;
			}
			toSend.addAll(Lists.transform(recipe.getInputs(), ItemStack::copy));
		}

		public void read(CompoundNBT nbt) {
			clearRecipe();
			if(nbt.contains("Recipe")) {
				CompoundNBT tag = nbt.getCompound("Recipe");
				IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipe(tag);
				if(recipe != null) {
					this.recipe = recipe;
					amount = nbt.getByte("Amount");
					received.size(amount);
					byte[] receivedArray = nbt.getByteArray("Received");
					for(int i = 0; i < received.size(); ++i) {
						received.set(i, receivedArray[i] != 0);
					}
				}
			}
			MiscHelper.INSTANCE.loadAllItems(nbt.getList("ToSend", 10), toSend);
			if(nbt.contains("Direction")) {
				direction = Direction.from3DDataValue(nbt.getByte("Direction"));
			}
		}

		public CompoundNBT write(CompoundNBT nbt) {
			if(recipe != null) {
				CompoundNBT tag = MiscHelper.INSTANCE.writeRecipe(new CompoundNBT(), recipe);
				nbt.put("Recipe", tag);
				nbt.putByte("Amount", (byte)amount);
				byte[] receivedArray = new byte[received.size()];
				for(int i = 0; i < received.size(); ++i) {
					receivedArray[i] = (byte)(received.getBoolean(i) ? 1 : 0);
				}
				nbt.putByteArray("Received", receivedArray);
			}
			nbt.put("ToSend", MiscHelper.INSTANCE.saveAllItems(new ListNBT(), toSend));
			if(direction != null) {
				nbt.putByte("Direction", (byte)direction.get3DDataValue());
			}
			return nbt;
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
