package thelm.packagedauto.tile;

import java.util.List;
import java.util.Objects;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.client.gui.GuiCrafter;
import thelm.packagedauto.container.ContainerCrafter;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.networking.HostHelperCrafter;
import thelm.packagedauto.inventory.InventoryCrafter;
import thelm.packagedauto.recipe.IPackageRecipeInfoCrafting;
import thelm.packagedauto.util.MiscHelper;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
})
public class TileCrafter extends TileBase implements IPackageCraftingMachine, IGridHost, IActionHost {

	public static boolean enabled = false;

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static boolean drawMEEnergy = true;

	public boolean isWorking = false;
	public int remainingProgress = 0;
	public IPackageRecipeInfoCrafting currentRecipe;

	public TileCrafter() {
		setInventory(new InventoryCrafter(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperCrafter(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return StatCollector.translateToLocal("tile.packagedauto.crafter.name");
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0) {
					finishProcess();
					if(hostHelper != null && hostHelper.isActive()) {
						hostHelper.ejectItem();
					}
					else {
						ejectItems();
					}
				}
			}
			chargeEnergy();
			if(worldObj.getTotalWorldTime() % 8 == 0) {
				if(hostHelper != null && hostHelper.isActive()) {
					hostHelper.ejectItem();
					if(drawMEEnergy) {
						hostHelper.chargeEnergy();
					}
				}
				else {
					ejectItems();
				}
			}
			energyStorage.updateIfChanged();
		}
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, ForgeDirection side) {
		if(!isBusy() && recipeInfo instanceof IPackageRecipeInfoCrafting) {
			IPackageRecipeInfoCrafting recipe = (IPackageRecipeInfoCrafting)recipeInfo;
			ItemStack slotStack = inventory.getStackInSlot(9);
			ItemStack outputStack = recipe.getOutput();
			if(slotStack == null || slotStack.getItem() == outputStack.getItem() && slotStack.getItemDamage() == outputStack.getItemDamage() && ItemStack.areItemStackTagsEqual(slotStack, outputStack) && slotStack.stackSize+outputStack.stackSize <= outputStack.getMaxStackSize()) {
				currentRecipe = recipe;
				isWorking = true;
				remainingProgress = energyReq;
				for(int i = 0; i < 9; ++i) {
					ItemStack stack = recipe.getMatrix().getStackInSlot(i);
					inventory.setInventorySlotContents(i, stack == null ? null : stack.copy());
				}
				markDirty();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return isWorking || !inventory.stacks.subList(0, 9).stream().allMatch(Objects::isNull);
	}

	protected void tickProcess() {
		int energy = energyStorage.extractEnergy(Math.min(energyUsage, remainingProgress), false);
		remainingProgress -= energy;
	}

	protected void finishProcess() {
		if(currentRecipe == null) {
			endProcess();
			return;
		}
		if(inventory.getStackInSlot(9) == null) {
			inventory.setInventorySlotContents(9, currentRecipe.getOutput());
		}
		else {
			inventory.getStackInSlot(9).stackSize += currentRecipe.getOutput().stackSize;
		}
		for(int i = 0; i < 9; ++i) {
			inventory.setInventorySlotContents(i, MiscHelper.INSTANCE.getContainerItem(inventory.getStackInSlot(i)));
		}
		endProcess();
	}

	public void endProcess() {
		remainingProgress = 0;
		isWorking = false;
		currentRecipe = null;
		markDirty();
	}

	protected void ejectItems() {
		int endIndex = isWorking ? 9 : 0;
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			if(tile != null && !(tile instanceof TileUnpackager) && tile instanceof IInventory) {
				IInventory inv = (IInventory)tile;
				boolean flag = true;
				for(int i = 9; i >= endIndex; --i) {
					ItemStack stack = inventory.getStackInSlot(i);
					if(stack == null) {
						continue;
					}
					for(int slot : MiscHelper.INSTANCE.getSlots(inv, side)) {
						ItemStack stackRem = MiscHelper.INSTANCE.insertItem(inv, slot, side.getOpposite(), stack, false);
						if(stackRem == null || stackRem.stackSize < stack.stackSize) {
							stack = stackRem;
							flag = false;
						}
						if(stack == null) {
							break;
						}
					}
					inventory.setInventorySlotContents(i, stack);
					if(flag) {
						break;
					}
				}
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

	public HostHelperCrafter hostHelper;

	@Override
	public void invalidate() {
		super.invalidate();
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

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		currentRecipe = null;
		if(nbt.hasKey("Recipe")) {
			NBTTagCompound tag = nbt.getCompoundTag("Recipe");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipeFromNBT(tag);
			if(recipe instanceof IPackageRecipeInfoCrafting) {
				currentRecipe = (IPackageRecipeInfoCrafting)recipe;
			}
		}
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(currentRecipe != null) {
			NBTTagCompound tag = MiscHelper.INSTANCE.writeRecipeToNBT(new NBTTagCompound(), currentRecipe);
			nbt.setTag("Recipe", tag);
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
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
		super.writeSyncNBT(nbt);
		nbt.setBoolean("Working", isWorking);
		nbt.setInteger("Progress", remainingProgress);
		return nbt;
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
		return new GuiCrafter(new ContainerCrafter(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerCrafter(player.inventory, this);
	}
}
