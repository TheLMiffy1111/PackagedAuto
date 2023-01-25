package thelm.packagedauto.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.network.packet.PacketChangeBlocking;
import thelm.packagedauto.network.packet.PacketChangePackaging;
import thelm.packagedauto.network.packet.PacketCycleRecipeType;
import thelm.packagedauto.network.packet.PacketLoadRecipeList;
import thelm.packagedauto.network.packet.PacketSaveRecipeList;
import thelm.packagedauto.network.packet.PacketSetItemStack;
import thelm.packagedauto.network.packet.PacketSetPatternIndex;
import thelm.packagedauto.network.packet.PacketSetRecipe;
import thelm.packagedauto.network.packet.PacketSyncEnergy;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PackagedAuto.MOD_ID);

	public static void registerPackets() {
		int id = 0;
		INSTANCE.registerMessage(PacketSyncEnergy::handle, PacketSyncEnergy.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketSetPatternIndex::handle, PacketSetPatternIndex.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketCycleRecipeType::handle, PacketCycleRecipeType.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketSaveRecipeList::handle, PacketSaveRecipeList.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketSetRecipe::handle, PacketSetRecipe.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketLoadRecipeList::handle, PacketLoadRecipeList.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketChangeBlocking::handle, PacketChangeBlocking.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketSetItemStack::handle, PacketSetItemStack.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketChangePackaging::handle, PacketChangePackaging.class, id++, Side.SERVER);
	}
}
