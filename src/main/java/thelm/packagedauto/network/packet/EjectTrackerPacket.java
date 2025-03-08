package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.UnpackagerMenu;

public record EjectTrackerPacket(int index) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(index);
	}

	public static EjectTrackerPacket decode(FriendlyByteBuf buf) {
		return new EjectTrackerPacket(buf.readUnsignedByte());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerMenu menu) {
				menu.blockEntity.trackers[index].ejectItems();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
