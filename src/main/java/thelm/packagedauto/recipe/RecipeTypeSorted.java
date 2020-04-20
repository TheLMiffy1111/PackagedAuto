package thelm.packagedauto.recipe;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.api.IRecipeInfo;

public class RecipeTypeSorted extends RecipeTypeProcessing {
    public static final RecipeTypeSorted INSTANCE = new RecipeTypeSorted();
    public static final ResourceLocation NAME = new ResourceLocation("packagedauto:sorted");

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal("recipe.packagedauto.sorted");
    }

    @Override
    public String getLocalizedNameShort() {
        return I18n.translateToLocal("recipe.packagedauto.sorted.short");
    }

    @Override
    public IRecipeInfo getNewRecipeInfo() {
        return new RecipeInfoSorted();
    }
}
