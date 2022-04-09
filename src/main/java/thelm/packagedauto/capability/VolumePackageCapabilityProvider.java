package thelm.packagedauto.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeType;

public class VolumePackageCapabilityProvider implements ICapabilityProvider {

	protected ItemStack container;

	public VolumePackageCapabilityProvider(ItemStack container) {
		this.container = container;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if(container.getItem() instanceof IVolumePackageItem volumePackage) {
			IVolumeType type = volumePackage.getVolumeType(container);
			return type.getItemCapability().orEmpty(capability, LazyOptional.of(()->type.makeItemCapability(container)));
		}
		return LazyOptional.empty();
	}
}
