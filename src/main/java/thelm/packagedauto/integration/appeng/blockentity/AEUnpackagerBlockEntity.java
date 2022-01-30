package thelm.packagedauto.integration.appeng.blockentity;

import java.util.Arrays;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.me.helpers.MachineSource;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.block.UnpackagerBlock;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternDetails;

public class AEUnpackagerBlockEntity extends UnpackagerBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AEUnpackagerBlockEntity>, IActionHost, ICraftingProvider {

	public MachineSource source;
	public IManagedGridNode gridNode;

	public AEUnpackagerBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = new MachineSource(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			getMainNode().create(level, worldPosition);
		}
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % 8 == 0 && getActionableNode().isActive()) {
			chargeMEEnergy();
		}
	}

	@Override
	public void setPlacer(Player placer) {
		if(placer instanceof ServerPlayer serverPlacer) {
			placerID = IPlayerRegistry.getPlayerId(serverPlacer);
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
	public void onSecurityBreak(AEUnpackagerBlockEntity nodeOwner, IGridNode node) {
		level.destroyBlock(worldPosition, true);
	}

	@Override
	public void onSaveChanges(AEUnpackagerBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("Node");
			gridNode.setVisualRepresentation(UnpackagerBlock.INSTANCE);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setFlags(GridFlags.REQUIRE_CHANNEL);
			gridNode.addService(ICraftingProvider.class, this);
			gridNode.setIdlePowerUsage(1);
			gridNode.setInWorldNode(true);
			gridNode.setOwningPlayerId(placerID);
		}
		return gridNode;
	}

	@Override
	public IGridNode getActionableNode() {
		return getMainNode().getNode();
	}

	@Override
	public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
		if(!isBusy() && patternDetails instanceof RecipeCraftingPatternDetails pattern) {
			IntList emptySlots = new IntArrayList();
			for(int i = 0; i < 9; ++i) {
				if(itemHandler.getStackInSlot(i).isEmpty()) {
					emptySlots.add(i);
				}
			}
			List<IPackagePattern> patterns = pattern.recipe.getPatterns();
			if(patterns.size() > emptySlots.size()) {
				return false;
			}
			for(int i = 0; i < patterns.size(); ++i) {
				itemHandler.setStackInSlot(emptySlots.getInt(i), patterns.get(i).getOutput().copy());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return Arrays.stream(trackers).noneMatch(PackageTracker::isEmpty);
	}

	@Override
	public List<IPatternDetails> getAvailablePatterns() {
		ItemStack patternStack = itemHandler.getStackInSlot(9);
		return recipeList.stream().filter(pattern->!pattern.getOutputs().isEmpty()).<IPatternDetails>map(pattern->new RecipeCraftingPatternDetails(patternStack, pattern)).toList();
	}

	@Override
	protected boolean isPatternProvider(BlockEntity blockEntity, Direction direction) {
		return AppEngUtil.isPatternProvider(blockEntity, direction);
	}

	@Override
	public void postPatternChange() {
		ICraftingProvider.requestUpdate(getMainNode());
	}

	protected void chargeMEEnergy() {
		IGrid grid = getActionableNode().getGrid();
		if(grid == null) {
			return;
		}
		IEnergyService energyService = grid.getService(IEnergyService.class);
		if(energyService == null) {
			return;
		}
		double energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored())/2D;
		double canExtract = energyService.extractAEPower(energyRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		double extract = Math.round(canExtract*2)/2D;
		energyStorage.receiveEnergy((int)Math.round(energyService.extractAEPower(extract, Actionable.MODULATE, PowerMultiplier.CONFIG)*2), false);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if(level != null && nbt.contains("Node")) {
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
