package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.UnpackagerContainer;

public class TrackerCountPacket {

	private final boolean decrease;

	public TrackerCountPacket(boolean decrease) {
		this.decrease = decrease;
	}

	public void encode(PacketBuffer buf) {
		buf.writeBoolean(decrease);
	}

	public static TrackerCountPacket decode(PacketBuffer buf) {
		return new TrackerCountPacket(buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof UnpackagerContainer) {
				UnpackagerContainer container = (UnpackagerContainer)player.containerMenu;
				container.tile.changeTrackerCount(decrease);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
