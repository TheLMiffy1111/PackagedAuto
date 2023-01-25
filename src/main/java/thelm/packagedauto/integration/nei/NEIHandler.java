package thelm.packagedauto.integration.nei;

import java.awt.Rectangle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.event.NEIConfigsLoadedEvent;
import codechicken.nei.recipe.BookmarkRecipeId;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import thelm.packagedauto.client.gui.GuiEncoder;

public class NEIHandler {

	private static final Logger LOGGER = LogManager.getLogger();
	public static final NEIHandler INSTANCE = new NEIHandler();
	private static final ListMultimap<String, ICraftingHandler> HANDLERS = MultimapBuilder.treeKeys().arrayListValues().build();
	private static Function<TemplateRecipeHandler.RecipeTransferRect, String> getCategory;
	private static Function<TemplateRecipeHandler.RecipeTransferRect, Rectangle> getArea;
	private static Function<ArrayList<ICraftingHandler>, GuiCraftingRecipe> createGui;

	static {
		try {
			Field f = TemplateRecipeHandler.RecipeTransferRect.class.getDeclaredField("outputId");
			f.setAccessible(true);
			getCategory = rect->{
				try {
					return (String)f.get(rect);
				}
				catch(Exception e) {
					LOGGER.error("Unexpected error when getting recipe category", e);
					return null;
				}
			};
		}
		catch(Exception e) {
			LOGGER.error("Unexpected error when getting recipe category field", e);
			getCategory = rect->null;
		}
		try {
			Field f = TemplateRecipeHandler.RecipeTransferRect.class.getDeclaredField("rect");
			f.setAccessible(true);
			getArea = rect->{
				try {
					return (Rectangle)f.get(rect);
				}
				catch(Exception e) {
					LOGGER.error("Unexpected error when getting recipe area", e);
					return null;
				}
			};
		}
		catch(Exception e) {
			LOGGER.error("Unexpected error when getting recipe area field", e);
			getArea = rect->null;
		}
		try {
			Constructor<GuiCraftingRecipe> c = GuiCraftingRecipe.class.getDeclaredConstructor(ArrayList.class, BookmarkRecipeId.class);
			c.setAccessible(true);
			createGui = handlers->{
				try {
					return c.newInstance(handlers, null);
				}
				catch(Exception e) {
					LOGGER.error("Unexpected error when creating recipe gui", e);
					return null;
				}
			};
		}
		catch(Exception e) {
			LOGGER.error("Unexpected error when getting recipe gui constructor", e);
			createGui = handlers->null;
		}
	}

	private NEIHandler() {}

	public void register() {
		MinecraftForge.EVENT_BUS.register(INSTANCE);
		API.registerNEIGuiHandler(EncoderNEIGuiHandler.INSTANCE);
	}

	// Using the event to use the built in recipe transfer functionality on most recipes, requiring GTNH's NEI fork
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onNEIConfigsLoaded(NEIConfigsLoadedEvent event) {
		HANDLERS.clear();
		Stream.concat(GuiCraftingRecipe.craftinghandlers.stream(), GuiCraftingRecipe.serialCraftingHandlers.stream()).
		forEach(handler->{
			for(String category : getRecipeCategories(handler)) {
				API.registerGuiOverlayHandler(GuiEncoder.class, EncoderOverlayHandler.INSTANCE, category);
				HANDLERS.put(category, handler.getRecipeHandler(category));
			}
		});
	}

	public List<String> getAllRecipeCategories() {
		return new ArrayList<>(HANDLERS.keySet());
	}

	public Set<String> getRecipeCategories(IRecipeHandler recipeHandler) {
		Set<String> categories = new TreeSet<>();
		if(recipeHandler instanceof TemplateRecipeHandler) {
			((TemplateRecipeHandler)recipeHandler).transferRects.stream().map(getCategory).
			filter(Objects::nonNull).distinct().forEach(categories::add);
		}
		String cat = recipeHandler.getOverlayIdentifier();
		if(cat != null) {
			categories.add(cat);
		}
		return categories;
	}

	@SideOnly(Side.CLIENT)
	public void showCategories(List<String> categories) {
		ArrayList<ICraftingHandler> handlers = categories.stream().flatMap(c->HANDLERS.get(c).stream()).
				filter(h->h.numRecipes() > 0).collect(Collectors.toCollection(()->new ArrayList<>()));
		if(handlers.isEmpty()) {
			return;
		}
		GuiCraftingRecipe gui = createGui.apply(handlers);
		if(gui != null) {
			Minecraft.getMinecraft().displayGuiScreen(gui);
		}
	}

	public Pair<List<PositionedStack>, List<PositionedStack>> getInputOutputLists(IRecipeHandler recipeHandler, int recipeIndex) {
		List<PositionedStack> inputs = new ArrayList<>();
		List<PositionedStack> outputs = new ArrayList<>();
		inputs.addAll(recipeHandler.getIngredientStacks(recipeIndex));
		PositionedStack mainOutput = recipeHandler.getResultStack(recipeIndex);
		if(mainOutput != null) {
			outputs.add(mainOutput);
		}
		int x = 88;
		if(recipeHandler instanceof TemplateRecipeHandler) {
			Rectangle rect = ((TemplateRecipeHandler)recipeHandler).transferRects.stream().
					filter(r->recipeHandler.getOverlayIdentifier().equals(getCategory.apply(r))).
					findAny().map(getArea).orElse(null);
			if(rect != null) {
				x = rect.x + rect.width/2;
			}
		}
		for(PositionedStack other : recipeHandler.getOtherStacks(recipeIndex)) {
			(other.relx <= x ? inputs : outputs).add(other);
		}
		return Pair.of(inputs, outputs);
	}
}
