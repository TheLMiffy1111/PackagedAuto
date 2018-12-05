package thelm.packagedauto.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.network.packet.PacketCycleRecipeType;
import thelm.packagedauto.network.packet.PacketSaveRecipeList;
import thelm.packagedauto.network.packet.PacketSetPatternIndex;
import thelm.packagedauto.network.packet.PacketSetRecipe;
import thelm.packagedauto.network.packet.PacketSyncEnergy;

public class PacketHandler<REQ extends ISelfHandleMessage> implements IMessageHandler<REQ, IMessage> {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PackagedAuto.MOD_ID);

	public static void registerPackets() {
		int id = 0;
		INSTANCE.registerMessage(get(), PacketSyncEnergy.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(get(), PacketSetPatternIndex.class, id++, Side.SERVER);
		INSTANCE.registerMessage(get(), PacketCycleRecipeType.class, id++, Side.SERVER);
		INSTANCE.registerMessage(get(), PacketSaveRecipeList.class, id++, Side.SERVER);
		INSTANCE.registerMessage(get(), PacketSetRecipe.class, id++, Side.SERVER);
	}

	public static <REQ extends ISelfHandleMessage> PacketHandler<REQ> get() {
		return new PacketHandler<>();
	}

	@Override
	public IMessage onMessage(ISelfHandleMessage message, MessageContext ctx) {
		return message.onMessage(ctx);
	}
}
