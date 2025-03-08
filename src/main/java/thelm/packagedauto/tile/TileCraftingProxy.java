package thelm.packagedauto.tile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.client.gui.GuiCraftingProxy;
import thelm.packagedauto.container.ContainerCraftingProxy;
import thelm.packagedauto.integration.appeng.networking.HostHelperTileCraftingProxy;
import thelm.packagedauto.inventory.InventoryCraftingProxy;
import thelm.packagedauto.item.ItemProxyMarker;
import thelm.packagedauto.network.packet.PacketBeam;
import thelm.packagedauto.network.packet.PacketDirectionalMarker;
import thelm.packagedauto.network.packet.PacketSizedMarker;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
})
public class TileCraftingProxy extends TileBase implements ITickable, IPackageCraftingMachine, ISettingsCloneable, IGridHost, IActionHost {

	public static int range = 8;

	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DirectionalGlobalPos target;
	public boolean firstTick = true;

	public TileCraftingProxy() {
		setInventory(new InventoryCraftingProxy(this));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperTileCraftingProxy(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.crafting_proxy.name");
	}

	@Override
	public String getConfigTypeName() {
		return "tile.packagedauto.crafting_proxy.name";
	}

	@Override
	public void update() {
		if(firstTick) {
			firstTick = false;
			if(!world.isRemote && hostHelper != null) {
				hostHelper.isActive();
			}
		}
	}

	@Override
	public boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing) {
		if(!isBusy()) {
			IPackageCraftingMachine machine = (IPackageCraftingMachine)world.getTileEntity(target.blockPos());
			if(machine.acceptPackage(recipeInfo, stacks, target.direction())) {
				EnumFacing dir = target.direction();
				Vec3d source = new Vec3d(pos).add(0.5, 0.5, 0.5);
				Vec3d delta = new Vec3d(target.blockPos().subtract(pos)).add(new Vec3d(dir.getDirectionVec()).scale(0.5));
				PacketBeam.sendBeams(source, Collections.singletonList(delta), 0xFF7F00, 6, true, world.provider.getDimension(), 32);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		if(target == null) {
			return true;
		}
		BlockPos pos = target.blockPos();
		if(!world.isBlockLoaded(pos)) {
			return true;
		}
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null &&
				!(tile instanceof TileCraftingProxy) &&
				tile instanceof IPackageCraftingMachine) {
			return ((IPackageCraftingMachine)tile).isBusy();
		}
		return true;
	}

	public void sendPreview(EntityPlayerMP player) {
		long currentTime = world.getTotalWorldTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUniqueID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			previewTimes.put(player.getUniqueID(), currentTime);
			if(target != null) {
				EnumFacing dir = target.direction();
				Vec3d source = new Vec3d(pos).add(0.5, 0.5, 0.5);
				Vec3d delta = new Vec3d(target.blockPos().subtract(pos)).add(new Vec3d(dir.getDirectionVec()).scale(0.5));
				PacketDirectionalMarker.sendDirectionalMarkers(player, Collections.singletonList(target), 0xFFFF00, 200);
				PacketBeam.sendBeams(player, source, Collections.singletonList(delta), 0xFFFF00, 200, false);
			}
			Vec3d lowerCorner = new Vec3d(pos).subtract(range, range, range);
			Vec3d size = new Vec3d(range*2+1, range*2+1, range*2+1);
			PacketSizedMarker.sendSizedMarker(player, lowerCorner, size, 0xFF7F00, 200);
		}
	}

	public HostHelperTileCraftingProxy hostHelper;

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

	@Override
	public ISettingsCloneable.Result loadConfig(NBTTagCompound nbt, EntityPlayer player) {
		if(!nbt.hasKey("Target")) {
			return ISettingsCloneable.Result.fail(new TextComponentTranslation("item.packagedauto.settings_cloner.invalid"));
		}
		int availableCount = 0;
		InventoryPlayer playerInventory = player.inventory;
		if(!inventory.getStackInSlot(0).isEmpty()) {
			if(inventory.getStackInSlot(0).getItem() == ItemProxyMarker.INSTANCE) {
				availableCount += inventory.getStackInSlot(0).getCount();
			}
			else {
				return ISettingsCloneable.Result.fail(new TextComponentTranslation("tile.packagedauto.crafting_proxy.non_marker_present"));
			}
		}
		f:if(availableCount < 1) {
			for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
				ItemStack stack = playerInventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ItemProxyMarker.INSTANCE && !stack.hasTagCompound()) {
					availableCount += stack.getCount();
				}
				if(availableCount >= 1) {
					break f;
				}
			}
			return ISettingsCloneable.Result.fail(new TextComponentTranslation("tile.packagedauto.crafting_proxy.no_markers"));
		}
		int removedCount = inventory.getStackInSlot(0).getCount();
		inventory.setInventorySlotContents(0, ItemStack.EMPTY);
		if(removedCount < 1) {
			for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
				ItemStack stack = playerInventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ItemProxyMarker.INSTANCE && !stack.hasTagCompound()) {
					removedCount += stack.splitStack(1).getCount();
				}
				if(removedCount >= 1) {
					break;
				}
			}
		}
		if(removedCount > 1) {
			ItemStack stack = new ItemStack(ItemProxyMarker.INSTANCE, removedCount-1);
			if(!playerInventory.addItemStackToInventory(stack)) {
				EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, stack);
				item.setThrower(player.getName());
				world.spawnEntity(item);
			}
		}
		NBTTagCompound targetTag = nbt.getCompoundTag("Target");
		int dimension = targetTag.getInteger("Dimension");
		int[] posArray = targetTag.getIntArray("Position");
		BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		EnumFacing direction = EnumFacing.byIndex(targetTag.getByte("Direction"));
		DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dimension, blockPos, direction);
		ItemStack stack = new ItemStack(ItemProxyMarker.INSTANCE);
		ItemProxyMarker.INSTANCE.setDirectionalGlobalPos(stack, globalPos);
		inventory.setInventorySlotContents(0, stack);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(NBTTagCompound nbt, EntityPlayer player) {
		if(target == null) {
			return ISettingsCloneable.Result.fail(new TextComponentTranslation("tile.packagedauto.crafting_proxy.empty"));
		}
		NBTTagCompound targetTag = new NBTTagCompound();
		targetTag.setInteger("Dimension", target.dimension());
		targetTag.setIntArray("Position", new int[] {target.x(), target.y(), target.z()});
		targetTag.setByte("Direction", (byte)target.direction().getIndex());
		nbt.setTag("Target", targetTag);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
		}
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiCraftingProxy(new ContainerCraftingProxy(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerCraftingProxy(player.inventory, this);
	}
}
