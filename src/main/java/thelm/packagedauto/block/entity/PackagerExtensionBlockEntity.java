package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.block.entity.PackagerBlockEntity.Mode;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.PackagerExtensionItemResourceHandler;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.util.MiscHelper;

public class PackagerExtensionBlockEntity extends BaseBlockEntity implements ISettingsCloneable {

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static int refreshInterval = 4;
	public static boolean drawMEEnergy = true;

	public final ResourceHandler<ItemResource> packageSlot;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public int remainingProgress = 0;
	public PackagerBlockEntity packager;
	public ItemStack listStack = ItemStack.EMPTY;
	public List<IPackagePattern> patternList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public boolean lockPattern = false;
	public PackagerBlockEntity.Mode mode = PackagerBlockEntity.Mode.EXACT;
	public boolean disjoint = false;
	public boolean powered = false;

	public PackagerExtensionBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.PACKAGER_EXTENSION.get(), pos, state);
		setItemHandler(new PackagerExtensionItemResourceHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		packageSlot = RangedResourceHandler.ofSingleIndex(this::getItemHandler, 9);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.packager_extension");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.packager";
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			updatePatternList();
			updatePowered();
		}
		if(!level.isClientSide()) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isInputValid()) {
					finishProcess();
					if(!itemHandler.getStack(9).isEmpty()) {
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
			else if(level.getGameTime() % refreshInterval == 0) {
				if(canStart()) {
					startProcess();
					tickProcess();
					isWorking = true;
				}
			}
			chargeEnergy();
			if(level.getGameTime() % refreshInterval == 0) {
				if(!itemHandler.getStack(9).isEmpty()) {
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
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).toList();
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
		ItemStack slotStack = itemHandler.getStack(9);
		ItemStack outputStack = currentPattern.getOutput();
		return slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
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
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).toList();
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
		packager = null;
		listStack = ItemStack.EMPTY;
		patternList.clear();
		if(level != null) {
			for(BlockPos posP : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
				if(level.getBlockEntity(posP) instanceof PackagerBlockEntity packager) {
					this.packager = packager;
					this.listStack = packager.itemHandler.getStack(10);
					patternList.addAll(packager.patternList);
					disjoint = switch(mode) {
					case EXACT -> false;
					case DISJOINT -> MiscHelper.INSTANCE.arePatternsDisjoint(patternList);
					case FIRST -> true;
					};
					break;
				}
			}
		}
	}

	protected void tickProcess() {
		try(Transaction transaction = Transaction.openRoot()) {
			int energy = energyStorage.extract(Math.min(energyUsage, remainingProgress), transaction);
			transaction.commit();
			remainingProgress -= energy;
		}
	}

	protected void finishProcess() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			endProcess();
			return;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).toList();
		if(input.isEmpty()) {
			endProcess();
			return;
		}
		if(!lockPattern && disjoint) {
			if(!MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true)) {
				endProcess();
				return;
			}
			if(itemHandler.getStack(9).isEmpty()) {
				itemHandler.setStack(9, currentPattern.getOutput());
			}
			else if(MiscHelper.INSTANCE.isPackage(itemHandler.getStack(9))) {
				itemHandler.getStack(9).grow(1);
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
			if(itemHandler.getStack(9).isEmpty()) {
				itemHandler.setStack(9, currentPattern.getOutput());
			}
			else if(MiscHelper.INSTANCE.isPackage(itemHandler.getStack(9))) {
				itemHandler.getStack(9).grow(1);
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
			if(itemHandler.getStack(i).isEmpty()) {
				itemHandler.setStack(i, ItemStack.EMPTY);
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
				int moveCount = packageSlot.getAmountAsInt(0);
				if(moveCount > 0) {
					ResourceHandlerUtil.moveStacking(packageSlot, unpackager.itemHandler, _->true, moveCount, null);
				}
			}
		}
	}

	protected void chargeEnergy() {
		EnergyHandler itemEnergyStorage = ItemAccess.forHandlerIndex(itemHandler, 10).getCapability(Capabilities.Energy.ITEM);
		if(itemEnergyStorage != null) {
			EnergyHandlerUtil.move(itemEnergyStorage, energyStorage, energyStorage.getMaxInsert(), null);
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
		if(isWorking) {
			return 1;
		}
		if(!itemHandler.getStack(9).isEmpty()) {
			return 15;
		}
		return 0;
	}

	public boolean canPushPattern() {
		return !isWorking && itemHandler.getStacks().subList(0, 9).stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ISettingsCloneable.Result loadConfig(ValueInput input, Player player) {
		mode = Mode.values()[input.getByteOr("mode", (byte)0)];
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(ValueOutput output, Player player) {
		output.putByte("mode", (byte)mode.ordinal());
		if(listStack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			List<IPackageRecipeInfo> recipeList = listStack.get(PackagedAutoDataComponents.RECIPE_LIST);
			if(!recipeList.isEmpty()) {
				MiscHelper.INSTANCE.serializeRecipeList(recipeList, output.childrenList("recipes"));
			}
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		mode = Mode.values()[input.getByteOr("mode", (byte)0)];
		super.loadAdditional(input);
		updatePatternList();
		isWorking = input.getBooleanOr("working", false);
		remainingProgress = input.getIntOr("progress", 0);
		powered = input.getBooleanOr("powered", false);
		currentPattern = input.read("pattern", IPackagePattern.CODEC).orElse(null);
		lockPattern = currentPattern != null;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("mode", (byte)mode.ordinal());
		output.putBoolean("working", isWorking);
		output.putInt("progress", remainingProgress);
		output.putBoolean("powered", powered);
		if(lockPattern) {
			output.store("pattern", IPackagePattern.CODEC, currentPattern);
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
		return new PackagerExtensionMenu(windowId, inventory, this);
	}
}
