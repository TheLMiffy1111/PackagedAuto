package thelm.packagedauto.client.property;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public record BoundItemModelProperty() implements ConditionalItemModelProperty {

	public static final BoundItemModelProperty INSTANCE = new BoundItemModelProperty();
	public static final MapCodec<BoundItemModelProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext context) {
		return stack.has(PackagedAutoDataComponents.MARKER_POS);
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return MAP_CODEC;
	}

}
