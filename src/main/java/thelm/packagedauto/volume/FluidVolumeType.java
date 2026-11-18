package thelm.packagedauto.volume;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.capability.VolumeFluidResourceHandler;
import thelm.packagedauto.client.FluidRenderer;

public class FluidVolumeType implements IVolumeType {

	public static final FluidVolumeType INSTANCE = new FluidVolumeType();
	public static final Identifier NAME = Identifier.parse("minecraft:fluid");

	@Override
	public Identifier getName() {
		return NAME;
	}

	@Override
	public Class<?> getTypeClass() {
		return FluidStack.class;
	}

	@Override
	public Class<?> getTypeBaseClass() {
		return Fluid.class;
	}

	@Override
	public MutableComponent getDisplayName() {
		return Component.translatable("volume.packagedauto.minecraft.fluid");
	}

	@Override
	public boolean supportsAE() {
		return true;
	}

	@Override
	public Optional<FluidStack> makeStackFromBase(Object volumeBase, int amount, DataComponentPatch patch) {
		if(volumeBase instanceof Fluid fluid) {
			FluidStack fluidStack = new FluidStack(fluid, amount);
			fluidStack.applyComponents(patch);
			return Optional.of(fluidStack);
		}
		else if(volumeBase instanceof FluidStack fluidStack) {
			fluidStack = fluidStack.copyWithAmount(amount);
			return Optional.of(fluidStack);
		}
		return Optional.empty();
	}

	@Override
	public IVolumeStackWrapper getEmptyStackInstance() {
		return FluidStackWrapper.EMPTY;
	}

	@Override
	public Optional<IVolumeStackWrapper> wrapStack(Object volumeStack) {
		if(volumeStack instanceof FluidStack fluidStack) {
			if(fluidStack.getFluid() instanceof FlowingFluid fFluid && fFluid == fFluid.getFlowing()) {
				DataComponentPatch patch = fluidStack.getComponentsPatch();
				fluidStack = new FluidStack(fFluid.getSource(), fluidStack.getAmount());
				fluidStack.applyComponents(patch);
			}
			return Optional.of(new FluidStackWrapper(fluidStack));
		}
		return Optional.empty();
	}

	@Override
	public Optional<IVolumeStackWrapper> getStackContained(ItemStack container) {
		FluidStack stack = FluidUtil.getFirstStackContained(container);
		return stack.isEmpty() ? Optional.empty() : Optional.of(new FluidStackWrapper(stack));
	}

	@Override
	public void setStack(ItemStack stack, IVolumeStackWrapper volumeStack) {
		if(volumeStack instanceof FluidStackWrapper fluidStack) {
			ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
			if(handler instanceof VolumeFluidResourceHandler vHandler) {
				vHandler.setFluid(fluidStack.stack());
			}
		}
	}

	@Override
	public MapCodec<? extends IVolumeStackWrapper> getStackCodec() {
		return FluidStackWrapper.MAP_CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ? extends IVolumeStackWrapper> getStackStreamCodec() {
		return FluidStackWrapper.STREAM_CODEC;
	}

	@Override
	public ResourceHandler<FluidResource> makeItemCapability(ItemAccess itemAccess) {
		return new VolumeFluidResourceHandler(itemAccess);
	}

	@Override
	public ItemCapability<ResourceHandler<FluidResource>, ItemAccess> getItemCapability() {
		return Capabilities.Fluid.ITEM;
	}

	@Override
	public boolean hasBlockCapability(Level level, BlockPos pos, Direction direction) {
		return level.getCapability(Capabilities.Fluid.BLOCK, pos, direction) != null;
	}

	@Override
	public boolean isEmpty(Level level, BlockPos pos, Direction direction) {
		ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
		if(handler != null) {
			if(handler.size() == 0) {
				return false;
			}
			for(int i = 0; i < handler.size(); ++i) {
				if(handler.getAmountAsLong(i) > 0) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int insert(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, TransactionContext transaction) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
			if(handler != null) {
				return handler.insert(FluidResource.of(fluidStack.stack()), fluidStack.getAmount(), transaction);
			}
		}
		return 0;
	}

	@Override
	public int extract(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, TransactionContext transaction) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
			if(handler != null) {
				return handler.extract(FluidResource.of(fluidStack.stack()), fluidStack.getAmount(), transaction);
			}
		}
		return 0;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, int i, int j, IVolumeStackWrapper stack) {
		if(stack instanceof FluidStackWrapper fluidStack) {
			FluidRenderer.INSTANCE.render(graphics, i, j, fluidStack.stack());
		}
	}
}
