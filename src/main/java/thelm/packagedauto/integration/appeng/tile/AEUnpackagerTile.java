package thelm.packagedauto.integration.appeng.tile;

import java.util.Arrays;

import com.mojang.authlib.GameProfile;

import appeng.api.IAppEngApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.me.helpers.MachineSource;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.networking.BaseGridBlock;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternDetails;
import thelm.packagedauto.tile.UnpackagerTile;

public class AEUnpackagerTile extends UnpackagerTile implements IGridHost, IActionHost, ICraftingProvider {

	public BaseGridBlock<AEUnpackagerTile> gridBlock;
	public MachineSource source;
	public IGridNode gridNode;

	public AEUnpackagerTile() {
		super();
		gridBlock = new BaseGridBlock<>(this);
		source = new MachineSource(this);
	}

	@Override
	public void tick() {
		super.tick();
		if(drawMEEnergy && !level.isClientSide && level.getGameTime() % 8 == 0) {
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
	public IGridNode getGridNode(AEPartLocation dir) {
		return getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
		level.destroyBlock(worldPosition, true);
	}

	@Override
	public IGridNode getActionableNode() {
		if(gridNode == null && level != null && !level.isClientSide) {
			IAppEngApi api = Api.instance();
			gridNode = api.grid().createGridNode(gridBlock);
			if(ownerUUID != null) {
				gridNode.setPlayerID(api.registries().players().getID(new GameProfile(ownerUUID, "[UNKNOWN]")));
			}
			gridNode.updateState();
		}
		return gridNode;
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
		if(!isBusy()) {
			IntList emptySlots = new IntArrayList();
			for(int i = 0; i < 9; ++i) {
				if(itemHandler.getStackInSlot(i).isEmpty()) {
					emptySlots.add(i);
				}
			}
			IntList requiredSlots = new IntArrayList();
			for(int i = 0; i < table.getContainerSize(); ++i) {
				if(!table.getItem(i).isEmpty()) {
					requiredSlots.add(i);
				}
			}
			if(requiredSlots.size() > emptySlots.size()) {
				return false;
			}
			for(int i = 0; i < requiredSlots.size(); ++i) {
				itemHandler.setStackInSlot(emptySlots.getInt(i), table.getItem(requiredSlots.getInt(i)).copy());
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
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ItemStack patternStack = itemHandler.getStackInSlot(9);
		for(IPackageRecipeInfo pattern : recipeList) {
			if(!pattern.getOutputs().isEmpty()) {
				craftingTracker.addCraftingOption(this, new RecipeCraftingPatternDetails(patternStack, pattern).toAEInternal(level));
			}
		}
	}

	@Override
	protected boolean isInterface(TileEntity tile, Direction direction) {
		return AppEngUtil.isInterface(tile, direction);
	}

	@Override
	public void postPatternChange() {
		if(getActionableNode().isActive()) {
			IGrid grid = getActionableNode().getGrid();
			grid.postEvent(new MENetworkCraftingPatternChange(this, getActionableNode()));
		}
	}

	protected void chargeMEEnergy() {
		if(getActionableNode().isActive()) {
			IGrid grid = getActionableNode().getGrid();
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			double conversion = PowerUnits.RF.convertTo(PowerUnits.AE, 1);
			int request = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored()-energyStorage.getEnergyStored());
			double available = energyGrid.extractAEPower((request+0.5)*conversion, Actionable.SIMULATE, PowerMultiplier.CONFIG);
			int extract = (int)(available/conversion);
			energyGrid.extractAEPower(extract*conversion, Actionable.MODULATE, PowerMultiplier.CONFIG);
			energyStorage.receiveEnergy(extract, false);
		}
	}

	@Override
	public void load(BlockState blockState, CompoundNBT nbt) {
		super.load(blockState, nbt);
		if(level != null && nbt.contains("Node")) {
			getActionableNode().loadFromNBT("Node", nbt);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		super.save(nbt);
		if(gridNode != null) {
			gridNode.saveToNBT("Node", nbt);
		}
		return nbt;
	}
}
