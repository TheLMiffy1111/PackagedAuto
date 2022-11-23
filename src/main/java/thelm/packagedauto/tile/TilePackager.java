package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.gui.GuiPackager;
import thelm.packagedauto.container.ContainerPackager;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.networking.HostHelperTilePackager;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternHelper;
import thelm.packagedauto.inventory.InventoryPackager;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.crafting.ICraftingProvider", modid="appliedenergistics2")
})
public class TilePackager extends TileBase implements ITickable, IGridHost, IActionHost, ICraftingProvider {

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static boolean drawMEEnergy = true;

	public boolean isWorking = false;
	public int remainingProgress = 0;
	public List<IPackagePattern> patternList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public boolean lockPattern = false;
	public Mode mode = Mode.EXACT;
	public boolean disjoint = false;
	public boolean powered = false;

	public TilePackager() {
		setInventory(new InventoryPackager(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperTilePackager(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.packager.name");
	}

	@Override
	public void onLoad() {
		updatePowered();
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isInputValid()) {
					energyStorage.receiveEnergy(Math.abs(remainingProgress), false);
					finishProcess();
					if(hostHelper != null && hostHelper.isActive() && !inventory.getStackInSlot(9).isEmpty()) {
						hostHelper.ejectItem();
					}
					else if(!inventory.getStackInSlot(9).isEmpty()) {
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
			else if(world.getTotalWorldTime() % 8 == 0) {
				if(canStart()) {
					startProcess();
					tickProcess();
					isWorking = true;
				}
			}
			chargeEnergy();
			if(world.getTotalWorldTime() % 8 == 0) {
				if(hostHelper != null && hostHelper.isActive()) {
					if(!inventory.getStackInSlot(9).isEmpty()) {
						hostHelper.ejectItem();
					}
					if(drawMEEnergy) {
						hostHelper.chargeEnergy();
					}
				}
				else if(!inventory.getStackInSlot(9).isEmpty()) {
					ejectItem();
				}
			}
			energyStorage.updateIfChanged();
		}
	}

	protected static Ingredient getIngredient(ItemStack stack) {
		return stack.hasTagCompound() ? new IngredientNBT(stack) {} : Ingredient.fromStacks(stack);
	}

	public boolean isInputValid() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			return false;
		}
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return false;
		}
		if(!lockPattern && disjoint) {
			return MiscUtil.removeExactSet(input, currentPattern.getInputs(), true);
		}
		List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), TilePackager::getIngredient);
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
		ItemStack slotStack = inventory.getStackInSlot(9);
		ItemStack outputStack = currentPattern.getOutput();
		return slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && slotStack.getItemDamage() == outputStack.getItemDamage() && ItemStack.areItemStackShareTagsEqual(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
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
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return;
		}
		for(IPackagePattern pattern : patternList) {
			if(disjoint) {
				if(MiscUtil.removeExactSet(input, pattern.getInputs(), true)) {
					currentPattern = pattern;
					return;
				}
			}
			else {
				List<Ingredient> matchers = Lists.transform(pattern.getInputs(), TilePackager::getIngredient);
				int[] matches = RecipeMatcher.findMatches(input, matchers);
				if(matches != null) {
					currentPattern = pattern;
					return;
				}
			}
		}
	}

	protected void tickProcess() {
		int energy = energyStorage.extractEnergy(energyUsage, false);
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
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			endProcess();
			return;
		}
		if(!lockPattern && disjoint) {
			if(!MiscUtil.removeExactSet(input, currentPattern.getInputs(), true)) {
				endProcess();
				return;
			}
			if(inventory.getStackInSlot(9).isEmpty()) {
				inventory.setInventorySlotContents(9, currentPattern.getOutput());
			}
			else if(inventory.getStackInSlot(9).getItem() instanceof IPackageItem) {
				inventory.getStackInSlot(9).grow(1);
			}
			else {
				endProcess();
				return;
			}
			MiscUtil.removeExactSet(input, currentPattern.getInputs(), false);
		}
		else {
			List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), TilePackager::getIngredient);
			int[] matches = RecipeMatcher.findMatches(input, matchers);
			if(matches == null) {
				endProcess();
				return;
			}
			if(inventory.getStackInSlot(9).isEmpty()) {
				inventory.setInventorySlotContents(9, currentPattern.getOutput());
			}
			else if(inventory.getStackInSlot(9).getItem() instanceof IPackageItem) {
				inventory.getStackInSlot(9).grow(1);
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
			if(inventory.getStackInSlot(i).isEmpty()) {
				inventory.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
	}

	protected void startProcess() {
		remainingProgress = energyReq;
		markDirty();
	}

	public void endProcess() {
		remainingProgress = 0;
		isWorking = false;
		lockPattern = false;
		markDirty();
	}

	protected void ejectItem() {
		for(EnumFacing facing : EnumFacing.VALUES) {
			TileEntity te = world.getTileEntity(pos.offset(facing));
			if(te instanceof TileUnpackager) {
				TileUnpackager tile = (TileUnpackager)te;
				ItemStack stack = inventory.getStackInSlot(9);
				if(!stack.isEmpty()) {
					IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
					for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
						ItemStack stackRem = itemHandler.insertItem(slot, stack, false);
						if(stackRem.getCount() < stack.getCount()) {
							stack = stackRem;
						}
						if(stack.isEmpty()) {
							break;
						}
					}
					inventory.setInventorySlotContents(9, stack);
				}
			}
		}
	}

	protected void chargeEnergy() {
		int prevStored = energyStorage.getEnergyStored();
		ItemStack energyStack = inventory.getStackInSlot(11);
		if(energyStack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				inventory.setInventorySlotContents(11, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(world.getRedstonePowerFromNeighbors(pos) > 0 != powered) {
			powered = !powered;
			syncTile(false);
			markDirty();
		}
	}

	public HostHelperTilePackager hostHelper;

	@Override
	public void invalidate() {
		super.invalidate();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void setPlacer(EntityPlayer placer) {
		placerID = AEApi.instance().registries().players().getID(placer);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		return getActionableNode();
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void securityBreak() {
		world.destroyBlock(pos, true);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public IGridNode getActionableNode() {
		return hostHelper.getNode();
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		if(!isBusy() && patternDetails instanceof PackageCraftingPatternHelper) {
			PackageCraftingPatternHelper pattern = (PackageCraftingPatternHelper)patternDetails;
			ItemStack slotStack = inventory.getStackInSlot(9);
			ItemStack outputStack = pattern.pattern.getOutput();
			if(slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && slotStack.getItemDamage() == outputStack.getItemDamage() && ItemStack.areItemStackShareTagsEqual(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize()) {
				currentPattern = pattern.pattern;
				lockPattern = true;
				for(int i = 0; i < table.getSizeInventory() && i < 9; ++i) {
					inventory.setInventorySlotContents(i, table.getStackInSlot(i).copy());
				}
				return true;
			}
		}
		return false;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean isBusy() {
		return isWorking || !inventory.stacks.subList(0, 9).stream().allMatch(ItemStack::isEmpty);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ItemStack listStack = inventory.getStackInSlot(10);
		for(IPackagePattern pattern : patternList) {
			craftingTracker.addCraftingOption(this, new PackageCraftingPatternHelper(listStack, pattern));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lockPattern = false;
		currentPattern = null;
		if(nbt.hasKey("Pattern")) {
			NBTTagCompound tag = nbt.getCompoundTag("Pattern");
			IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(tag);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("Index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
					lockPattern = true;
				}
			}
		}
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(lockPattern) {
			NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), currentPattern.getRecipeInfo());
			tag.setByte("Index", (byte)currentPattern.getIndex());
			nbt.setTag("Pattern", tag);
		}
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
		}
		return nbt;
	}

	@Override
	public void readSyncNBT(NBTTagCompound nbt) {
		super.readSyncNBT(nbt);
		isWorking = nbt.getBoolean("Working");
		remainingProgress = nbt.getInteger("Progress");
		powered = nbt.getBoolean("Powered");
		mode = Mode.values()[nbt.getByte("Mode")];
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
		super.writeSyncNBT(nbt);
		nbt.setBoolean("Working", isWorking);
		nbt.setInteger("Progress", remainingProgress);
		nbt.setBoolean("Powered", powered);
		nbt.setByte("Mode", (byte)mode.ordinal());
		return nbt;
	}

	public void changePackagingMode() {
		mode = Mode.values()[((mode.ordinal()+1) % 3)];
		markDirty();
	}

	@Override
	public void markDirty() {
		if(isWorking && !isInputValid()) {
			endProcess();
		}
		super.markDirty();
	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
	}

	public int getScaledProgress(int scale) {
		if(remainingProgress <= 0) {
			return 0;
		}
		return scale * (energyReq-remainingProgress) / energyReq;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiPackager(new ContainerPackager(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerPackager(player.inventory, this);
	}

	public static enum Mode {
		EXACT, DISJOINT, FIRST;

		public String getTooltip() {
			return I18n.translateToLocal("tile.packagedauto.packager.mode."+name().toLowerCase(Locale.US));
		}
	}
}
