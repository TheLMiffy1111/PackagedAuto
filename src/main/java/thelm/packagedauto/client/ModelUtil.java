package thelm.packagedauto.client;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModelUtil {

	private static List<Pair<ModelResourceLocation, IBakedModel>> registerModels = new LinkedList<>();
	private static List<IModelBakeCallback> modelBakeCallbacks = new LinkedList<>();

	public static void registerCallback(IModelBakeCallback callback) {
		modelBakeCallbacks.add(callback);
	}

	public static void register(ModelResourceLocation location, IBakedModel model) {
		registerModels.add(Pair.of(location, model));
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for(Pair<ModelResourceLocation, IBakedModel> pair : registerModels) {
			event.getModelRegistry().putObject(pair.getKey(), pair.getValue());
		}
		for(IModelBakeCallback callback : modelBakeCallbacks) {
			callback.onModelBake(event.getModelRegistry());
		}
	}

	public interface IModelBakeCallback {

		void onModelBake(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry);
	}
}
