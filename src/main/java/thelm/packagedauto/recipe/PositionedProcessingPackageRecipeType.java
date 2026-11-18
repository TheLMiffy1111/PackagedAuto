package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.util.MiscHelper;

public class PositionedProcessingPackageRecipeType extends OrderedProcessingPackageRecipeType {

	public static final PositionedProcessingPackageRecipeType INSTANCE = new PositionedProcessingPackageRecipeType();
	public static final Identifier NAME = Identifier.parse("packagedauto:positioned_processing");

	protected PositionedProcessingPackageRecipeType() {}

	@Override
	public Identifier getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return Component.translatable("recipe.packagedauto.positioned_processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return Component.translatable("recipe.packagedauto.positioned_processing.short");
	}

	@Override
	public MapCodec<? extends IPackageRecipeInfo> getRecipeInfoMapCodec() {
		return PositionedProcessingPackageRecipeInfo.MAP_CODEC;
	}

	@Override
	public Codec<? extends IPackageRecipeInfo> getRecipeInfoCodec() {
		return PositionedProcessingPackageRecipeInfo.CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ? extends IPackageRecipeInfo> getRecipeInfoStreamCodec() {
		return PositionedProcessingPackageRecipeInfo.STREAM_CODEC;
	}

	@Override
	public IPackageRecipeInfo generateRecipeInfoFromStacks(List<ItemStack> inputs, List<ItemStack> outputs, Level level) {
		return new PositionedProcessingPackageRecipeInfo(inputs, outputs);
	}

	@Override
	public boolean hasMachine() {
		return true;
	}

	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeSlotsViewWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		List<IRecipeSlotViewWrapper> slotViews = recipeLayoutWrapper.getRecipeSlotViews();
		int index = 0;
		List<ItemStack> output = new ArrayList<>();
		for(IRecipeSlotViewWrapper slotView : slotViews) {
			if(slotView.isInput() && index >= 81) {
				continue;
			}
			Object displayed = slotView.getDisplayedIngredient().orElse(null);
			ItemStack stack = displayed instanceof ItemStack item ? item : MiscHelper.INSTANCE.tryMakeVolumePackage(displayed);
			if(!stack.isEmpty()) {
				if(slotView.isInput()) {
					map.put(index, stack);
				}
				else if(slotView.isOutput()) {
					output.add(stack);
				}
			}
			if(slotView.isInput()) {
				index++;
			}
		}
		output = MiscHelper.INSTANCE.condenseStacks(output, true);
		for(int i = 0; i < output.size() && i < 9; ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(PackagedAutoBlocks.DISTRIBUTOR);
	}
}
