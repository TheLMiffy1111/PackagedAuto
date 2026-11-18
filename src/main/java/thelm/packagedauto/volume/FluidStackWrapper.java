package thelm.packagedauto.volume;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;

public record FluidStackWrapper(FluidStack stack) implements IFluidStackWrapper {

	public static final FluidStackWrapper EMPTY = new FluidStackWrapper(FluidStack.EMPTY);

	public static final MapCodec<FluidStackWrapper> MAP_CODEC = FluidStack.MAP_CODEC.xmap(
			FluidStackWrapper::of, FluidStackWrapper::getFluid);
	public static final Codec<FluidStackWrapper> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackWrapper> STREAM_CODEC = FluidStack.STREAM_CODEC.map(
			FluidStackWrapper::of, FluidStackWrapper::getFluid);

	public static FluidStackWrapper of(FluidStack stack) {
		if(stack.isEmpty()) {
			return EMPTY;
		}
		return new FluidStackWrapper(stack);
	}

	@Override
	public IVolumeType getVolumeType() {
		return FluidVolumeType.INSTANCE;
	}

	@Override
	public FluidStack getFluid() {
		return stack;
	}

	@Override
	public int getAmount() {
		return stack.getAmount();
	}

	@Override
	public IVolumeStackWrapper copy() {
		return new FluidStackWrapper(stack.copy());
	}

	@Override
	public IVolumeStackWrapper withAmount(int amount) {
		return new FluidStackWrapper(stack.copyWithAmount(amount));
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public void saveAEKey(ValueOutput output) {
		output.putString("#t", "ae2:f");
		output.store("id", BuiltInRegistries.FLUID.byNameCodec(), stack.getFluid());
		output.store("components", DataComponentPatch.CODEC, stack.getComponentsPatch());
	}

	@Override
	public Component getDisplayName() {
		return stack.getHoverName();
	}

	@Override
	public Component getAmountDesc() {
		return Component.literal(stack.getAmount()+"mB");
	}

	@Override
	public List<Component> getTooltip() {
		return List.of(stack.getHoverName());
	}

	@Override
	public int hashCode() {
		return FluidStack.hashFluidAndComponents(stack);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FluidStackWrapper other) {
			return FluidStack.matches(stack, other.stack);
		}
		return false;
	}
}
