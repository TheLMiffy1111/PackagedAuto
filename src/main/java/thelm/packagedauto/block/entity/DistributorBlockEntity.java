package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.integration.appeng.blockentity.AEDistributorBlockEntity;
import thelm.packagedauto.inventory.DistributorItemHandler;
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.menu.DistributorMenu;
import thelm.packagedauto.network.packet.BeamPacket;
import thelm.packagedauto.network.packet.DirectionalMarkerPacket;
import thelm.packagedauto.network.packet.SizedMarkerPacket;
import thelm.packagedauto.recipe.IPositionedProcessingPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class DistributorBlockEntity extends BaseBlockEntity implements IPackageCraftingMachine, ISettingsCloneable {

	public static final BlockEntityType<DistributorBlockEntity> TYPE_INSTANCE = (BlockEntityType<DistributorBlockEntity>)BlockEntityType.Builder.
			of(MiscHelper.INSTANCE.<BlockEntityType.BlockEntitySupplier<DistributorBlockEntity>>conditionalSupplier(
					()->ModList.get().isLoaded("ae2"),
					()->()->AEDistributorBlockEntity::new, ()->()->DistributorBlockEntity::new).get(),
					DistributorBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:distributor");

	public static int range = 16;
	public static int refreshInterval = 4;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);
	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DistributorBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new DistributorItemHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("block.packagedauto.distributor");
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
		if(!isBusy() && recipeInfo instanceof IPositionedProcessingPackageRecipeInfo recipe) {
			boolean blocking = false;
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof UnpackagerBlockEntity unpackager) {
				blocking = unpackager.blocking;
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
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity == null) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				Direction dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
				if(stack.getItem() instanceof IVolumePackageItem vPackage &&
						vPackage.getVolumeType(stack) != null &&
						vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, dir)) {
					if(blocking && !vPackage.getVolumeType(stack).isEmpty(blockEntity, dir)) {
						return false;
					}
					if(!MiscHelper.INSTANCE.fillVolume(blockEntity, dir, stack, true).isEmpty()) {
						return false;
					}
				}
				else if(itemHandler != null) {
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
		List<Vec3> deltas = new ArrayList<>();
		for(int i : pending.keySet().toIntArray()) {
			if(!positions.containsKey(i)) {
				ejectItems();
				break;
			}
			BlockPos pos = positions.get(i).blockPos();
			if(!level.isLoaded(pos)) {
				continue;
			}
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity == null) {
				ejectItems();
				break;
			}
			ItemStack stack = pending.get(i);
			Direction dir = positions.get(i).direction();
			IItemHandler itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
			ItemStack stackRem = stack;
			if(stack.getItem() instanceof IVolumePackageItem vPackage &&
					vPackage.getVolumeType(stack) != null &&
					vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, dir)) {
				stackRem = MiscHelper.INSTANCE.fillVolume(blockEntity, dir, stack, false);
			}
			else if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				break;
			}
			if(stackRem.getCount() < stack.getCount()) {
				Vec3 delta = Vec3.atLowerCornerOf(pos.subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
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
			Vec3 source = Vec3.atCenterOf(worldPosition);
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

	public void sendPreview(ServerPlayer player) {
		long currentTime = level.getGameTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUUID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			previewTimes.put(player.getUUID(), currentTime);
			if(!positions.isEmpty()) {
				List<Vec3> deltas = positions.values().stream().map(globalPos->{
					BlockPos pos = globalPos.blockPos();
					Direction dir = globalPos.direction();
					return Vec3.atLowerCornerOf(pos.subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
				}).collect(Collectors.toList());
				Vec3 source = Vec3.atCenterOf(worldPosition);
				DirectionalMarkerPacket.sendDirectionalMarkers(player, new ArrayList<>(positions.values()), 0x00FF7F, 200);
				BeamPacket.sendBeams(player, source, deltas, 0x00FF7F, 200, false);
			}
			Vec3 lowerCorner = Vec3.atLowerCornerOf(worldPosition).subtract(range, range, range);
			Vec3 size = new Vec3(range*2+1, range*2+1, range*2+1);
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
	public boolean loadConfig(CompoundTag nbt, Player player) {
		ListTag positionsTag = nbt.getList("Positions", 10);
		if(positionsTag.isEmpty()) {
			return false;
		}
		int requiredCount = positionsTag.size();
		int availableCount = 0;
		Inventory playerInventory = player.getInventory();
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.is(DistributorMarkerItem.INSTANCE)) {
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
				if(!stack.isEmpty() && stack.is(DistributorMarkerItem.INSTANCE) && !stack.hasTag()) {
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
				if(!stack.isEmpty() && stack.is(DistributorMarkerItem.INSTANCE) && !stack.hasTag()) {
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
			CompoundTag positionTag = positionsTag.getCompound(i);
			int index = positionTag.getByte("Index");
			ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(positionTag.getString("Dimension")));
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
	public boolean saveConfig(CompoundTag nbt, Player player) {
		if(positions.isEmpty()) {
			return false;
		}
		ListTag positionsTag = new ListTag();
		for(Int2ObjectMap.Entry<DirectionalGlobalPos> entry : positions.int2ObjectEntrySet()) {
			DirectionalGlobalPos pos = entry.getValue();
			CompoundTag positionTag = new CompoundTag();
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
	public void load(CompoundTag nbt) {
		super.load(nbt);
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
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		ListTag pendingTag = MiscHelper.INSTANCE.saveAllItems(new ListTag(), pendingList);
		nbt.put("Pending", pendingTag);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new DistributorMenu(windowId, inventory, this);
	}
}
