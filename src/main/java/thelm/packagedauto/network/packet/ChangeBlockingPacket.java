package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.UnpackagerContainer;

public class ChangeBlockingPacket {

	public ChangeBlockingPacket() {}

	public static void encode(ChangeBlockingPacket pkt, PacketBuffer buf) {

	}

	public static ChangeBlockingPacket decode(PacketBuffer buf) {
		return new ChangeBlockingPacket();
	}

	public static void handle(ChangeBlockingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerContainer) {
				UnpackagerContainer container = (UnpackagerContainer)player.containerMenu;
				container.tile.changeBlockingMode();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
