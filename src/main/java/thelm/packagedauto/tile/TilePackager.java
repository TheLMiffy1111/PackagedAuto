package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
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
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.client.gui.GuiPackager;
import thelm.packagedauto.container.ContainerPackager;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.networking.HostHelperPackager;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternDetails;
import thelm.packagedauto.inventory.InventoryPackager;
import thelm.packagedauto.util.MiscHelper;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.crafting.ICraftingProvider", modid="appliedenergistics2")
})
public class TilePackager extends TileBase implements IGridHost, IActionHost, ICraftingProvider {

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
	public boolean firstTick = true;

	public TilePackager() {
		setInventory(new InventoryPackager(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperPackager(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return StatCollector.translateToLocal("tile.packagedauto.packager.name");
	}

	@Override
	public void updateEntity() {
		if(firstTick) {
			firstTick = false;
			updatePowered();
		}
		if(!worldObj.isRemote) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isInputValid()) {
					finishProcess();
					if(hostHelper != null && hostHelper.isActive() && inventory.getStackInSlot(9) != null) {
						hostHelper.ejectItem();
					}
					else if(inventory.getStackInSlot(9) != null) {
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
			else if(worldObj.getTotalWorldTime() % 8 == 0) {
				if(canStart()) {
					startProcess();
					tickProcess();
					isWorking = true;
				}
			}
			chargeEnergy();
			if(worldObj.getTotalWorldTime() % 8 == 0) {
				if(hostHelper != null && hostHelper.isActive()) {
					if(inventory.getStackInSlot(9) != null) {
						hostHelper.ejectItem();
					}
					if(drawMEEnergy) {
						hostHelper.chargeEnergy();
					}
				}
				else if(inventory.getStackInSlot(9) != null) {
					ejectItem();
				}
			}
			energyStorage.updateIfChanged();
		}
	}

	protected static Predicate<ItemStack> getIngredient(ItemStack stack) {
		return s->stack != null && s != null && stack.getItem() == s.getItem() && stack.getItemDamage() == s.getItemDamage() && (!stack.hasTagCompound() || ItemStack.areItemStackTagsEqual(stack, s));
	}

	public boolean isInputValid() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			return false;
		}
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(Objects::nonNull).collect(Collectors.toList());
		if(input.isEmpty()) {
			return false;
		}
		if(!lockPattern && disjoint) {
			return MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true);
		}
		List<Predicate<ItemStack>> matchers = Lists.transform(currentPattern.getInputs(), TilePackager::getIngredient);
		int[] matches = MiscHelper.INSTANCE.findMatches(input, matchers);
		if(matches == null) {
			return false;
		}
		for(int i = 0; i < matches.length; ++i) {
			if(input.get(i).stackSize < currentPattern.getInputs().get(matches[i]).stackSize) {
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
		return slotStack == null || slotStack.getItem() == outputStack.getItem() && slotStack.getItemDamage() == outputStack.getItemDamage() && ItemStack.areItemStackTagsEqual(slotStack, outputStack) && slotStack.stackSize+1 <= outputStack.getMaxStackSize();
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
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(Objects::nonNull).collect(Collectors.toList());
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
				List<Predicate<ItemStack>> matchers = Lists.transform(pattern.getInputs(), TilePackager::getIngredient);
				int[] matches = MiscHelper.INSTANCE.findMatches(input, matchers);
				if(matches != null) {
					currentPattern = pattern;
					return;
				}
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
		List<ItemStack> input = inventory.stacks.subList(0, 9).stream().filter(Objects::nonNull).collect(Collectors.toList());
		if(input.isEmpty()) {
			endProcess();
			return;
		}
		if(!lockPattern && disjoint) {
			if(!MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true)) {
				endProcess();
				return;
			}
			if(inventory.getStackInSlot(9) == null) {
				inventory.setInventorySlotContents(9, currentPattern.getOutput());
			}
			else if(inventory.getStackInSlot(9).getItem() instanceof IPackageItem) {
				inventory.getStackInSlot(9).stackSize += 1;
			}
			else {
				endProcess();
				return;
			}
			MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), false);
		}
		else {
			List<Predicate<ItemStack>> matchers = Lists.transform(currentPattern.getInputs(), TilePackager::getIngredient);
			int[] matches = MiscHelper.INSTANCE.findMatches(input, matchers);
			if(matches == null) {
				endProcess();
				return;
			}
			if(inventory.getStackInSlot(9) == null) {
				inventory.setInventorySlotContents(9, currentPattern.getOutput());
			}
			else if(inventory.getStackInSlot(9).getItem() instanceof IPackageItem) {
				inventory.getStackInSlot(9).stackSize += 1;
			}
			else {
				endProcess();
				return;
			}
			for(int i = 0; i < matches.length; ++i) {
				input.get(i).stackSize -= currentPattern.getInputs().get(matches[i]).stackSize;
			}
		}
		for(int i = 0; i < 9; ++i) {
			if(inventory.getStackInSlot(i) == null || inventory.getStackInSlot(i).stackSize <= 0) {
				inventory.setInventorySlotContents(i, null);
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
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity te = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			if(te instanceof TileUnpackager) {
				TileUnpackager tile = (TileUnpackager)te;
				ItemStack stack = inventory.getStackInSlot(9);
				if(stack != null) {
					IInventory inv = (IInventory)tile;
					for(int slot : MiscHelper.INSTANCE.getSlots(inv, side.getOpposite())) {
						ItemStack stackRem = MiscHelper.INSTANCE.insertItem(inv, slot, side.getOpposite(), stack, false);
						if(stackRem == null || stackRem.stackSize < stack.stackSize) {
							stack = stackRem;
						}
						if(stack == null) {
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
		if(energyStack != null && energyStack.getItem() instanceof IEnergyContainerItem) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(((IEnergyContainerItem)energyStack.getItem()).extractEnergy(energyStack, energyRequest, false), false);
			if(energyStack.stackSize <= 0) {
				inventory.setInventorySlotContents(11, null);
			}
		}
	}

	public void updatePowered() {
		if(worldObj.getStrongestIndirectPower(xCoord, yCoord, zCoord) > 0 != powered) {
			powered = !powered;
			syncTile();
			markDirty();
		}
	}

	public HostHelperPackager hostHelper;

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
	public IGridNode getGridNode(ForgeDirection dir) {
		return getActionableNode();
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.SMART;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void securityBreak() {
		worldObj.func_147480_a(xCoord, yCoord, zCoord, true);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public IGridNode getActionableNode() {
		return hostHelper.getNode();
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		if(!isBusy() && patternDetails instanceof PackageCraftingPatternDetails) {
			PackageCraftingPatternDetails pattern = (PackageCraftingPatternDetails)patternDetails;
			ItemStack slotStack = inventory.getStackInSlot(9);
			ItemStack outputStack = pattern.pattern.getOutput();
			if(slotStack == null || slotStack.getItem() == outputStack.getItem() && slotStack.getItemDamage() == outputStack.getItemDamage() && ItemStack.areItemStackTagsEqual(slotStack, outputStack) && slotStack.stackSize+1 <= outputStack.getMaxStackSize()) {
				currentPattern = pattern.pattern;
				lockPattern = true;
				for(int i = 0; i < table.getSizeInventory() && i < 9; ++i) {
					ItemStack stack = table.getStackInSlot(i);
					inventory.setInventorySlotContents(i, stack == null ? null : stack.copy());
				}
				return true;
			}
		}
		return false;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean isBusy() {
		return isWorking || !inventory.stacks.subList(0, 9).stream().allMatch(Objects::isNull);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ItemStack listStack = inventory.getStackInSlot(10);
		for(IPackagePattern pattern : patternList) {
			craftingTracker.addCraftingOption(this, new PackageCraftingPatternDetails(listStack, pattern));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lockPattern = false;
		currentPattern = null;
		if(nbt.hasKey("Pattern")) {
			NBTTagCompound tag = nbt.getCompoundTag("Pattern");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipeFromNBT(tag);
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
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(lockPattern) {
			NBTTagCompound tag = MiscHelper.INSTANCE.writeRecipeToNBT(new NBTTagCompound(), currentPattern.getRecipeInfo());
			tag.setByte("Index", (byte)currentPattern.getIndex());
			nbt.setTag("Pattern", tag);
		}
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
		}
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
			return StatCollector.translateToLocal("tile.packagedauto.packager.mode."+name().toLowerCase(Locale.US));
		}
	}
}
