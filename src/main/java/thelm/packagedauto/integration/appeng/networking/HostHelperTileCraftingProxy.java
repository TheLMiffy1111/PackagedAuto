package thelm.packagedauto.integration.appeng.networking;

import appeng.api.networking.GridFlags;
import thelm.packagedauto.tile.TileCraftingProxy;

public class HostHelperTileCraftingProxy extends HostHelperTile<TileCraftingProxy> {

	public HostHelperTileCraftingProxy(TileCraftingProxy tile) {
		super(tile);
		gridBlock.flags.remove(GridFlags.REQUIRE_CHANNEL);
	}
}
