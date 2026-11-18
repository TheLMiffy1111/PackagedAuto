package thelm.packagedauto.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import thelm.packagedauto.api.IPackageRecipeInfo;

public class OrderedProcessingPackageRecipeType extends ProcessingPackageRecipeType {

	public static final OrderedProcessingPackageRecipeType INSTANCE = new OrderedProcessingPackageRecipeType();
	public static final Identifier NAME = Identifier.parse("packagedauto:ordered_processing");

	@Override
	public Identifier getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return Component.translatable("recipe.packagedauto.ordered_processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return Component.translatable("recipe.packagedauto.ordered_processing.short");
	}

	@Override
	public MapCodec<? extends IPackageRecipeInfo> getRecipeInfoMapCodec() {
		return OrderedProcessingPackageRecipeInfo.MAP_CODEC;
	}

	@Override
	public Codec<? extends IPackageRecipeInfo> getRecipeInfoCodec() {
		return OrderedProcessingPackageRecipeInfo.CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ? extends IPackageRecipeInfo> getRecipeInfoStreamCodec() {
		return OrderedProcessingPackageRecipeInfo.STREAM_CODEC;
	}

	@Override
	public IPackageRecipeInfo generateRecipeInfoFromStacks(List<ItemStack> inputs, List<ItemStack> outputs, Level level) {
		return new OrderedProcessingPackageRecipeInfo(inputs, outputs);
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.BREWING_STAND);
	}
}
