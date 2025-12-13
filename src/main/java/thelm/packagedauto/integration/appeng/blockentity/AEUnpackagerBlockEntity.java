package thelm.packagedauto.integration.appeng.blockentity;

import java.util.Arrays;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
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
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternDetails;

public class AEUnpackagerBlockEntity extends UnpackagerBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AEUnpackagerBlockEntity>, IActionHost, ICraftingProvider {

	public IActionSource source;
	public IManagedGridNode gridNode;

	public AEUnpackagerBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = IActionSource.ofMachine(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			getMainNode().create(level, worldPosition);
		}
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % refreshInterval == 0) {
			chargeMEEnergy();
		}
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
	public void onSaveChanges(AEUnpackagerBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	@Override
	public void onStateChanged(AEUnpackagerBlockEntity nodeOwner, IGridNode node, State state) {
		if(state == State.POWER || state == State.CHANNEL) {
			postPatternChange();
		}
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("node");
			gridNode.setVisualRepresentation(PackagedAutoBlocks.UNPACKAGER);
			gridNode.setGridColor(AEColor.TRANSPARENT);
			gridNode.setFlags(GridFlags.REQUIRE_CHANNEL);
			gridNode.addService(ICraftingProvider.class, this);
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
	public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
		if(getMainNode().isActive() && !isBusy() && patternDetails instanceof RecipeCraftingPatternDetails pattern) {
			int energyReq = energyUsage*pattern.recipe.getPatterns().size();
			if(energyStorage.getEnergyStored() >= energyReq) {
				PackageTracker tracker = Arrays.stream(trackers).limit(trackerCount).filter(PackageTracker::isEmpty).findFirst().get();
				tracker.fillRecipe(pattern.recipe);
				energyStorage.extractEnergy(energyReq, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return Arrays.stream(trackers).limit(trackerCount).noneMatch(PackageTracker::isEmpty);
	}

	@Override
	public List<IPatternDetails> getAvailablePatterns() {
		if(getMainNode().isActive()) {
			RegistryAccess registry = level.registryAccess();
			return recipeList.stream().filter(IPackageRecipeInfo::isCraftable).
					<IPatternDetails>map(pattern->new RecipeCraftingPatternDetails(pattern, registry)).
					toList();
		}
		else {
			return List.of();
		}
	}

	@Override
	public void postPatternChange() {
		ICraftingProvider.requestUpdate(getMainNode());
	}

	@Override
	protected boolean validSendTarget(BlockEntity blockEntity, Direction direction) {
		return super.validSendTarget(blockEntity, direction) && !AppEngUtil.isPatternProvider(blockEntity, direction);
	}

	protected void chargeMEEnergy() {
		if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IEnergyService energyService = grid.getEnergyService();
			double conversion = PowerUnit.FE.convertTo(PowerUnit.AE, 1);
			int request = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());
			double available = energyService.extractAEPower((request+0.5)*conversion, Actionable.SIMULATE, PowerMultiplier.CONFIG);
			int extract = (int)(available/conversion);
			energyService.extractAEPower(extract*conversion, Actionable.MODULATE, PowerMultiplier.CONFIG);
			energyStorage.receiveEnergy(extract, false);
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		if(nbt.contains("node")) {
			getMainNode().loadFromNBT(nbt);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		if(gridNode != null) {
			gridNode.saveToNBT(nbt);
		}
	}
}
