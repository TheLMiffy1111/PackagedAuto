package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.PacketHandler;

public record SizedMarkerPacket(Vec3 lowerCorner, Vec3 size, int color, int lifetime) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeDouble(lowerCorner.x);
		buf.writeDouble(lowerCorner.y);
		buf.writeDouble(lowerCorner.z);
		buf.writeDouble(size.x);
		buf.writeDouble(size.y);
		buf.writeDouble(size.z);
		buf.writeMedium(color);
		buf.writeShort(lifetime);
	}

	public static SizedMarkerPacket decode(FriendlyByteBuf buf) {
		Vec3 lowerCorner = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		Vec3 size = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new SizedMarkerPacket(lowerCorner, size, buf.readUnsignedMedium(), buf.readUnsignedShort());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addSizedMarker(lowerCorner, size, color, lifetime);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendSizedMarker(ServerPlayer player, Vec3 lowerCorner, Vec3 size, int color, int lifetime) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new SizedMarkerPacket(lowerCorner, size, color, lifetime));
	}
}
