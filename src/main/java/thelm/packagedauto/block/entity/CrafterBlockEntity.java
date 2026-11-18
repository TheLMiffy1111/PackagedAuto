package thelm.packagedauto.block.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.CrafterItemResourceHandler;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.recipe.ICraftingPackageRecipeInfo;

public class CrafterBlockEntity extends BaseBlockEntity implements IPackageCraftingMachine {

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static int refreshInterval = 4;
	public static boolean drawMEEnergy = true;

	public final ResourceHandler<ItemResource> ejectSlots;
	public final ResourceHandler<ItemResource> outputSlot;

	public boolean isWorking = false;
	public int remainingProgress = 0;
	public ICraftingPackageRecipeInfo currentRecipe;

	public CrafterBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.CRAFTER.get(), pos, state);
		setItemHandler(new CrafterItemResourceHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		ejectSlots = RangedResourceHandler.of(this::getItemHandler, 0, 10);
		outputSlot = RangedResourceHandler.ofSingleIndex(this::getItemHandler, 9);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.crafter");
	}

	@Override
	public void tick() {
		if(!level.isClientSide()) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0) {
					finishProcess();
					ejectItems();
				}
			}
			chargeEnergy();
			if(level.getGameTime() % refreshInterval == 0) {
				ejectItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction) {
		if(!isBusy() && recipeInfo.isValid() && recipeInfo instanceof ICraftingPackageRecipeInfo recipe) {
			ItemStack slotStack = itemHandler.getStack(9);
			ItemStack outputStack = recipe.getOutput();
			if(slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, outputStack) && slotStack.getCount()+outputStack.getCount() <= outputStack.getMaxStackSize()) {
				currentRecipe = recipe;
				isWorking = true;
				remainingProgress = energyReq;
				CraftingInput matrix = recipe.getMatrix();
				for(int i = 0; i < matrix.height(); ++i) {
					for(int j = 0; j < matrix.width(); ++j) {
						itemHandler.setStack(i*3+j, matrix.getItem(i*matrix.width()+j).copy());
					}
				}
				setChanged();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return isWorking || !itemHandler.getStacks().subList(0, 9).stream().allMatch(ItemStack::isEmpty);
	}

	protected void tickProcess() {
		try(Transaction transaction = Transaction.openRoot()) {
			int energy = energyStorage.extract(Math.min(energyUsage, remainingProgress), transaction);
			transaction.commit();
			remainingProgress -= energy;
		}
	}

	protected void finishProcess() {
		if(currentRecipe == null) {
			endProcess();
			return;
		}
		if(itemHandler.getStack(9).isEmpty()) {
			itemHandler.setStack(9, currentRecipe.getOutput());
		}
		else {
			itemHandler.getStack(9).grow(currentRecipe.getOutput().getCount());
		}
		List<ItemStack> remainingItems = currentRecipe.getRemainingItems();
		CraftingInput matrix = currentRecipe.getMatrix();
		for(int i = 0; i < matrix.height(); ++i) {
			for(int j = 0; j < matrix.width(); ++j) {
				itemHandler.setStack(i*3+j, remainingItems.get(i*matrix.width()+j));
			}
		}
		endProcess();
	}

	public void endProcess() {
		remainingProgress = 0;
		isWorking = false;
		currentRecipe = null;
		setChanged();
	}

	protected void ejectItems() {
		ResourceHandler<ItemResource> slots = isWorking ? outputSlot : ejectSlots;
		for(Direction direction : Direction.values()) {
			BlockPos offsetPos = worldPosition.relative(direction);
			Block block = level.getBlockState(offsetPos).getBlock();
			ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, offsetPos, direction.getOpposite());
			if(block != PackagedAutoBlocks.UNPACKAGER.get() && itemHandler != null) {
				int moveCount = 0;
				for(int i = 0; i < slots.size(); ++i) {
					moveCount += slots.getAmountAsInt(i);
				}
				if(moveCount > 0) {
					ResourceHandlerUtil.moveStacking(slots, itemHandler, _->true, moveCount, null);
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

	@Override
	public int getComparatorSignal() {
		if(isWorking) {
			return 1;
		}
		if(!itemHandler.getStacks().subList(0, 10).stream().allMatch(ItemStack::isEmpty)) {
			return 15;
		}
		return 0;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		isWorking = input.getBooleanOr("working", false);
		remainingProgress = input.getIntOr("progress", 0);
		currentRecipe = input.read("recipe", IPackageRecipeInfo.CODEC).
				filter(ICraftingPackageRecipeInfo.class::isInstance).
				map(ICraftingPackageRecipeInfo.class::cast).
				orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("working", isWorking);
		output.putInt("progress", remainingProgress);
		output.storeNullable("recipe", IPackageRecipeInfo.CODEC, currentRecipe);
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
		return new CrafterMenu(windowId, inventory, this);
	}
}
