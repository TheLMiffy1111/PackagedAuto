package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.container.DistributorContainer;
import thelm.packagedauto.integration.appeng.tile.AEDistributorTile;
import thelm.packagedauto.inventory.DistributorItemHandler;
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.network.packet.BeamPacket;
import thelm.packagedauto.network.packet.DirectionalMarkerPacket;
import thelm.packagedauto.network.packet.SizedMarkerPacket;
import thelm.packagedauto.recipe.IPositionedProcessingPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class DistributorTile extends BaseTile implements ITickableTileEntity, IPackageCraftingMachine, ISettingsCloneable {

	public static final TileEntityType<DistributorTile> TYPE_INSTANCE = (TileEntityType<DistributorTile>)TileEntityType.Builder.
			of(MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("appliedenergistics2"),
					()->AEDistributorTile::new, ()->DistributorTile::new), DistributorBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:distributor");

	public static int range = 16;
	public static int refreshInterval = 4;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);
	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DistributorTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new DistributorItemHandler(this));
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.distributor");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.distributor";
	}

	@Override
	public void tick() {
		if(!level.isClientSide) {
			if(level.getGameTime() % refreshInterval == 0 && !pending.isEmpty()) {
				distributeItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction) {
		if(!isBusy() && recipeInfo instanceof IPositionedProcessingPackageRecipeInfo) {
			IPositionedProcessingPackageRecipeInfo recipe = (IPositionedProcessingPackageRecipeInfo)recipeInfo;
			boolean blocking = false;
			TileEntity unpackager = level.getBlockEntity(worldPosition.relative(direction));
			if(unpackager instanceof UnpackagerTile) {
				blocking = ((UnpackagerTile)unpackager).blocking;
			}
			Int2ObjectMap<ItemStack> matrix = recipe.getMatrix();
			if(!positions.keySet().containsAll(matrix.keySet())) {
				return false;
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				BlockPos pos = positions.get(entry.getIntKey()).blockPos();
				if(!level.isLoaded(pos)) {
					return false;
				}
				TileEntity tile = level.getBlockEntity(pos);
				if(tile == null) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				Direction dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
				if(itemHandler != null) {
					if(blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
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
		List<Vector3d> deltas = new ArrayList<>();
		for(int i : pending.keySet().toIntArray()) {
			if(!positions.containsKey(i)) {
				ejectItems();
				break;
			}
			BlockPos pos = positions.get(i).blockPos();
			if(!level.isLoaded(pos)) {
				continue;
			}
			TileEntity tile = level.getBlockEntity(pos);
			if(tile == null) {
				ejectItems();
				break;
			}
			ItemStack stack = pending.get(i);
			Direction dir = positions.get(i).direction();
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
			ItemStack stackRem = stack;
			if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				break;
			}
			if(stackRem.getCount() < stack.getCount()) {
				Vector3d delta = Vector3d.atLowerCornerOf(pos.subtract(worldPosition)).add(Vector3d.atLowerCornerOf(dir.getNormal()).scale(0.5));
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
			Vector3d source = Vector3d.atCenterOf(worldPosition);
			BeamPacket.sendBeams(source, deltas, 0x00FFFF, 6, true, level.dimension(), 32);
			setChanged();
		}
	}

	protected void ejectItems() {
		for(int i = 0; i < 81; ++i) {
			if(pending.containsKey(i)) {
				ItemStack stack = pending.remove(i);
				if(!stack.isEmpty()) {
					double dx = level.random.nextFloat()/2+0.25;
					double dy = level.random.nextFloat()/2+0.75;
					double dz = level.random.nextFloat()/2+0.25;
					ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX()+dx, worldPosition.getY()+dy, worldPosition.getZ()+dz, stack);
					itemEntity.setDefaultPickUpDelay();
					level.addFreshEntity(itemEntity);
				}
			}
		}
		setChanged();
	}

	public void sendPreview(ServerPlayerEntity player) {
		long currentTime = level.getGameTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUUID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			previewTimes.put(player.getUUID(), currentTime);
			if(!positions.isEmpty()) {
				List<Vector3d> deltas = positions.values().stream().map(globalPos->{
					BlockPos pos = globalPos.blockPos();
					Direction dir = globalPos.direction();
					return Vector3d.atLowerCornerOf(pos.subtract(worldPosition)).add(dir.getStepX()*0.5, dir.getStepY()*0.5, dir.getStepZ()*0.5);
				}).collect(Collectors.toList());
				Vector3d source = Vector3d.atCenterOf(worldPosition);
				DirectionalMarkerPacket.sendDirectionalMarkers(player, new ArrayList<>(positions.values()), 0x00FF7F, 200);
				BeamPacket.sendBeams(player, source, deltas, 0x00FF7F, 200, false);
			}
			Vector3d lowerCorner = Vector3d.atLowerCornerOf(worldPosition).subtract(range, range, range);
			Vector3d size = new Vector3d(range*2+1, range*2+1, range*2+1);
			SizedMarkerPacket.sendSizedMarker(player, lowerCorner, size, 0x00FFFF, 200);
		}
	}

	@Override
	public int getComparatorSignal() {
		if(!pending.isEmpty()) {
			return 15;
		}
		return 0;
	}

	@Override
	public boolean loadConfig(CompoundNBT nbt, PlayerEntity player) {
		ListNBT positionsTag = nbt.getList("Positions", 10);
		if(positionsTag.isEmpty()) {
			return false;
		}
		int requiredCount = positionsTag.size();
		int availableCount = 0;
		PlayerInventory playerInventory = player.inventory;
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == DistributorMarkerItem.INSTANCE) {
					availableCount += stack.getCount();
				}
				else {
					return false;
				}
			}
		}
		if(availableCount < requiredCount) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.getItem() == DistributorMarkerItem.INSTANCE && !stack.hasTag()) {
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
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			removedCount += itemHandler.getStackInSlot(i).getCount();
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
		if(removedCount < requiredCount) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.getItem() == DistributorMarkerItem.INSTANCE && !stack.hasTag()) {
					removedCount += stack.split(requiredCount - removedCount).getCount();
				}
				if(removedCount >= requiredCount) {
					break;
				}
			}
		}
		if(removedCount > requiredCount) {
			ItemStack stack = new ItemStack(DistributorMarkerItem.INSTANCE, removedCount-requiredCount);
			if(!playerInventory.add(stack)) {
				ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
				item.setThrower(player.getUUID());
				level.addFreshEntity(item);
			}
		}
		for(int i = 0; i < requiredCount; ++i) {
			CompoundNBT positionTag = positionsTag.getCompound(i);
			int index = positionTag.getByte("Index");
			RegistryKey<World> dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(positionTag.getString("Dimension")));
			int[] posArray = positionTag.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			Direction direction = Direction.from3DDataValue(positionTag.getByte("Direction"));
			DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dimension, blockPos, direction);
			ItemStack stack = new ItemStack(DistributorMarkerItem.INSTANCE);
			DistributorMarkerItem.INSTANCE.setDirectionalGlobalPos(stack, globalPos);
			itemHandler.setStackInSlot(index, stack);
		}
		return true;
	}

	@Override
	public boolean saveConfig(CompoundNBT nbt, PlayerEntity player) {
		if(positions.isEmpty()) {
			return false;
		}
		ListNBT positionsTag = new ListNBT();
		for(Int2ObjectMap.Entry<DirectionalGlobalPos> entry : positions.int2ObjectEntrySet()) {
			DirectionalGlobalPos pos = entry.getValue();
			CompoundNBT positionTag = new CompoundNBT();
			positionTag.putByte("Index", (byte)entry.getIntKey());
			positionTag.putString("Dimension", pos.dimension().location().toString());
			positionTag.putIntArray("Position", new int[] {pos.x(), pos.y(), pos.z()});
			positionTag.putByte("Direction", (byte)pos.direction().get3DDataValue());
			positionsTag.add(positionTag);
		}
		nbt.put("Positions", positionsTag);
		return true;
	}

	@Override
	public void load(BlockState blockState, CompoundNBT nbt) {
		super.load(blockState, nbt);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Pending", 10), pendingList);
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		super.save(nbt);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		ListNBT pendingTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), pendingList);
		nbt.put("Pending", pendingTag);
		return nbt;
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new DistributorContainer(windowId, playerInventory, this);
	}
}
