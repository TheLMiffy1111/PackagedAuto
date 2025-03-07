package thelm.packagedauto.integration.appeng.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

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
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.recipe.DirectCraftingPatternDetails;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternDetails;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternDetails;
import thelm.packagedauto.util.MiscHelper;

public class AEPackagingProviderBlockEntity extends PackagingProviderBlockEntity implements IInWorldGridNodeHost, IGridNodeListener<AEPackagingProviderBlockEntity>, IActionHost, ICraftingProvider {

	public boolean firstTick = true;
	public IActionSource source;
	public IManagedGridNode gridNode;
	public int roundRobinIndex = 0;

	public AEPackagingProviderBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		source = IActionSource.ofMachine(this);
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			getMainNode().create(level, worldPosition);
			postPatternChange();
		}
		super.tick();
		if(currentPattern != null) {
			sendPackaging();
		}
		if(!toSend.isEmpty()) {
			sendUnpackaging();
		}
	}

	protected void sendPackaging() {
		if(currentPattern == null) {
			return;
		}
		if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IStorageService storageService = grid.getStorageService();
			IEnergyService energyService = grid.getEnergyService();
			MEStorage inventory = storageService.getInventory();
			AEItemKey key = AEItemKey.of(currentPattern.getOutput());
			int inserted = (int)StorageHelper.poweredInsert(energyService, inventory, key, 1, source, Actionable.MODULATE);
			if(inserted == 1) {
				currentPattern = null;
			}
		}
	}

	protected void sendUnpackaging() {
		if(toSend.isEmpty()) {
			return;
		}
		if(sendDirection != null) {
			BlockPos offsetPos = worldPosition.relative(sendDirection);
			BlockEntity blockEntity = level.getBlockEntity(offsetPos);
			if(!validSendTarget(blockEntity, sendDirection.getOpposite())) {
				sendDirection = null;
				return;
			}
			IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, offsetPos, sendDirection.getOpposite());
			for(int i = 0; i < toSend.size(); ++i) {
				ItemStack stack = toSend.get(i);
				ItemStack stackRem = stack;
				if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
						stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, sendDirection.getOpposite())) {
					stackRem = MiscHelper.INSTANCE.fillVolume(level, offsetPos, sendDirection.getOpposite(), stack, false);
				}
				else if(itemHandler != null) {
					stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, sendOrdered, false);
				}
				toSend.set(i, stackRem);
			}
			toSend.removeIf(ItemStack::isEmpty);
			setChanged();
		}
		else if(getMainNode().isActive()) {
			IGrid grid = getMainNode().getGrid();
			IStorageService storageService = grid.getStorageService();
			IEnergyService energyService = grid.getEnergyService();
			MEStorage inventory = storageService.getInventory();
			for(int i = 0; i < toSend.size(); ++i) {
				ItemStack is = toSend.get(i);
				if(is.isEmpty()) {
					continue;
				}
				AEItemKey key = AEItemKey.of(is);
				int count = is.getCount();
				int inserted = (int)StorageHelper.poweredInsert(energyService, inventory, key, count, source, Actionable.MODULATE);
				if(inserted == count) {
					toSend.set(i, ItemStack.EMPTY);
				}
				else {
					toSend.set(i, key.toStack(count-inserted));
				}
			}
			toSend.removeIf(ItemStack::isEmpty);
			setChanged();
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
	public void onSaveChanges(AEPackagingProviderBlockEntity nodeOwner, IGridNode node) {
		setChanged();
	}

	@Override
	public void onStateChanged(AEPackagingProviderBlockEntity nodeOwner, IGridNode node, State state) {
		if(state == State.POWER || state == State.CHANNEL) {
			postPatternChange();
		}
	}

	public IManagedGridNode getMainNode() {
		if(gridNode == null) {
			gridNode = GridHelper.createManagedNode(this, this);
			gridNode.setTagName("node");
			gridNode.setVisualRepresentation(PackagedAutoBlocks.PACKAGING_PROVIDER);
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
		if(getMainNode().isActive() && !isBusy()) {
			IGrid grid = getMainNode().getGrid();
			IEnergyService energyService = grid.getEnergyService();
			double conversion = PowerUnit.FE.convertTo(PowerUnit.AE, 1);
			IPackageRecipeInfo recipe = null;
			if(patternDetails instanceof DirectCraftingPatternDetails pattern) {
				recipe = pattern.recipe;
			}
			else if(patternDetails instanceof RecipeCraftingPatternDetails pattern) {
				recipe = pattern.recipe;
			}
			else if(patternDetails instanceof PackageCraftingPatternDetails pattern) {
				double request = PackagerBlockEntity.energyReq*2*conversion;
				if(request - energyService.extractAEPower(request, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001) {
					return false;
				}
				energyService.extractAEPower(request, Actionable.MODULATE, PowerMultiplier.CONFIG);
				currentPattern = pattern.pattern;
				return true;
			}
			if(recipe != null) {
				double request = (PackagerBlockEntity.energyReq*2+UnpackagerBlockEntity.energyUsage)*conversion;
				if(request - energyService.extractAEPower(request, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001) {
					return false;
				}
				if(recipe.getRecipeType().hasMachine()) {
					List<Direction> directions = Lists.newArrayList(Direction.values());
					Collections.rotate(directions, roundRobinIndex);
					for(Direction direction : Direction.values()) {
						if(level.getBlockEntity(worldPosition.relative(direction)) instanceof IPackageCraftingMachine machine) {
							if(!machine.isBusy() && machine.acceptPackage(recipe, Lists.transform(recipe.getInputs(), ItemStack::copy), direction.getOpposite())) {
								energyService.extractAEPower(request, Actionable.MODULATE, PowerMultiplier.CONFIG);
								roundRobinIndex = (roundRobinIndex+1) % 6;
								return true;
							}
						}
					}
					return false;
				}
				else {
					List<ItemStack> toSend = new ArrayList<>();
					recipe.getInputs().stream().map(ItemStack::copy).forEach(toSend::add);
					List<Direction> directions = Lists.newArrayList(Direction.values());
					Collections.rotate(directions, roundRobinIndex);
					dir:for(Direction direction : directions) {
						BlockPos offsetPos = worldPosition.relative(direction);
						BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
						if(!validSendTarget(blockEntity, direction.getOpposite())) {
							continue;
						}
						IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, offsetPos, direction.getOpposite());
						if(blocking) {
							for(int i = 0; i < toSend.size(); ++i) {
								ItemStack stack = toSend.get(i);
								if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
										stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, direction.getOpposite())) {
									if(!stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().isEmpty(level, offsetPos, direction.getOpposite())) {
										continue dir;
									}
								}
								else if(itemHandler != null && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
									continue dir;
								}
							}
						}
						boolean acceptsAll = true;
						for(int i = 0; i < toSend.size(); ++i) {
							ItemStack stack = toSend.get(i);
							ItemStack stackRem = stack;
							if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
									stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, direction.getOpposite())) {
								stackRem = MiscHelper.INSTANCE.fillVolume(level, offsetPos, direction.getOpposite(), stack, true);
							}
							else if(itemHandler != null) {
								stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, false, true);
							}
							acceptsAll &= stackRem.getCount() < stack.getCount();
						}
						if(acceptsAll) {
							energyService.extractAEPower(request, Actionable.MODULATE, PowerMultiplier.CONFIG);
							sendDirection = direction;
							this.toSend.addAll(toSend);
							sendOrdered = recipe.getRecipeType().isOrdered();
							roundRobinIndex = (roundRobinIndex+1) % 6;
							sendUnpackaging();
							return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	protected boolean validSendTarget(BlockEntity blockEntity, Direction direction) {
		return blockEntity == null ||
				!(blockEntity instanceof IPackageCraftingMachine) &&
				!(blockEntity instanceof PackagerBlockEntity) &&
				!(blockEntity instanceof PackagerExtensionBlockEntity) &&
				!(blockEntity instanceof UnpackagerBlockEntity) &&
				!AppEngUtil.isPatternProvider(blockEntity, direction);
	}

	@Override
	public boolean isBusy() {
		return powered || currentPattern != null || !toSend.isEmpty();
	}

	@Override
	public List<IPatternDetails> getAvailablePatterns() {
		if(getMainNode().isActive()) {
			List<IPatternDetails> patterns = new ArrayList<>();
			RegistryAccess registry = level.registryAccess();
			if(provideDirect) {
				recipeList.stream().filter(pattern->!pattern.getOutputs().isEmpty()).
				map(pattern->new DirectCraftingPatternDetails(pattern, registry)).
				forEach(patterns::add);
			}
			if(providePackaging) {
				recipeList.stream().filter(IPackageRecipeInfo::isValid).
				flatMap(recipe->Streams.concat(recipe.getPatterns().stream(), recipe.getExtraPatterns().stream())).
				map(pattern->new PackageCraftingPatternDetails(pattern, registry)).
				forEach(patterns::add);
			}
			if(provideUnpackaging) {
				recipeList.stream().filter(pattern->!pattern.getOutputs().isEmpty()).
				map(pattern->new RecipeCraftingPatternDetails(pattern, registry)).
				forEach(patterns::add);
			}
			return patterns;
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
