package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class CraftingPackageRecipeInfo implements ICraftingPackageRecipeInfo {

	public static final MapCodec<CraftingPackageRecipeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(CraftingPackageRecipeInfo::getRecipeId),
			Codec.INT.fieldOf("width").forGetter(CraftingPackageRecipeInfo::getMatrixWidth),
			Codec.INT.fieldOf("height").forGetter(CraftingPackageRecipeInfo::getMatrixHeight),
			ItemStack.OPTIONAL_CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(9).fieldOf("input").forGetter(CraftingPackageRecipeInfo::getMatrixAsList)).
			apply(instance, CraftingPackageRecipeInfo::new));
	public static final Codec<CraftingPackageRecipeInfo> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, CraftingPackageRecipeInfo> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, CraftingPackageRecipeInfo::getRecipeId,
			ByteBufCodecs.INT, CraftingPackageRecipeInfo::getMatrixWidth,
			ByteBufCodecs.INT, CraftingPackageRecipeInfo::getMatrixHeight,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, CraftingPackageRecipeInfo::getMatrixAsList,
			CraftingPackageRecipeInfo::new);

	private final Identifier id;
	private final CraftingRecipe recipe;
	private final List<ItemStack> input;
	private final CraftingInput matrix;
	private final ItemStack output;
	private final List<IPackagePattern> patterns = new ArrayList<>();

	public CraftingPackageRecipeInfo(Identifier id, int width, int height, List<ItemStack> matrixSer) {
		this.id = id;
		matrix = CraftingInput.of(width, height, matrixSer);
		input = MiscHelper.INSTANCE.condenseStacks(matrix.items());
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i));
		}
		RecipeHolder<?> recipeHolder = MiscHelper.INSTANCE.getRecipeMap().byKey(ResourceKey.create(Registries.RECIPE, id));
		if(recipeHolder != null && recipeHolder.value() instanceof CraftingRecipe craftingRecipe) {
			recipe = craftingRecipe;
			output = recipe.assemble(matrix).copy();
		}
		else {
			recipe = null;
			output = ItemStack.EMPTY;
		}
	}

	public CraftingPackageRecipeInfo(List<ItemStack> inputs, Level level) {
		NonNullList<ItemStack> matrixList = NonNullList.withSize(9, ItemStack.EMPTY);
		int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
		for(int i = 0; i < 9; ++i) {
			ItemStack toSet = inputs.get(slotArray[i]);
			toSet.setCount(1);
			matrixList.set(i, toSet.copy());
		}
		matrix = CraftingInput.of(3, 3, matrixList);
		RecipeHolder<CraftingRecipe> recipeHolder = MiscHelper.INSTANCE.getRecipeMap().getRecipesFor(RecipeType.CRAFTING, matrix, level).findFirst().orElse(null);
		if(recipeHolder != null) {
			id = recipeHolder.id().identifier();
			recipe = recipeHolder.value();
			output = recipe.assemble(matrix).copy();
		}
		else {
			id = null;
			recipe = null;
			output = null;
		}
		input = MiscHelper.INSTANCE.condenseStacks(matrix.items());
		for(int i = 0; i*9 < input.size(); ++i) {
			this.patterns.add(new PackagePattern(this, i));
		}
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return CraftingPackageRecipeType.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return id != null && recipe != null;
	}

	@Override
	public List<IPackagePattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.unmodifiableList(input);
	}

	@Override
	public ItemStack getOutput() {
		return output.copy();
	}

	@Override
	public CraftingRecipe getRecipe() {
		return recipe;
	}

	public Identifier getRecipeId() {
		return id;
	}

	@Override
	public CraftingInput getMatrix() {
		return matrix;
	}

	public List<ItemStack> getMatrixAsList() {
		return Collections.unmodifiableList(matrix.items());
	}

	public int getMatrixWidth() {
		return matrix.width();
	}

	public int getMatrixHeight() {
		return matrix.height();
	}

	@Override
	public List<ItemStack> getRemainingItems() {
		return recipe.getRemainingItems(matrix);
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectArrayMap<>();
		int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
		for(int i = 0; i < matrix.height(); ++i) {
			for(int j = 0; j < matrix.width(); ++j) {
				map.put(slotArray[i*3+j], matrix.getItem(i*matrix.width()+j));
			}
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CraftingPackageRecipeInfo other) {
			return MiscHelper.INSTANCE.recipeEquals(this, recipe, other, other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, recipe);
	}
}
