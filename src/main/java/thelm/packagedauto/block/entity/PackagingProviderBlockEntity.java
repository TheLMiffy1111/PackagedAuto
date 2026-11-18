package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.inventory.PackagingProviderItemResourceHandler;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.util.MiscHelper;

public class PackagingProviderBlockEntity extends BaseBlockEntity implements ISettingsCloneable {

	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public List<ItemStack> toSend = new ArrayList<>();
	public Direction sendDirection;
	public boolean sendOrdered;
	public boolean powered = false;
	public boolean blocking = false;
	public boolean provideDirect = true;
	public boolean providePackaging = false;
	public boolean provideUnpackaging = false;

	public PackagingProviderBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.PACKAGING_PROVIDER.get(), pos, state);
		setItemHandler(new PackagingProviderItemResourceHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.packaging_provider");
	}

	@Override
	public String getConfigTypeName() {
		return "block.packagedauto.packaging_provider";
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			setChanged();
		}
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		setChanged();
	}

	public void changeProvideType(Type type) {
		switch(type) {
		case DIRECT -> {
			provideDirect = !provideDirect;
			if(provideDirect && providePackaging && provideUnpackaging) {
				providePackaging = provideUnpackaging = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				providePackaging = provideUnpackaging = true;
			}
		}
		case PACKAGING -> {
			providePackaging = !providePackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
		}
		case UNPACKAGING -> {
			provideUnpackaging = !provideUnpackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
		}
		}
		postPatternChange();
		setChanged();
	}

	public void postPatternChange() {}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if(currentPattern != null) {
			for(ItemStack stack : currentPattern.getInputs()) {
				if(!stack.isEmpty()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		if(!toSend.isEmpty()) {
			for(ItemStack stack : toSend) {
				if(!stack.isEmpty()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
	}

	@Override
	public ISettingsCloneable.Result loadConfig(ValueInput input, Player player) {
		blocking = input.getBooleanOr("blocking", false);
		provideDirect = input.getBooleanOr("direct", false);
		providePackaging = input.getBooleanOr("packaging", false);
		provideUnpackaging = input.getBooleanOr("unpackaging", false);
		Component message = null;
		ValueInputList recipesInput = input.childrenList("recipes").orElse(null);
		if(recipesInput != null) {
			f:if(itemHandler.getStack(0).isEmpty()) {
				Inventory playerInventory = player.getInventory();
				for(int i = 0; i < playerInventory.getContainerSize(); ++i) {
					ItemStack stack = playerInventory.getItem(i);
					if(!stack.isEmpty() && stack.is(PackagedAutoItems.RECIPE_HOLDER) && !stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
						ItemStack stackCopy = stack.split(1);
						List<IPackageRecipeInfo> recipeList = MiscHelper.INSTANCE.deserializeRecipeList(recipesInput);
						if(!recipeList.isEmpty()) {
							DataComponentPatch patch = DataComponentPatch.builder().
									set(PackagedAutoDataComponents.RECIPE_LIST.get(), recipeList).
									build();
							stackCopy.applyComponents(patch);
						}
						itemHandler.setStack(0, stackCopy);
						break f;
					}
				}
			}
		}
		if(message != null) {
			return ISettingsCloneable.Result.partial(message);
		}
		else {
			return ISettingsCloneable.Result.success();
		}
	}

	@Override
	public ISettingsCloneable.Result saveConfig(ValueOutput output, Player player) {
		output.putBoolean("blocking", blocking);
		output.putBoolean("direct", provideDirect);
		output.putBoolean("packaging", providePackaging);
		output.putBoolean("unpackaging", provideUnpackaging);
		if(!recipeList.isEmpty()) {
			MiscHelper.INSTANCE.serializeRecipeList(recipeList, output.childrenList("recipes"));
		}
		return ISettingsCloneable.Result.success();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		blocking = input.getBooleanOr("blocking", false);
		provideDirect = input.getBooleanOr("direct", false);
		providePackaging = input.getBooleanOr("packaging", false);
		provideUnpackaging = input.getBooleanOr("unpackaging", false);
		powered = input.getBooleanOr("powered", false);
		currentPattern = input.read("pattern", IPackagePattern.CODEC).orElse(null);
		MiscHelper.INSTANCE.deserializeItemList(toSend, input.childrenListOrEmpty("to_send"));
		sendDirection = input.read("send_direction", Direction.LEGACY_ID_CODEC).orElse(null);
		sendOrdered = input.getBooleanOr("send_ordered", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("blocking", blocking);
		output.putBoolean("direct", provideDirect);
		output.putBoolean("packaging", providePackaging);
		output.putBoolean("unpackaging", provideUnpackaging);
		output.putBoolean("powered", powered);
		output.storeNullable("pattern", IPackagePattern.CODEC, currentPattern);
		MiscHelper.INSTANCE.serializeItemList(toSend, output.childrenList("to_send"));
		output.storeNullable("send_direction", Direction.LEGACY_ID_CODEC, sendDirection);
		output.putBoolean("send_ordered", sendOrdered);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new PackagingProviderMenu(windowId, inventory, this);
	}

	public static enum Type {
		DIRECT, PACKAGING, UNPACKAGING;

		public static final IntFunction<Type> BY_ID = ByIdMap.continuous(Type::ordinal, values(), OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Type::ordinal);
	}
}
