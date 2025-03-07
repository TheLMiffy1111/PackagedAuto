package thelm.packagedauto.integration.appeng.blockentity;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.block.CraftingProxyBlock;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;

public class AECraftingProxyBlockEntity extends CraftingProxyBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AECraftingProxyBlockEntity>, IActionHost {

	public boolean firstTick = true;
	public IActionSource source;
	public IManagedGridNode gridNode;

	public AECraftingProxyBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = IActionSource.ofMachine(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			getMainNode().create(level, worldPosition);
		}
		super.tick();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(gridNode != null) {
			gridNode.destroy();
		}
	}

	@Override
	public IGridNode getGridNode(Direction dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(Direction dir) {
		return AECableType.SMART;
	}

	@Override
	public void onSaveChanges(AECraftingProxyBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("Node");
			gridNode.setVisualRepresentation(CraftingProxyBlock.INSTANCE);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setIdlePowerUsage(1);
			gridNode.setInWorldNode(true);
			if(ownerUUID != null && level instanceof ServerLevel) {
				gridNode.setOwningPlayerId(IPlayerRegistry.getMapping(level).getPlayerId(ownerUUID));
			}
		}
		return gridNode;
	}

	@Override
	public IGridNode getActionableNode() {
		return getMainNode().getNode();
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if(nbt.contains("Node")) {
			getMainNode().loadFromNBT(nbt);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if(gridNode != null) {
			gridNode.saveToNBT(nbt);
		}
	}
}
