package thelm.packagedauto.integration.patchouli;

import thelm.packagedauto.tile.TileCrafter;
import vazkii.patchouli.api.PatchouliAPI;

public class PackagedAutoPatchouliHandler {

	public static void init() {
		PatchouliAPI.instance.setConfigFlag("packagedauto:crafter", TileCrafter.enabled);
	}
}
