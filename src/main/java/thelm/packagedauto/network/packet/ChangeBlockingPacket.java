package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.UnpackagerMenu;

public class ChangeBlockingPacket {

	public ChangeBlockingPacket() {}

	public static void encode(ChangeBlockingPacket pkt, FriendlyByteBuf buf) {}

	public static ChangeBlockingPacket decode(FriendlyByteBuf buf) {
		return new ChangeBlockingPacket();
	}

	public static void handle(ChangeBlockingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerMenu menu) {
				menu.blockEntity.changeBlockingMode();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
