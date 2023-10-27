package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.PackagerExtensionBlock;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagerExtensionBlockEntity;
import thelm.packagedauto.inventory.PackagerExtensionItemHandler;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.util.MiscHelper;

public class PackagerExtensionBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<PackagerExtensionBlockEntity> TYPE_INSTANCE = BlockEntityType.Builder.
			of(MiscHelper.INSTANCE.<BlockEntityType.BlockEntitySupplier<PackagerExtensionBlockEntity>>conditionalSupplier(
					()->ModList.get().isLoaded("ae2"),
					()->()->AEPackagerExtensionBlockEntity::new, ()->()->PackagerExtensionBlockEntity::new).get(),
					PackagerExtensionBlock.INSTANCE).build(null);

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static boolean drawMEEnergy = true;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public int remainingProgress = 0;
	public IItemHandlerModifiable listStackItemHandler = new ItemStackHandler(1);
	public List<IPackagePattern> patternList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public boolean lockPattern = false;
	public PackagerBlockEntity.Mode mode = PackagerBlockEntity.Mode.EXACT;
	public boolean disjoint = false;
	public boolean powered = false;

	public PackagerExtensionBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new PackagerExtensionItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.packager_extension");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			updatePatternList();
			updatePowered();
		}
		if(!level.isClientSide) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isInputValid()) {
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
		}
	}

	public boolean isInputValid() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			return false;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return false;
		}
		if(!lockPattern && disjoint) {
			return MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true);
		}
		List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), PackagerBlockEntity::getIngredient);
		int[] matches = RecipeMatcher.findMatches(input, matchers);
		if(matches == null) {
			return false;
		}
		for(int i = 0; i < matches.length; ++i) {
			if(input.get(i).getCount() < currentPattern.getInputs().get(matches[i]).getCount()) {
				return false;
			}
		}
		return true;
	}

	protected boolean canStart() {
		getPattern();
		if(currentPattern == null) {
			return false;
		}
		if(!isInputValid()) {
			return false;
		}
		ItemStack slotStack = itemHandler.getStackInSlot(9);
		ItemStack outputStack = currentPattern.getOutput();
		return slotStack.isEmpty() || ItemStack.isSameItemSameTags(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
	}

	protected boolean canFinish() {
		return remainingProgress <= 0 && isInputValid();
	}

	protected void getPattern() {
		if(currentPattern != null && lockPattern) {
			return;
		}
		lockPattern = false;
		currentPattern = null;
		if(powered) {
			return;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return;
		}
		for(IPackagePattern pattern : patternList) {
			if(disjoint) {
				if(MiscHelper.INSTANCE.removeExactSet(input, pattern.getInputs(), true)) {
					currentPattern = pattern;
					return;
				}
			}
			else {
				List<Ingredient> matchers = Lists.transform(pattern.getInputs(), PackagerBlockEntity::getIngredient);
				int[] matches = RecipeMatcher.findMatches(input, matchers);
				if(matches != null) {
					currentPattern = pattern;
					return;
				}
			}
		}
	}

	public void updatePatternList() {
		patternList.clear();
		if(level != null) {
			for(BlockPos posP : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
				if(level.getBlockEntity(posP) instanceof PackagerBlockEntity packager) {
					ItemStack listStack = packager.itemHandler.getStackInSlot(10);
					listStackItemHandler.setStackInSlot(0, listStack);
					if(listStack.getItem() instanceof IPackageRecipeListItem listItem) {
						listItem.getRecipeList(level, listStack).getRecipeList().forEach(recipe->recipe.getPatterns().forEach(patternList::add));
					}
					else if(listStack.getItem() instanceof IPackageItem packageItem) {
						patternList.add(packageItem.getRecipeInfo(listStack).getPatterns().get(packageItem.getIndex(listStack)));
					}
					if(mode == PackagerBlockEntity.Mode.FIRST) {
						disjoint = true;
					}
					else if(mode == PackagerBlockEntity.Mode.DISJOINT) {
						disjoint = MiscHelper.INSTANCE.arePatternsDisjoint(patternList);
					}
					break;
				}
			}
			if(!level.isClientSide) {
				postPatternChange();
			}
		}
	}

	protected void tickProcess() {
		int energy = energyStorage.extractEnergy(Math.min(energyUsage, remainingProgress), false);
		remainingProgress -= energy;
	}

	protected void finishProcess() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			endProcess();
			return;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			endProcess();
			return;
		}
		if(!lockPattern && disjoint) {
			if(!MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true)) {
				endProcess();
				return;
			}
			if(itemHandler.getStackInSlot(9).isEmpty()) {
				itemHandler.setStackInSlot(9, currentPattern.getOutput());
			}
			else if(itemHandler.getStackInSlot(9).getItem() instanceof IPackageItem) {
				itemHandler.getStackInSlot(9).grow(1);
			}
			else {
				endProcess();
				return;
			}
			MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), false);
		}
		else {
			List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), PackagerBlockEntity::getIngredient);
			int[] matches = RecipeMatcher.findMatches(input, matchers);
			if(matches == null) {
				endProcess();
				return;
			}
			if(itemHandler.getStackInSlot(9).isEmpty()) {
				itemHandler.setStackInSlot(9, currentPattern.getOutput());
			}
			else if(itemHandler.getStackInSlot(9).getItem() instanceof IPackageItem) {
				itemHandler.getStackInSlot(9).grow(1);
			}
			else {
				endProcess();
				return;
			}
			for(int i = 0; i < matches.length; ++i) {
				input.get(i).shrink(currentPattern.getInputs().get(matches[i]).getCount());
			}
		}
		for(int i = 0; i < 9; ++i) {
			if(itemHandler.getStackInSlot(i).isEmpty()) {
				itemHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	protected void startProcess() {
		remainingProgress = energyReq;
		setChanged();
	}

	public void endProcess() {
		remainingProgress = 0;
		isWorking = false;
		lockPattern = false;
		setChanged();
	}

	protected void ejectItem() {
		for(Direction direction : Direction.values()) {
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof UnpackagerBlockEntity unpackager) {
				ItemStack stack = itemHandler.getStackInSlot(9);
				if(!stack.isEmpty()) {
					ItemStack stackRem = ItemHandlerHelper.insertItem(unpackager.itemHandler, stack, false);
					itemHandler.setStackInSlot(9, stackRem);
				}
			}
		}
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
			setChanged();
		}
	}

	@Override
	public int getComparatorSignal() {
		if(isWorking) {
			return 1;
		}
		if(!itemHandler.getStackInSlot(9).isEmpty()) {
			return 15;
		}
		return 0;
	}

	protected void postPatternChange() {

	}

	@Override
	public void load(CompoundTag nbt) {
		mode = PackagerBlockEntity.Mode.values()[nbt.getByte("Mode")];
		super.load(nbt);
		updatePatternList();
		isWorking = nbt.getBoolean("Working");
		remainingProgress = nbt.getInt("Progress");
		powered = nbt.getBoolean("Powered");
		lockPattern = false;
		currentPattern = null;
		if(nbt.contains("Pattern")) {
			CompoundTag tag = nbt.getCompound("Pattern");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("Index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
					lockPattern = true;
				}
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putByte("Mode", (byte)mode.ordinal());
		nbt.putBoolean("Working", isWorking);
		nbt.putInt("Progress", remainingProgress);
		nbt.putBoolean("Powered", powered);
		if(lockPattern) {
			CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), currentPattern.getRecipeInfo());
			tag.putByte("Index", (byte)currentPattern.getIndex());
			nbt.put("Pattern", tag);
		}
	}

	public void changePackagingMode() {
		mode = PackagerBlockEntity.Mode.values()[((mode.ordinal()+1) % 3)];
		updatePatternList();
		setChanged();
	}

	@Override
	public void setChanged() {
		if(isWorking && !isInputValid()) {
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
		if(remainingProgress <= 0 || energyReq <= 0) {
			return 0;
		}
		return scale * (energyReq-remainingProgress) / energyReq;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new PackagerExtensionMenu(windowId, inventory, this);
	}
}
