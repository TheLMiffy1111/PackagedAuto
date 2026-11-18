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
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.inventory.DistributorItemResourceHandler;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.menu.DistributorMenu;
import thelm.packagedauto.packet.BeamPacket;
import thelm.packagedauto.packet.DirectionalMarkerPacket;
import thelm.packagedauto.packet.SizedMarkerPacket;
import thelm.packagedauto.recipe.IPositionedProcessingPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class DistributorBlockEntity extends BaseBlockEntity implements IPackageCraftingMachine, ISettingsCloneable {

	public static int range = 16;
	public static int refreshInterval = 4;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);
	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DistributorBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.DISTRIBUTOR.get(), pos, state);
		setItemHandler(new DistributorItemResourceHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.distributor");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.distributor";
	}

	@Override
	public void tick() {
		if(!level.isClientSide()) {
			if(level.getGameTime() % refreshInterval == 0 && !pending.isEmpty()) {
				distributeItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction) {
		return acceptPackage(recipeInfo, stacks, direction, false);
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction, boolean blocking) {
		if(!isBusy() && recipeInfo instanceof IPositionedProcessingPackageRecipeInfo recipe) {
			Int2ObjectMap<ItemStack> matrix = recipe.getMatrix();
			if(!positions.keySet().containsAll(matrix.keySet())) {
				return false;
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				BlockPos pos = positions.get(entry.getIntKey()).blockPos();
				if(!level.isLoaded(pos)) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				Direction dir = positions.get(entry.getIntKey()).direction();
				ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, pos, dir);
				if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
						stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, dir)) {
					if(blocking && !stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().isEmpty(level, pos, dir)) {
						return false;
					}
					try(Transaction transaction = Transaction.openRoot()) {
						if(MiscHelper.INSTANCE.insertVolume(level, pos, dir, stack, transaction) == 0) {
							return false;
						}
					}
				}
				else if(itemHandler != null) {
					if(blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
						return false;
					}
					try(Transaction transaction = Transaction.openRoot()) {
						if(ResourceHandlerUtil.insertStacking(itemHandler, ItemResource.of(stack), stack.count(), transaction) == 0) {
							return false;
						}
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
			ItemStack stack = pending.get(i);
			Direction dir = positions.get(i).direction();
			ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, pos, dir);
			int inserted = 0;
			if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
					stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, dir)) {
				try(Transaction transaction = Transaction.openRoot()) {
					inserted = MiscHelper.INSTANCE.insertVolume(level, pos, dir, stack, transaction);
					transaction.commit();
				}
			}
			else if(itemHandler != null) {
				try(Transaction transaction = Transaction.openRoot()) {
					inserted = ResourceHandlerUtil.insertStacking(itemHandler, ItemResource.of(stack), stack.count(), transaction);
					transaction.commit();
				}
			}
			else {
				ejectItems();
				break;
			}
			if(inserted > 0) {
				Vec3 delta = Vec3.atLowerCornerOf(pos.subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getUnitVec3i()).scale(0.5));
				deltas.add(delta);
			}
			if(inserted == stack.count()) {
				pending.remove(i);
			}
			else {
				pending.put(i, stack.copyWithCount(stack.count()-inserted));
			}
			setChanged();
		}
		if(!deltas.isEmpty()) {
			Vec3 source = Vec3.atCenterOf(worldPosition);
			BeamPacket.sendBeams((ServerLevel)level, source, deltas, 0x00FFFF, 6, true, 32);
			setChanged();
		}
	}

	protected void ejectItems() {
		for(int i = 0; i < 81; ++i) {
			if(pending.containsKey(i)) {
				ItemStack stack = pending.remove(i);
				if(!stack.isEmpty()) {
					double dx = level.getRandom().nextFloat()/2+0.25;
					double dy = level.getRandom().nextFloat()/2+0.75;
					double dz = level.getRandom().nextFloat()/2+0.25;
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
					return Vec3.atLowerCornerOf(pos.subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getUnitVec3i()).scale(0.5));
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
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		for(Int2ObjectMap.Entry<ItemStack> entry : pending.int2ObjectEntrySet()) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), entry.getValue());
		}
	}

	@Override
	public ISettingsCloneable.Result loadConfig(ValueInput input, Player player) {
		ValueInputList positionsInput = input.childrenListOrEmpty("positions");
		if(positionsInput.isEmpty()) {
			return ISettingsCloneable.Result.fail(Component.translatable("item.packagedauto.settings_cloner.invalid"));
		}
		Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81); 
		for(ValueInput positionInput : positionsInput) {
			int index = positionInput.getByteOr("index", (byte)-1);
			DirectionalGlobalPos globalPos = positionInput.read(DirectionalGlobalPos.MAP_CODEC).orElse(null);
			if(index > 0 && globalPos != null) {
				positions.put(index, globalPos);
			}
		}
		int requiredCount = positions.size();
		int availableCount = 0;
		Inventory playerInventory = player.getInventory();
		for(int i = 0; i < itemHandler.size(); ++i) {
			ItemStack stack = itemHandler.getStack(i);
			if(!stack.isEmpty()) {
				if(stack.is(PackagedAutoItems.DISTRIBUTOR_MARKER)) {
					availableCount += stack.getCount();
				}
				else {
					return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.distributor.non_marker_present"));
				}
			}
		}
		f:if(availableCount < requiredCount) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.is(PackagedAutoItems.DISTRIBUTOR_MARKER) && stack.isComponentsPatchEmpty()) {
					availableCount += stack.getCount();
				}
				if(availableCount >= requiredCount) {
					break f;
				}
			}
			return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.distributor.no_markers"));
		}
		int removedCount = 0;
		for(int i = 0; i < itemHandler.size(); ++i) {
			removedCount += itemHandler.getStack(i).getCount();
			itemHandler.setStack(i, ItemStack.EMPTY);
		}
		if(removedCount < requiredCount) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.is(PackagedAutoItems.DISTRIBUTOR_MARKER) && stack.isComponentsPatchEmpty()) {
					removedCount += stack.split(requiredCount - removedCount).getCount();
				}
				if(removedCount >= requiredCount) {
					break;
				}
			}
		}
		if(removedCount > requiredCount) {
			ItemStack stack = PackagedAutoItems.DISTRIBUTOR_MARKER.toStack(removedCount-requiredCount);
			if(!playerInventory.add(stack)) {
				ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
				item.setThrower(player);
				level.addFreshEntity(item);
			}
		}
		for(Int2ObjectMap.Entry<DirectionalGlobalPos> entry : positions.int2ObjectEntrySet()) {
			int index = entry.getIntKey();
			DirectionalGlobalPos globalPos = entry.getValue();
			ItemStack stack = PackagedAutoItems.DISTRIBUTOR_MARKER.toStack();
			DataComponentPatch patch = DataComponentPatch.builder().
					set(PackagedAutoDataComponents.MARKER_POS.get(), globalPos).
					build();
			stack.applyComponents(patch);
			itemHandler.setStack(index, stack);
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(ValueOutput output, Player player) {
		if(positions.isEmpty()) {
			return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.distributor.empty"));
		}
		ValueOutputList positionsOutput = output.childrenList("positions");
		for(Int2ObjectMap.Entry<DirectionalGlobalPos> entry : positions.int2ObjectEntrySet()) {
			ValueOutput positionOutput = positionsOutput.addChild();
			DirectionalGlobalPos pos = entry.getValue();
			positionOutput.store(DirectionalGlobalPos.MAP_CODEC, pos);
			positionOutput.putByte("index", (byte)entry.getIntKey());
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscHelper.INSTANCE.deserializeItemList(pendingList, input.childrenListOrEmpty("pending"));
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		MiscHelper.INSTANCE.serializeItemList(pendingList, output.childrenList("pending"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new DistributorMenu(windowId, inventory, this);
	}
}
