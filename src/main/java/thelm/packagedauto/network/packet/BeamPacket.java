package thelm.packagedauto.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.PacketHandler;

public record BeamPacket(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeByte(deltas.size());
		for(Vec3 delta : deltas) {
			buf.writeDouble(delta.x);
			buf.writeDouble(delta.y);
			buf.writeDouble(delta.z);
		}
		buf.writeMedium(color);
		buf.writeShort(lifetime);
		buf.writeBoolean(fadeout);
	}

	public static BeamPacket decode(FriendlyByteBuf buf) {
		Vec3 source = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		int size = buf.readByte();
		List<Vec3> deltas = new ArrayList<>(size);
		for(int i = 0; i < size; ++i) {
			deltas.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		}
		return new BeamPacket(source, deltas, buf.readUnsignedMedium(), buf.readUnsignedShort(), buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addBeams(source, deltas, color, lifetime, fadeout);	
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendBeams(Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout, ResourceKey<Level> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(source.x, source.y, source.z, range, dimension)), new BeamPacket(source, deltas, color, lifetime, fadeout));
	}

	public static void sendBeams(ServerPlayer player, Vec3 source, List<Vec3> deltas, int color, int lifetime, boolean fadeout) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new BeamPacket(source, deltas, color, lifetime, fadeout));
	}
}
