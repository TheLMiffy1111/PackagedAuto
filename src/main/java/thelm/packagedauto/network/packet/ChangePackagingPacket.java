package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.PackagerContainer;
import thelm.packagedauto.container.PackagerExtensionContainer;

public class ChangePackagingPacket {

	public ChangePackagingPacket() {}

	public static void encode(ChangePackagingPacket pkt, PacketBuffer buf) {

	}

	public static ChangePackagingPacket decode(PacketBuffer buf) {
		return new ChangePackagingPacket();
	}

	public static void handle(ChangePackagingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof PackagerContainer) {
				PackagerContainer container = (PackagerContainer)player.containerMenu;
				container.tile.changePackagingMode();
			}
			if(player.containerMenu instanceof PackagerExtensionContainer) {
				PackagerExtensionContainer container = (PackagerExtensionContainer)player.containerMenu;
				container.tile.changePackagingMode();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
