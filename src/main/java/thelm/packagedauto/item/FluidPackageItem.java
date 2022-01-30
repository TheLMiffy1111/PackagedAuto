package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import thelm.packagedauto.api.IFluidPackageItem;

public class FluidPackageItem extends Item implements IFluidPackageItem {

	public static final FluidPackageItem INSTANCE = new FluidPackageItem();

	protected FluidPackageItem() {
		super(new Item.Properties());
		setRegistryName("packagedauto:fluid_package");
	}

	public static ItemStack makeFluidPackage(FluidStack fluidStack) {
		ItemStack stack = new ItemStack(INSTANCE);
		stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).
		lazyMap(f->(FluidPackageFluidHandlerItem)f).
		ifPresent(f->f.setFluid(fluidStack));
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		FluidStack fluidStack = getFluidStack(stack);
		if(fluidStack != null) {
			tooltip.add(new TextComponent(fluidStack.getAmount()+"mB ").append(fluidStack.getDisplayName()));
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	@Override
	public FluidStack getFluidStack(ItemStack stack) {
		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).
				lazyMap(f->(FluidPackageFluidHandlerItem)f).
				lazyMap(f->f.getFluid()).orElse(FluidStack.EMPTY);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new FluidPackageFluidHandlerItem(stack);
	}
}
