package thelm.packagedauto.client.property;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public record FilledItemModelProperty() implements ConditionalItemModelProperty {

	public static final FilledItemModelProperty INSTANCE = new FilledItemModelProperty();
	public static final MapCodec<FilledItemModelProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext context) {
		return stack.has(PackagedAutoDataComponents.RECIPE_LIST) || stack.has(PackagedAutoDataComponents.CLONER_DATA);
	}

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return MAP_CODEC;
	}

}
