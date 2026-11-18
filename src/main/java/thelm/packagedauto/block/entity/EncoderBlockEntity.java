package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.inventory.EncoderItemResourceHandler;
import thelm.packagedauto.inventory.EncoderPatternItemResourceHandler;
import thelm.packagedauto.menu.EncoderMenu;

public class EncoderBlockEntity extends BaseBlockEntity {

	public static int patternSlots = 20;
	public static Set<String> disabledRecipeTypes = Set.of();

	public final EncoderPatternItemResourceHandler[] patternItemHandlers = new EncoderPatternItemResourceHandler[patternSlots];
	public int patternIndex;

	public EncoderBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.ENCODER.get(), pos, state);
		setItemHandler(new EncoderItemResourceHandler(this));
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			patternItemHandlers[i] = new EncoderPatternItemResourceHandler(this);
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.encoder");
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		for(EncoderPatternItemResourceHandler inv : patternItemHandlers) {
			inv.updateRecipeInfo(false);
		}
	}

	@Override
	protected void loadSync(ValueInput input) {
		super.loadSync(input);
		patternIndex = input.getByteOr("pattern_index", (byte)0);
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			input.readChild(String.format("pattern_%02d", i), patternItemHandlers[i]);
		}
	}

	@Override
	protected void saveSync(ValueOutput output) {
		super.saveSync(output);
		output.putByte("pattern_index", (byte)patternIndex);
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			output.putChild(String.format("pattern_%02d", i), patternItemHandlers[i]);
		}
	}

	public void setPatternIndex(int patternIndex) {
		this.patternIndex = patternIndex;
		sync(false);
		setChanged();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = itemHandler.getStack(0);
		if(!stack.isEmpty()) {
			List<IPackageRecipeInfo> recipeList = new ArrayList<>();
			if(!single) {
				for(EncoderPatternItemResourceHandler inv : patternItemHandlers) {
					if(inv.recipeInfo != null) {
						recipeList.add(inv.recipeInfo);
					}
				}
			}
			else {
				EncoderPatternItemResourceHandler inv = patternItemHandlers[patternIndex];
				if(inv.recipeInfo != null) {
					recipeList.add(inv.recipeInfo);
				}
			}
			DataComponentPatch patch;
			if(recipeList.isEmpty()) {
				patch = DataComponentPatch.builder().
						remove(PackagedAutoDataComponents.RECIPE_LIST.get()).
						build();
			}
			else {
				patch = DataComponentPatch.builder().
						set(PackagedAutoDataComponents.RECIPE_LIST.get(), recipeList).
						build();
			}
			stack.applyComponents(patch);
		}
	}

	public void loadRecipeList(boolean single, boolean clear) {
		ItemStack stack = itemHandler.getStack(0);
		if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			List<IPackageRecipeInfo> recipeList = stack.get(PackagedAutoDataComponents.RECIPE_LIST);
			if(single) {
				EncoderPatternItemResourceHandler inv = patternItemHandlers[patternIndex];
				if(!clear && !recipeList.isEmpty()) {
					IPackageRecipeInfo recipe = recipeList.get(0);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
			else for(int i = 0; i < patternItemHandlers.length; ++i) {
				EncoderPatternItemResourceHandler inv = patternItemHandlers[i];
				if(!clear && i < recipeList.size()) {
					IPackageRecipeInfo recipe = recipeList.get(i);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
		}
		else if(single) {
			patternItemHandlers[patternIndex].setRecipe(null);
		}
		else for(EncoderPatternItemResourceHandler inv : patternItemHandlers) {
			inv.setRecipe(null);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new EncoderMenu(windowId, inventory, this);
	}
}
