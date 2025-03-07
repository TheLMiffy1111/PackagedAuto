package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.PacketHandler;

public class SizedMarkerPacket {

	private final Vector3d lowerCorner;
	private final Vector3d size;
	private final int color;
	private final int lifetime;

	public SizedMarkerPacket(Vector3d lowerCorner, Vector3d size, int color, int lifetime) {
		this.lowerCorner = lowerCorner;
		this.size = size;
		this.color = color;
		this.lifetime = lifetime;
	}

	public void encode(PacketBuffer buf) {
		buf.writeDouble(lowerCorner.x);
		buf.writeDouble(lowerCorner.y);
		buf.writeDouble(lowerCorner.z);
		buf.writeDouble(size.x);
		buf.writeDouble(size.y);
		buf.writeDouble(size.z);
		buf.writeMedium(color);
		buf.writeShort(lifetime);
	}

	public static SizedMarkerPacket decode(PacketBuffer buf) {
		Vector3d lowerCorner = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		Vector3d size = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new SizedMarkerPacket(lowerCorner, size, buf.readUnsignedMedium(), buf.readUnsignedShort());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addSizedMarker(lowerCorner, size, color, lifetime);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendSizedMarker(ServerPlayerEntity player, Vector3d lowerCorner, Vector3d size, int color, int lifetime) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new SizedMarkerPacket(lowerCorner, size, color, lifetime));
	}
}
