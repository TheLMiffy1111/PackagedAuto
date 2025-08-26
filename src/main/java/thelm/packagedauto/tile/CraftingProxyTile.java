package thelm.packagedauto.tile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.block.CraftingProxyBlock;
import thelm.packagedauto.container.CraftingProxyContainer;
import thelm.packagedauto.integration.appeng.tile.AECraftingProxyTile;
import thelm.packagedauto.inventory.CraftingProxyItemHandler;
import thelm.packagedauto.item.ProxyMarkerItem;
import thelm.packagedauto.network.packet.BeamPacket;
import thelm.packagedauto.network.packet.DirectionalMarkerPacket;
import thelm.packagedauto.network.packet.SizedMarkerPacket;
import thelm.packagedauto.util.MiscHelper;

public class CraftingProxyTile extends BaseTile implements IPackageCraftingMachine, ISettingsCloneable {

	public static final TileEntityType<CraftingProxyTile> TYPE_INSTANCE = (TileEntityType<CraftingProxyTile>)TileEntityType.Builder.
			of(MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("appliedenergistics2"),
					()->AECraftingProxyTile::new, ()->CraftingProxyTile::new), CraftingProxyBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:crafting_proxy");

	public static int range = 8;

	public final Cache<UUID, Long> previewTimes = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(60, TimeUnit.SECONDS).build();

	public DirectionalGlobalPos target;

	public CraftingProxyTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new CraftingProxyItemHandler(this));
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.crafting_proxy");
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
				Vector3d source = Vector3d.atCenterOf(worldPosition);
				Vector3d delta = Vector3d.atLowerCornerOf(target.blockPos().subtract(worldPosition)).add(Vector3d.atLowerCornerOf(dir.getNormal()).scale(0.5));
				BeamPacket.sendBeams(source, Collections.singletonList(delta), 0xFF7F00, 6, true, level.dimension(), 32);
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
		TileEntity tile = level.getBlockEntity(pos);
		if(tile != null &&
				!(tile instanceof CraftingProxyTile) &&
				tile instanceof IPackageCraftingMachine) {
			return ((IPackageCraftingMachine)tile).isBusy();
		}
		return true;
	}

	public void sendPreview(ServerPlayerEntity player) {
		long currentTime = level.getGameTime();
		Long cachedTime = previewTimes.getIfPresent(player.getUUID());
		if(cachedTime == null || currentTime-cachedTime > 180) {
			if(target != null) {
				Direction dir = target.direction();
				Vector3d source = Vector3d.atCenterOf(worldPosition);
				Vector3d delta = Vector3d.atLowerCornerOf(target.blockPos().subtract(worldPosition)).add(Vector3d.atLowerCornerOf(dir.getNormal()).scale(0.5));
				DirectionalMarkerPacket.sendDirectionalMarkers(player, Collections.singletonList(target), 0xFFFF00, 200);
				BeamPacket.sendBeams(player, source, Collections.singletonList(delta), 0xFFFF00, 200, false);
			}
			Vector3d lowerCorner = Vector3d.atLowerCornerOf(worldPosition).subtract(range, range, range);
			Vector3d size = new Vector3d(range*2+1, range*2+1, range*2+1);
			SizedMarkerPacket.sendSizedMarker(player, lowerCorner, size, 0xFF7F00, 200);
		}
	}

	@Override
	public ISettingsCloneable.Result loadConfig(CompoundNBT nbt, PlayerEntity player) {
		if(!nbt.contains("Target")) {
			return ISettingsCloneable.Result.fail(new TranslationTextComponent("item.packagedauto.settings_cloner.invalid"));
		}
		int availableCount = 0;
		PlayerInventory playerInventory = player.inventory;
		if(!itemHandler.getStackInSlot(0).isEmpty()) {
			if(itemHandler.getStackInSlot(0).getItem() == ProxyMarkerItem.INSTANCE) {
				availableCount += itemHandler.getStackInSlot(0).getCount();
			}
			else {
				return ISettingsCloneable.Result.fail(new TranslationTextComponent("block.packagedauto.crafting_proxy.non_marker_present"));
			}
		}
		f:if(availableCount < 1) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.getItem() == ProxyMarkerItem.INSTANCE && !stack.hasTag()) {
					availableCount += stack.getCount();
				}
				if(availableCount >= 1) {
					break f;
				}
			}
			return ISettingsCloneable.Result.fail(new TranslationTextComponent("block.packagedauto.crafting_proxy.no_markers"));
		}
		int removedCount = itemHandler.getStackInSlot(0).getCount();
		itemHandler.setStackInSlot(0, ItemStack.EMPTY);
		if(removedCount < 1) {
			for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
				ItemStack stack = playerInventory.getItem(i);
				if(!stack.isEmpty() && stack.getItem() == ProxyMarkerItem.INSTANCE && !stack.hasTag()) {
					removedCount += stack.split(1).getCount();
				}
				if(removedCount >= 1) {
					break;
				}
			}
		}
		if(removedCount > 1) {
			ItemStack stack = new ItemStack(ProxyMarkerItem.INSTANCE, removedCount-1);
			if(!playerInventory.add(stack)) {
				ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
				item.setThrower(player.getUUID());
				level.addFreshEntity(item);
			}
		}
		CompoundNBT targetTag = nbt.getCompound("Target");
		RegistryKey<World> dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(targetTag.getString("Dimension")));
		int[] posArray = targetTag.getIntArray("Position");
		BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		Direction direction = Direction.from3DDataValue(targetTag.getByte("Direction"));
		DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dimension, blockPos, direction);
		ItemStack stack = new ItemStack(ProxyMarkerItem.INSTANCE);
		ProxyMarkerItem.INSTANCE.setDirectionalGlobalPos(stack, globalPos);
		itemHandler.setStackInSlot(0, stack);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public ISettingsCloneable.Result saveConfig(CompoundNBT nbt, PlayerEntity player) {
		if(target == null) {
			return ISettingsCloneable.Result.fail(new TranslationTextComponent("block.packagedauto.crafting_proxy.empty"));
		}
		CompoundNBT targetTag = new CompoundNBT();
		targetTag.putString("Dimension", target.dimension().location().toString());
		targetTag.putIntArray("Position", new int[] {target.x(), target.y(), target.z()});
		targetTag.putByte("Direction", (byte)target.direction().get3DDataValue());
		nbt.put("Target", targetTag);
		return ISettingsCloneable.Result.success();
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new CraftingProxyContainer(windowId, playerInventory, this);
	}
}
