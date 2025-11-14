package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
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
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageProvidingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.gui.GuiUnpackager;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.networking.HostHelperTileUnpackager;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper;
import thelm.packagedauto.inventory.InventoryUnpackager;
import thelm.packagedauto.item.ItemRecipeHolder;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.crafting.ICraftingProvider", modid="appliedenergistics2")
})
public class TileUnpackager extends TileBase implements ITickable, IPackageProvidingMachine, ISettingsCloneable, IGridHost, IActionHost, ICraftingProvider {

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static int refreshInterval = 4;
	public static boolean drawMEEnergy = true;

	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;
	public int trackerCount = 6;
	public int roundRobinIndex = 0;

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
	public String getConfigTypeName() {
		return "tile.packagedauto.unpackager.name";
	}

	@Override
	public void onLoad() {
		updatePowered();
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			chargeEnergy();
			if(world.getTotalWorldTime() % refreshInterval == 0) {
				fillTrackers();
				emptyTrackers();
				if(drawMEEnergy && hostHelper != null && hostHelper.isActive()) {
					hostHelper.chargeEnergy();
				}
			}
		}
	}

	@Override
	public ItemStack getPatternStack() {
		return inventory.getStackInSlot(9);
	}

	@Override
	public void setPatternStack(ItemStack stack) {
		inventory.setInventorySlotContents(9, stack);
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).limit(trackerCount).filter(t->t.isEmpty()).collect(Collectors.toList());
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
		List<EnumFacing> directions = Lists.newArrayList(EnumFacing.VALUES);
		Collections.rotate(directions, roundRobinIndex);
		for(EnumFacing facing : directions) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if(tile instanceof IPackageCraftingMachine) {
				IPackageCraftingMachine machine = (IPackageCraftingMachine)tile;
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), facing.getOpposite(), blocking)) {
							tracker.clearRecipe();
							roundRobinIndex = (roundRobinIndex+1) % 6;
							markDirty();
							break;
						}
					}
				}
				continue;
			}
		}
		if(!powered) {
			directions = Lists.newArrayList(EnumFacing.VALUES);
			Collections.rotate(directions, roundRobinIndex);
			for(EnumFacing facing : directions) {
				TileEntity tile = world.getTileEntity(pos.offset(facing));
				if(!validSendTarget(tile, facing.getOpposite())) {
					continue;
				}
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				if(itemHandler == null) {
					continue;
				}
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
				boolean acceptsAll = true;
				for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
					ItemStack stack = trackerToEmpty.toSend.get(i);
					ItemStack stackRem = MiscUtil.insertItem(itemHandler, stack, false, true);
					acceptsAll &= stackRem.getCount() < stack.getCount();
				}
				trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
				if(acceptsAll) {
					trackerToEmpty.facing = facing;
					roundRobinIndex = (roundRobinIndex+1) % 6;
				}
				markDirty();
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
				ordered = trackerToEmpty.recipe.getRecipeType().isOrdered();
			}
			if(!validSendTarget(tile, facing.getOpposite())) {
				trackerToEmpty.facing = null;
				continue;
			}
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			if(itemHandler == null) {
				trackerToEmpty.facing = null;
				continue;
			}
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
	}

	protected boolean validSendTarget(TileEntity tile, EnumFacing facing) {
		return tile != null &&
				!(tile instanceof IPackageCraftingMachine) &&
				!(tile instanceof TilePackager) &&
				!(tile instanceof TilePackagerExtension) &&
				!(tile instanceof TileUnpackager) &&
				!isInterface(tile, facing.getOpposite());
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
		if(hostHelper.isActive() && !isBusy() && patternDetails instanceof RecipeCraftingPatternHelper) {
			RecipeCraftingPatternHelper pattern = (RecipeCraftingPatternHelper)patternDetails;
			int energyReq = energyUsage*pattern.recipe.getPatterns().size();
			if(energyStorage.getEnergyStored() >= energyReq) {
				PackageTracker tracker = Arrays.stream(trackers).limit(trackerCount).filter(PackageTracker::isEmpty).findFirst().get();
				tracker.fillRecipe(pattern.recipe);
				energyStorage.extractEnergy(energyReq, false);
				return true;
			}
		}
		return false;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public boolean isBusy() {
		return Arrays.stream(trackers).limit(trackerCount).noneMatch(PackageTracker::isEmpty);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		if(hostHelper.isActive()) {
			for(IRecipeInfo pattern : recipeList) {
				if(!pattern.getOutputs().isEmpty()) {
					craftingTracker.addCraftingOption(this, new RecipeCraftingPatternHelper(pattern));
				}
			}
		}
	}

	@Optional.Method(modid="appliedenergistics2")
	@MENetworkEventSubscribe
	public void onChannelsChanged(MENetworkChannelsChanged event) {
		hostHelper.postPatternChange();
	}

	@Optional.Method(modid="appliedenergistics2")
	@MENetworkEventSubscribe
	public void onPowerStatusChange(MENetworkPowerStatusChange event) {
		hostHelper.postPatternChange();
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
	public ISettingsCloneable.Result loadConfig(NBTTagCompound nbt, EntityPlayer player) {
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.getByte("Trackers");
		ITextComponent message = null;
		if(nbt.hasKey("Recipes")) {
			f:if(inventory.getStackInSlot(9).isEmpty()) {
				InventoryPlayer playerInventory = player.inventory;
				for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
					ItemStack stack = playerInventory.getStackInSlot(i);
					if(!stack.isEmpty() && stack.getItem() == ItemRecipeHolder.INSTANCE && !stack.hasTagCompound()) {
						ItemStack stackCopy = stack.splitStack(1);
						IRecipeList recipeListObj = ItemRecipeHolder.INSTANCE.getRecipeList(stackCopy);
						List<IRecipeInfo> recipeList = MiscUtil.readRecipeListFromNBT(nbt.getTagList("Recipes", 10));
						recipeListObj.setRecipeList(recipeList);
						ItemRecipeHolder.INSTANCE.setRecipeList(stackCopy, recipeListObj);
						inventory.setInventorySlotContents(9, stackCopy);
						break f;
					}
				}
				message = new TextComponentTranslation("tile.packagedauto.unpackager.no_holders");
			}
			else {
				message = new TextComponentTranslation("tile.packagedauto.unpackager.holder_present");
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
	public ISettingsCloneable.Result saveConfig(NBTTagCompound nbt, EntityPlayer player) {
		nbt.setBoolean("Blocking", blocking);
		nbt.setByte("Trackers", (byte)trackerCount);
		if(!recipeList.isEmpty()) {
			nbt.setTag("Recipes", MiscUtil.writeRecipeListToNBT(new NBTTagList(), recipeList));
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
		super.readFromNBT(nbt);
		blocking = nbt.getBoolean("Blocking");
		trackerCount = nbt.hasKey("Trackers") ? nbt.getByte("Trackers") : 6;
		powered = nbt.getBoolean("Powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].readFromNBT(nbt.getCompoundTag(String.format("Tracker%02d", i)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("Blocking", blocking);
		nbt.setByte("Trackers", (byte)trackerCount);
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

	public void changeTrackerCount(boolean decrease) {
		trackerCount = MathHelper.clamp(trackerCount + (decrease ? -1 : 1), 1, 10);
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

		public void fillRecipe(IRecipeInfo recipe) {
			this.recipe = recipe;
			amount = recipe.getPatterns().size();
			received.clear();
			received.size(amount);
			for(int i = 0; i < received.size(); ++i) {
				received.set(i, true);
			}
			if(world != null && !world.isRemote) {
				markDirty();
			}
		}

		public void ejectItems() {
			if(world != null && !isEmpty()) {
				if(!toSend.isEmpty()) {
					for(ItemStack stack : toSend) {
						if(!stack.isEmpty()) {
							double dx = world.rand.nextFloat()/2+0.25;
							double dy = world.rand.nextFloat()/2+0.75;
							double dz = world.rand.nextFloat()/2+0.25;
							EntityItem entityitem = new EntityItem(world, pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz, stack);
							entityitem.setDefaultPickupDelay();
							world.spawnEntity(entityitem);
						}
					}
				}
				else {
					List<IPackagePattern> patterns = recipe.getPatterns();
					for(int i = 0; i < received.size() && i < patterns.size(); ++i) {
						if(received.getBoolean(i)) {
							double dx = world.rand.nextFloat()/2+0.25;
							double dy = world.rand.nextFloat()/2+0.75;
							double dz = world.rand.nextFloat()/2+0.25;
							EntityItem entityitem = new EntityItem(world, pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz, patterns.get(i).getOutput());
							entityitem.setDefaultPickupDelay();
							world.spawnEntity(entityitem);
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
			IRecipeInfo recipe = packageItem.getRecipeInfo(stack);
			int index = packageItem.getIndex(stack);
			if(recipe != null && recipe.isValid() && recipe.validPatternIndex(index)) {
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
			return recipe == null;
		}

		public void setupToSend() {
			if(isEmpty() || !recipe.isValid() || recipe.getRecipeType().hasMachine() || !toSend.isEmpty()) {
				return;
			}
			toSend.addAll(Lists.transform(recipe.getInputs(), ItemStack::copy));
		}

		public void readFromNBT(NBTTagCompound nbt) {
			clearRecipe();
			if(nbt.hasKey("Recipe")) {
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
