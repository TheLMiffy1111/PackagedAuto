package thelm.packagedauto.block.entity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.inventory.CraftingProxyItemHandler;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.menu.CraftingProxyMenu;
import thelm.packagedauto.packet.BeamPacket;
import thelm.packagedauto.packet.DirectionalMarkerPacket;
import thelm.packagedauto.packet.SizedMarkerPacket;

public class CraftingProxyBlockEntity extends BaseBlockEntity implements IPackageCraftingMachine, ISettingsCloneable {

	public static int range = 8;

	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DirectionalGlobalPos target;

	public CraftingProxyBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.CRAFTING_PROXY.get(), pos, state);
		setItemHandler(new CraftingProxyItemHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.crafting_proxy");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.crafting_proxy";
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction) {
		return acceptPackage(recipeInfo, stacks, direction, false);
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction, boolean blocking) {
		if(!isBusy()) {
			IPackageCraftingMachine machine = (IPackageCraftingMachine)level.getBlockEntity(target.blockPos());
			if(machine.acceptPackage(recipeInfo, stacks, target.direction(), blocking)) {
				Direction dir = target.direction();
				Vec3 source = Vec3.atCenterOf(worldPosition);
				Vec3 delta = Vec3.atLowerCornerOf(target.blockPos().subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
				BeamPacket.sendBeams((ServerLevel)level, source, List.of(delta), 0xFF7F00, 6, true, 32);
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
		if(!level.isLoaded(pos)) {
			return true;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity != null &&
				!(blockEntity instanceof CraftingProxyBlockEntity) &&
				blockEntity instanceof IPackageCraftingMachine machine) {
			return machine.isBusy();
		}
		return true;
	}

	public void sendPreview(ServerPlayer player) {
		long currentTime = level.getGameTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUUID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			if(target != null) {
				Direction dir = target.direction();
				Vec3 source = Vec3.atCenterOf(worldPosition);
				Vec3 delta = Vec3.atLowerCornerOf(target.blockPos().subtract(worldPosition)).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
				DirectionalMarkerPacket.sendDirectionalMarkers(player, List.of(target), 0xFFFF00, 200);
				BeamPacket.sendBeams(player, source, List.of(delta), 0xFFFF00, 200, false);
			}
			Vec3 lowerCorner = Vec3.atLowerCornerOf(worldPosition).subtract(range, range, range);
			Vec3 size = new Vec3(range*2+1, range*2+1, range*2+1);
			SizedMarkerPacket.sendSizedMarker(player, lowerCorner, size, 0xFF7F00, 200);
		}
	}

	@Override
	public ISettingsCloneable.Result loadConfig(CompoundTag nbt, HolderLookup.Provider registries, Player player) {
		if(!nbt.contains("target")) {
			return ISettingsCloneable.Result.fail(Component.translatable("item.packagedauto.settings_cloner.invalid"));
		}
		int availableCount = 0;
		Inventory playerInventory = player.getInventory();
		if(!itemHandler.getStackInSlot(0).isEmpty()) {
			if(itemHandler.getStackInSlot(0).is(PackagedAutoItems.PROXY_MARKER)) {
				availableCount += itemHandler.getStackInSlot(0).getCount();
			}
			else {
				return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.crafting_proxy.non_marker_present"));
			}
		}
		f:if(availableCount < 1) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.is(PackagedAutoItems.PROXY_MARKER) && stack.isComponentsPatchEmpty()) {
					availableCount += stack.getCount();
				}
				if(availableCount >= 1) {
					break f;
				}
			}
			return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.crafting_proxy.no_markers"));
		}
		int removedCount = itemHandler.getStackInSlot(0).getCount();
		itemHandler.setStackInSlot(0, ItemStack.EMPTY);
		if(removedCount < 1) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.is(PackagedAutoItems.PROXY_MARKER) && stack.isComponentsPatchEmpty()) {
					removedCount += stack.split(1).getCount();
				}
				if(removedCount >= 1) {
					break;
				}
			}
		}
		if(removedCount > 1) {
			ItemStack stack = PackagedAutoItems.PROXY_MARKER.toStack(removedCount-1);
			if(!playerInventory.add(stack)) {
				ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
				item.setThrower(player);
				level.addFreshEntity(item);
			}
		}
		Tag targetTag = nbt.get("target");
		DirectionalGlobalPos globalPos = DirectionalGlobalPos.CODEC.parse(NbtOps.INSTANCE, targetTag).result().get();
		ItemStack stack = PackagedAutoItems.PROXY_MARKER.toStack();
		DataComponentPatch patch = DataComponentPatch.builder().
				set(PackagedAutoDataComponents.MARKER_POS.get(), globalPos).
				build();
		stack.applyComponents(patch);
		itemHandler.setStackInSlot(0, stack);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(CompoundTag nbt, HolderLookup.Provider registries, Player player) {
		if(target == null) {
			return ISettingsCloneable.Result.fail(Component.translatable("block.packagedauto.crafting_proxy.empty"));
		}
		Tag targetTag = DirectionalGlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, target).result().get();
		nbt.put("target", targetTag);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new CraftingProxyMenu(windowId, inventory, this);
	}
}
