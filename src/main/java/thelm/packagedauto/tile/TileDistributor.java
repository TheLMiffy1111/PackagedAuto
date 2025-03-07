package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.gui.GuiDistributor;
import thelm.packagedauto.container.ContainerDistributor;
import thelm.packagedauto.integration.appeng.networking.HostHelperTileDistributor;
import thelm.packagedauto.inventory.InventoryDistributor;
import thelm.packagedauto.item.ItemDistributorMarker;
import thelm.packagedauto.network.packet.PacketBeam;
import thelm.packagedauto.network.packet.PacketDirectionalMarker;
import thelm.packagedauto.network.packet.PacketSizedMarker;
import thelm.packagedauto.recipe.IRecipeInfoProcessingPositioned;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
})
public class TileDistributor extends TileBase implements ITickable, IPackageCraftingMachine, ISettingsCloneable, IGridHost, IActionHost {

	public static int range = 16;
	public static int refreshInterval = 4;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);
	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public boolean firstTick = true;

	public TileDistributor() {
		setInventory(new InventoryDistributor(this));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperTileDistributor(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.distributor.name");
	}

	@Override
	public String getConfigTypeName() {
		return "tile.packagedauto.distributor.name";
	}

	@Override
	public void update() {
		if(firstTick) {
			firstTick = false;
			if(!world.isRemote && hostHelper != null) {
				hostHelper.isActive();
			}
		}
		if(!world.isRemote) {
			if(world.getTotalWorldTime() % refreshInterval == 0 && !pending.isEmpty()) {
				distributeItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing) {
		if(!isBusy() && recipeInfo instanceof IRecipeInfoProcessingPositioned) {
			IRecipeInfoProcessingPositioned recipe = (IRecipeInfoProcessingPositioned)recipeInfo;
			boolean blocking = false;
			TileEntity unpackager = world.getTileEntity(pos.offset(facing));
			if(unpackager instanceof TileUnpackager) {
				blocking = ((TileUnpackager)unpackager).blocking;
			}
			Int2ObjectMap<ItemStack> matrix = recipe.getMatrix();
			if(!positions.keySet().containsAll(matrix.keySet())) {
				return false;
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				BlockPos pos = positions.get(entry.getIntKey()).blockPos();
				if(!world.isBlockLoaded(pos)) {
					return false;
				}
				TileEntity tile = world.getTileEntity(pos);
				if(tile == null) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				EnumFacing dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
				if(itemHandler != null) {
					if(blocking && !MiscUtil.isEmpty(itemHandler)) {
						return false;
					}
					if(!ItemHandlerHelper.insertItem(itemHandler, stack, true).isEmpty()) {
						return false;
					}
				}
				else {
					return false;
				}
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				pending.put(entry.getIntKey(), entry.getValue().copy());
			}
			distributeItems();
			return true;
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return !pending.isEmpty();
	}

	protected void distributeItems() {
		List<Vec3d> deltas = new ArrayList<>();
		for(int i : pending.keySet().toIntArray()) {
			if(!positions.containsKey(i)) {
				ejectItems();
				break;
			}
			BlockPos pos = positions.get(i).blockPos();
			if(!world.isBlockLoaded(pos)) {
				continue;
			}
			TileEntity tile = world.getTileEntity(pos);
			if(tile == null) {
				ejectItems();
				break;
			}
			ItemStack stack = pending.get(i);
			EnumFacing dir = positions.get(i).direction();
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
			ItemStack stackRem = stack;
			if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				break;
			}
			if(stackRem.getCount() < stack.getCount()) {
				Vec3d delta = new Vec3d(pos.subtract(this.pos)).add(new Vec3d(dir.getDirectionVec()).scale(0.5));
				deltas.add(delta);
			}
			if(stackRem.isEmpty()) {
				pending.remove(i);
			}
			else {
				pending.put(i, stackRem);
			}
		}
		if(!deltas.isEmpty()) {
			Vec3d source = new Vec3d(pos).add(0.5, 0.5, 0.5);
			PacketBeam.sendBeams(source, deltas, 0x00FFFF, 6, true, world.provider.getDimension(), 32);
			markDirty();
		}
	}

	protected void ejectItems() {
		for(int i = 0; i < 81; ++i) {
			if(pending.containsKey(i)) {
				ItemStack stack = pending.remove(i);
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
		markDirty();
	}

	public void sendPreview(EntityPlayerMP player) {
		long currentTime = world.getTotalWorldTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUniqueID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			previewTimes.put(player.getUniqueID(), currentTime);
			if(!positions.isEmpty()) {
				List<Vec3d> deltas = positions.values().stream().map(globalPos->{
					BlockPos pos = globalPos.blockPos();
					EnumFacing dir = globalPos.direction();
					return new Vec3d(pos.subtract(this.pos)).add(dir.getXOffset()*0.5, dir.getYOffset()*0.5, dir.getZOffset()*0.5);
				}).collect(Collectors.toList());
				Vec3d source = new Vec3d(pos).add(0.5, 0.5, 0.5);
				PacketDirectionalMarker.sendDirectionalMarkers(player, new ArrayList<>(positions.values()), 0x00FF7F, 200);
				PacketBeam.sendBeams(player, source, deltas, 0x00FF7F, 200, false);
			}
			Vec3d lowerCorner = new Vec3d(pos).subtract(range, range, range);
			Vec3d size = new Vec3d(range*2+1, range*2+1, range*2+1);
			PacketSizedMarker.sendSizedMarker(player, lowerCorner, size, 0x00FFFF, 200);
		}
	}

	@Override
	public int getComparatorSignal() {
		if(!pending.isEmpty()) {
			return 15;
		}
		return 0;
	}

	public HostHelperTileDistributor hostHelper;

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
	public boolean loadConfig(NBTTagCompound nbt, EntityPlayer player) {
		NBTTagList positionsTag = nbt.getTagList("Positions", 10);
		if(positionsTag.isEmpty()) {
			return false;
		}
		int requiredCount = positionsTag.tagCount();
		int availableCount = 0;
		InventoryPlayer playerInventory = player.inventory;
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
					availableCount += stack.getCount();
				}
				else {
					return false;
				}
			}
		}
		if(availableCount < requiredCount) {
			for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
				ItemStack stack = playerInventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ItemDistributorMarker.INSTANCE && !stack.hasTagCompound()) {
					availableCount += stack.getCount();
				}
				if(availableCount >= requiredCount) {
					break;
				}
			}
		}
		if(availableCount < requiredCount) {
			return false;
		}
		int removedCount = 0;
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			removedCount += inventory.getStackInSlot(i).getCount();
			inventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}
		if(removedCount < requiredCount) {
			for(int i = 0; i < playerInventory.getSizeInventory(); ++i) {
				ItemStack stack = playerInventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ItemDistributorMarker.INSTANCE && !stack.hasTagCompound()) {
					removedCount += stack.splitStack(requiredCount - removedCount).getCount();
				}
				if(removedCount >= requiredCount) {
					break;
				}
			}
		}
		if(removedCount > requiredCount) {
			ItemStack stack = new ItemStack(ItemDistributorMarker.INSTANCE, removedCount-requiredCount);
			if(!playerInventory.addItemStackToInventory(stack)) {
				EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, stack);
				item.setThrower(player.getName());
				world.spawnEntity(item);
			}
		}
		for(int i = 0; i < requiredCount; ++i) {
			NBTTagCompound positionTag = positionsTag.getCompoundTagAt(i);
			int index = positionTag.getByte("Index");
			int dimension = positionTag.getInteger("Dimension");
			int[] posArray = positionTag.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			EnumFacing direction = EnumFacing.byIndex(positionTag.getByte("Direction"));
			DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dimension, blockPos, direction);
			ItemStack stack = new ItemStack(ItemDistributorMarker.INSTANCE);
			ItemDistributorMarker.INSTANCE.setDirectionalGlobalPos(stack, globalPos);
			inventory.setInventorySlotContents(index, stack);
		}
		return true;
	}

	@Override
	public boolean saveConfig(NBTTagCompound nbt, EntityPlayer player) {
		if(positions.isEmpty()) {
			return false;
		}
		NBTTagList positionsTag = new NBTTagList();
		for(Int2ObjectMap.Entry<DirectionalGlobalPos> entry : positions.int2ObjectEntrySet()) {
			DirectionalGlobalPos pos = entry.getValue();
			NBTTagCompound positionTag = new NBTTagCompound();
			positionTag.setByte("Index", (byte)entry.getIntKey());
			positionTag.setInteger("Dimension", pos.dimension());
			positionTag.setIntArray("Position", new int[] {pos.x(), pos.y(), pos.z()});
			positionTag.setByte("Direction", (byte)pos.direction().getIndex());
			positionsTag.appendTag(positionTag);
		}
		nbt.setTag("Positions", positionsTag);
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(hostHelper != null) {
			hostHelper.readFromNBT(nbt);
		}
		super.readFromNBT(nbt);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscUtil.loadAllItems(nbt.getTagList("Pending", 10), pendingList);
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		NBTTagList pendingTag = MiscUtil.saveAllItems(new NBTTagList(), pendingList);
		nbt.setTag("Pending", pendingTag);
		if(hostHelper != null) {
			hostHelper.writeToNBT(nbt);
		}
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiDistributor(new ContainerDistributor(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerDistributor(player.inventory, this);
	}
}
