package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.client.gui.GuiUnpackager;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.AppEngHelper;
import thelm.packagedauto.integration.appeng.networking.HostHelperUnpackager;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternDetails;
import thelm.packagedauto.inventory.InventoryUnpackager;
import thelm.packagedauto.util.MiscHelper;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.crafting.ICraftingProvider", modid="appliedenergistics2")
})
public class TileUnpackager extends TileBase implements IGridHost, IActionHost, ICraftingProvider {

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static boolean drawMEEnergy = true;

	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;
	public boolean firstTick = true;

	public TileUnpackager() {
		setInventory(new InventoryUnpackager(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i] = new PackageTracker();
		}
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperUnpackager(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return StatCollector.translateToLocal("tile.packagedauto.unpackager.name");
	}

	@Override
	public void updateEntity() {
		if(firstTick) {
			firstTick = false;
			updatePowered();
		}
		if(!worldObj.isRemote) {
			chargeEnergy();
			if(worldObj.getTotalWorldTime() % 8 == 0) {
				fillTrackers();
				emptyTrackers();
				if(drawMEEnergy && hostHelper != null && hostHelper.isActive()) {
					hostHelper.chargeEnergy();
				}
			}
			energyStorage.updateIfChanged();
		}
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).filter(t->t.isEmpty()).collect(Collectors.toList());
		List<PackageTracker> nonEmptyTrackers = Arrays.stream(trackers).filter(t->!t.isEmpty()).filter(t->!t.isFilled()).collect(Collectors.toList());
		for(int i = 0; i < 9; ++i) {
			if(energyStorage.getEnergyStored() >= energyUsage) {
				ItemStack stack = inventory.getStackInSlot(i);
				if(stack != null && stack.getItem() instanceof IPackageItem) {
					IPackageItem packageItem = (IPackageItem)stack.getItem();
					boolean flag = false;
					for(PackageTracker tracker : nonEmptyTrackers) {
						if(tracker.tryAcceptPackage(packageItem, stack, i)) {
							flag = true;
							stack.stackSize -= 1;
							if(stack.stackSize <= 0) {
								inventory.setInventorySlotContents(i, null);
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
								stack.stackSize -= 1;
								if(stack.stackSize <= 0) {
									inventory.setInventorySlotContents(i, null);
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
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			if(tile instanceof IPackageCraftingMachine) {
				IPackageCraftingMachine machine = (IPackageCraftingMachine)tile;
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), side.getOpposite())) {
							tracker.clearRecipe();
							syncTile();
							markDirty();
							break;
						}
					}
				}
				continue;
			}
		}
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.facing == side).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			if(trackerToEmpty.recipe != null && trackerToEmpty.recipe.getRecipeType().hasMachine()) {
				trackerToEmpty.facing = null;
				continue;
			}
			if(tile == null || tile instanceof TilePackager || tile instanceof TileUnpackager || isInterface(tile, side.getOpposite()) || !(tile instanceof IInventory)) {
				trackerToEmpty.facing = null;
				continue;
			}
			IInventory inv = (IInventory)tile;
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				for(int slot : MiscHelper.INSTANCE.getSlots(inv, side.getOpposite())) {
					ItemStack stackRem = MiscHelper.INSTANCE.insertItem(inv, slot, side.getOpposite(), stack, false);
					if(stackRem == null || stackRem.stackSize < stack.stackSize) {
						stack = stackRem;
					}
					if(stack == null) {
						break;
					}
				}
				trackerToEmpty.toSend.set(i, stack);
			}
			trackerToEmpty.toSend.removeIf(Objects::isNull);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			markDirty();
		}
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			if(tile == null || tile instanceof TilePackager || tile instanceof TileUnpackager || isInterface(tile, side.getOpposite()) || !(tile instanceof IInventory)) {
				continue;
			}
			IInventory inv = (IInventory)tile;
			if(powered || blocking && !MiscHelper.INSTANCE.isEmpty(inv, side.getOpposite())) {
				continue;
			}
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled()).filter(t->t.facing == null).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			if(trackerToEmpty.recipe != null && trackerToEmpty.recipe.getRecipeType().hasMachine()) {
				continue;
			}
			boolean inserted = false;
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				for(int slot : MiscHelper.INSTANCE.getSlots(inv, side.getOpposite())) {
					ItemStack stackRem = MiscHelper.INSTANCE.insertItem(inv, slot, side.getOpposite(), stack, false);
					if(stackRem == null || stackRem.stackSize < stack.stackSize) {
						stack = stackRem;
						inserted = true;
					}
					if(stack == null) {
						break;
					}
				}
				trackerToEmpty.toSend.set(i, stack);
			}
			trackerToEmpty.toSend.removeIf(Objects::isNull);
			if(inserted) {
				trackerToEmpty.facing = side;
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.clearRecipe();
				}
				markDirty();
			}
		}
	}

	protected void chargeEnergy() {
		int prevStored = energyStorage.getEnergyStored();
		ItemStack energyStack = inventory.getStackInSlot(10);
		if(energyStack != null && energyStack.getItem() instanceof IEnergyContainerItem) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(((IEnergyContainerItem)energyStack.getItem()).extractEnergy(energyStack, energyRequest, false), false);
			if(energyStack.stackSize <= 0) {
				inventory.setInventorySlotContents(10, null);
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

	public HostHelperUnpackager hostHelper;

	@Override
	public void invalidate() {
		super.invalidate();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
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
		if(!isBusy() && patternDetails instanceof RecipeCraftingPatternDetails) {
			List<Integer> emptySlots = new ArrayList<>();
			for(int i = 0; i < 9; ++i) {
				if(inventory.getStackInSlot(i) == null) {
					emptySlots.add(i);
				}
			}
			List<Integer> requiredSlots = new ArrayList<>();
			for(int i = 0; i < table.getSizeInventory(); ++i) {
				if(table.getStackInSlot(i) != null) {
					requiredSlots.add(i);
				}
			}
			if(requiredSlots.size() > emptySlots.size()) {
				return false;
			}
			for(int i = 0; i < requiredSlots.size(); ++i) {
				ItemStack stack = table.getStackInSlot(requiredSlots.get(i));
				inventory.setInventorySlotContents(emptySlots.get(i), stack == null ? null : stack.copy());
			}
			return true;
		}
		return false;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean isBusy() {
		return Arrays.stream(trackers).noneMatch(PackageTracker::isEmpty);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ItemStack patternStack = inventory.getStackInSlot(9);
		for(IPackageRecipeInfo pattern : recipeList) {
			if(!pattern.getOutputs().isEmpty()) {
				craftingTracker.addCraftingOption(this, new RecipeCraftingPatternDetails(patternStack, pattern));
			}
		}
	}

	protected boolean isInterface(TileEntity tile, ForgeDirection side) {
		if(Loader.isModLoaded("appliedenergistics2")) {
			return AppEngHelper.INSTANCE.isInterface(tile, side);
		}
		return false;
	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
		}
	}

	@Override
	public void readSyncNBT(NBTTagCompound nbt) {
		super.readSyncNBT(nbt);
		blocking = nbt.getBoolean("Blocking");
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].readFromNBT(nbt.getCompoundTag(String.format("Tracker%02d", i)));
		}
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
		super.writeSyncNBT(nbt);
		nbt.setBoolean("Blocking", blocking);
		nbt.setBoolean("Powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			NBTTagCompound subNBT = new NBTTagCompound();
			trackers[i].writeToNBT(subNBT);
			nbt.setTag(String.format("Tracker%02d", i), subNBT);
		}
		return nbt;
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		markDirty();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiUnpackager(new ContainerUnpackager(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerUnpackager(player.inventory, this);
	}

	public class PackageTracker {

		public boolean[] rejectedIndexes = new boolean[9];
		public IPackageRecipeInfo recipe;
		public int amount;
		public List<Boolean> received = new ArrayList<>();
		public List<ItemStack> toSend = new ArrayList<>();
		public ForgeDirection facing;

		public void setRecipe(IPackageRecipeInfo recipe) {
			this.recipe = recipe;
		}

		public void clearRecipe() {
			clearRejectedIndexes();
			recipe = null;
			amount = 0;
			received.clear();
			facing = null;
			if(worldObj != null && !worldObj.isRemote) {
				syncTile();
				markDirty();
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
					while(received.size() < amount) {
						received.add(false);
					}
					received.set(packageItem.getIndex(stack), true);
					syncTile();
					markDirty();
					return true;
				}
				else if(this.recipe.equals(recipe)) {
					int index = packageItem.getIndex(stack);
					if(!received.get(index)) {
						received.set(index, true);
						syncTile();
						markDirty();
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

		public void readFromNBT(NBTTagCompound nbt) {
			clearRecipe();
			NBTTagCompound tag = nbt.getCompoundTag("Recipe");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipeFromNBT(tag);
			if(recipe != null) {
				this.recipe = recipe;
				amount = nbt.getByte("Amount");
				while(received.size() < amount) {
					received.add(false);
				}
				byte[] receivedArray = nbt.getByteArray("Received");
				for(int i = 0; i < received.size(); ++i) {
					received.set(i, receivedArray[i] != 0);
				}
			}
			MiscHelper.INSTANCE.loadAllItems(nbt.getTagList("ToSend", 10), toSend);
			if(nbt.hasKey("Facing")) {
				facing = ForgeDirection.getOrientation(nbt.getByte("Facing"));
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			if(recipe != null) {
				NBTTagCompound tag = MiscHelper.INSTANCE.writeRecipeToNBT(new NBTTagCompound(), recipe);
				nbt.setTag("Recipe", tag);
				nbt.setByte("Amount", (byte)amount);
				byte[] receivedArray = new byte[received.size()];
				for(int i = 0; i < received.size(); ++i) {
					receivedArray[i] = (byte)(received.get(i) ? 1 : 0);
				}
				nbt.setByteArray("Received", receivedArray);
			}
			nbt.setTag("ToSend", MiscHelper.INSTANCE.saveAllItems(new NBTTagList(), toSend));
			if(facing != null) {
				nbt.setByte("Facing", (byte)facing.ordinal());
			}
			return nbt;
		}
	}
}
