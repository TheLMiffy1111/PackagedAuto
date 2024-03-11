package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.gui.GuiUnpackager;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.networking.HostHelperTileUnpackager;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper;
import thelm.packagedauto.inventory.InventoryUnpackager;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.crafting.ICraftingProvider", modid="appliedenergistics2")
})
public class TileUnpackager extends TileBase implements ITickable, IGridHost, IActionHost, ICraftingProvider {

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static boolean drawMEEnergy = true;

	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;

	public TileUnpackager() {
		setInventory(new InventoryUnpackager(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i] = new PackageTracker();
		}
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperTileUnpackager(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.unpackager.name");
	}

	@Override
	public void onLoad() {
		updatePowered();
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			chargeEnergy();
			if(world.getTotalWorldTime() % 8 == 0) {
				fillTrackers();
				emptyTrackers();
				if(drawMEEnergy && hostHelper != null && hostHelper.isActive()) {
					hostHelper.chargeEnergy();
				}
			}
		}
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).filter(t->t.isEmpty()).collect(Collectors.toList());
		List<PackageTracker> nonEmptyTrackers = Arrays.stream(trackers).filter(t->!t.isEmpty()).filter(t->!t.isFilled()).collect(Collectors.toList());
		for(int i = 0; i < 9; ++i) {
			if(energyStorage.getEnergyStored() >= energyUsage) {
				ItemStack stack = inventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() instanceof IPackageItem) {
					IPackageItem packageItem = (IPackageItem)stack.getItem();
					boolean flag = false;
					for(PackageTracker tracker : nonEmptyTrackers) {
						if(tracker.tryAcceptPackage(packageItem, stack, i)) {
							flag = true;
							stack.shrink(1);
							if(stack.isEmpty()) {
								inventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
									inventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
		for(EnumFacing facing : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if(tile instanceof IPackageCraftingMachine) {
				IPackageCraftingMachine machine = (IPackageCraftingMachine)tile;
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), facing.getOpposite())) {
							tracker.clearRecipe();
							markDirty();
							break;
						}
					}
				}
				continue;
			}
		}
		for(EnumFacing facing : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.facing == facing).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			boolean ordered = false;
			if(trackerToEmpty.recipe != null) {
				IRecipeType recipeType = trackerToEmpty.recipe.getRecipeType();
				if(recipeType.hasMachine()) {
					trackerToEmpty.facing = null;
					continue;
				}
				ordered = recipeType.isOrdered();
			}
			if(tile == null || tile instanceof TilePackager || tile instanceof TileUnpackager || isInterface(tile, facing.getOpposite()) || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
				trackerToEmpty.facing = null;
				continue;
			}
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				ItemStack stackRem = MiscUtil.insertItem(itemHandler, stack, ordered, false);
				trackerToEmpty.toSend.set(i, stackRem);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			markDirty();
		}
		if(powered) {
			return;
		}
		for(EnumFacing facing : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if(tile == null || tile instanceof TilePackager || tile instanceof TileUnpackager || isInterface(tile, facing.getOpposite()) || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
				continue;
			}
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			if(blocking && !MiscUtil.isEmpty(itemHandler)) {
				continue;
			}
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled() && t.facing == null && t.recipe != null && !t.recipe.getRecipeType().hasMachine()).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			boolean ordered = trackerToEmpty.recipe.getRecipeType().isOrdered();
			boolean inserted = false;
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				ItemStack stackRem = MiscUtil.insertItem(itemHandler, stack, ordered, false);
				inserted |= stackRem.getCount() < stack.getCount();
				trackerToEmpty.toSend.set(i, stackRem);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(inserted) {
				trackerToEmpty.facing = facing;
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.clearRecipe();
				}
				markDirty();
			}
		}
	}

	protected void chargeEnergy() {
		ItemStack energyStack = inventory.getStackInSlot(10);
		if(energyStack.hasCapability(CapabilityEnergy.ENERGY, null)) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(CapabilityEnergy.ENERGY, null).extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				inventory.setInventorySlotContents(10, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(world.getRedstonePowerFromNeighbors(pos) > 0 != powered) {
			powered = !powered;
			markDirty();
		}
	}

	@Override
	public int getComparatorSignal() {
		return Math.min((int)Arrays.stream(trackers).filter(t->t.isFilled()).count(), 15);
	}

	public HostHelperTileUnpackager hostHelper;

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
		if(!isBusy() && patternDetails instanceof RecipeCraftingPatternHelper) {
			IntList emptySlots = new IntArrayList();
			for(int i = 0; i < 9; ++i) {
				if(inventory.getStackInSlot(i).isEmpty()) {
					emptySlots.add(i);
				}
			}
			IntList requiredSlots = new IntArrayList();
			for(int i = 0; i < table.getSizeInventory(); ++i) {
				if(!table.getStackInSlot(i).isEmpty()) {
					requiredSlots.add(i);
				}
			}
			if(requiredSlots.size() > emptySlots.size()) {
				return false;
			}
			for(int i = 0; i < requiredSlots.size(); ++i) {
				inventory.setInventorySlotContents(emptySlots.getInt(i), table.getStackInSlot(requiredSlots.getInt(i)).copy());
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
		for(IRecipeInfo pattern : recipeList) {
			if(!pattern.getOutputs().isEmpty()) {
				craftingTracker.addCraftingOption(this, new RecipeCraftingPatternHelper(patternStack, pattern));
			}
		}
	}

	protected boolean isInterface(TileEntity tile, EnumFacing facing) {
		if(Loader.isModLoaded("appliedenergistics2")) {
			return AppEngUtil.isInterface(tile, facing);
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
		blocking = nbt.getBoolean("Blocking");
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].readFromNBT(nbt.getCompoundTag(String.format("Tracker%02d", i)));
		}
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("Blocking", blocking);
		nbt.setBoolean("Powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			NBTTagCompound subNBT = new NBTTagCompound();
			trackers[i].writeToNBT(subNBT);
			nbt.setTag(String.format("Tracker%02d", i), subNBT);
		}
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
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
		public IRecipeInfo recipe;
		public int amount;
		public BooleanList received = new BooleanArrayList();
		public List<ItemStack> toSend = new ArrayList<>();
		public EnumFacing facing;

		public void setRecipe(IRecipeInfo recipe) {
			this.recipe = recipe;
		}

		public void clearRecipe() {
			clearRejectedIndexes();
			recipe = null;
			amount = 0;
			received.clear();
			facing = null;
			if(world != null && !world.isRemote) {
				markDirty();
			}
		}

		public boolean tryAcceptPackage(IPackageItem packageItem, ItemStack stack, int invIndex) {
			if(rejectedIndexes[invIndex]) {
				return false;
			}
			IRecipeInfo recipe = packageItem.getRecipeInfo(stack);
			int index = packageItem.getIndex(stack);
			if(recipe != null && recipe.validPatternIndex(index)) {
				if(this.recipe == null) {
					this.recipe = recipe;
					amount = recipe.getPatterns().size();
					received.size(amount);
					received.set(index, true);
					markDirty();
					return true;
				}
				else if(this.recipe.equals(recipe)) {
					if(!received.get(index)) {
						received.set(index, true);
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
			IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(tag);
			if(recipe != null) {
				this.recipe = recipe;
				amount = nbt.getByte("Amount");
				received.size(amount);
				byte[] receivedArray = nbt.getByteArray("Received");
				for(int i = 0; i < received.size(); ++i) {
					received.set(i, receivedArray[i] != 0);
				}
			}
			MiscUtil.loadAllItems(nbt.getTagList("ToSend", 10), toSend);
			if(nbt.hasKey("Facing")) {
				facing = EnumFacing.byIndex(nbt.getByte("Facing"));
			}
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			if(recipe != null) {
				NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), recipe);
				nbt.setTag("Recipe", tag);
				nbt.setByte("Amount", (byte)amount);
				byte[] receivedArray = new byte[received.size()];
				for(int i = 0; i < received.size(); ++i) {
					receivedArray[i] = (byte)(received.getBoolean(i) ? 1 : 0);
				}
				nbt.setByteArray("Received", receivedArray);
			}
			nbt.setTag("ToSend", MiscUtil.saveAllItems(new NBTTagList(), toSend));
			if(facing != null) {
				nbt.setByte("Facing", (byte)facing.getIndex());
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
