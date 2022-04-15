package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;

public class ChangePackagingPacket {

	public ChangePackagingPacket() {}

	public static void encode(ChangePackagingPacket pkt, FriendlyByteBuf buf) {

	}

	public static ChangePackagingPacket decode(FriendlyByteBuf buf) {
		return new ChangePackagingPacket();
	}

	public static void handle(ChangePackagingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof PackagerMenu menu) {
				menu.blockEntity.changePackagingMode();
			}
			if(player.containerMenu instanceof PackagerExtensionMenu menu) {
				menu.blockEntity.changePackagingMode();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
