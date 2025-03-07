package thelm.packagedauto.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.PacketHandler;

public class BeamPacket {

	private final Vector3d source;
	private final List<Vector3d> deltas;
	private final int color;
	private final int lifetime;
	private final boolean fadeout;

	public BeamPacket(Vector3d source, List<Vector3d> deltas, int color, int lifetime, boolean fadeout) {
		this.source = source;
		this.deltas = deltas;
		this.color = color;
		this.lifetime = lifetime;
		this.fadeout = fadeout;
	}

	public void encode(PacketBuffer buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeByte(deltas.size());
		for(Vector3d delta : deltas) {
			buf.writeDouble(delta.x);
			buf.writeDouble(delta.y);
			buf.writeDouble(delta.z);
		}
		buf.writeMedium(color);
		buf.writeShort(lifetime);
		buf.writeBoolean(fadeout);
	}

	public static BeamPacket decode(PacketBuffer buf) {
		Vector3d source = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		int size = buf.readByte();
		List<Vector3d> deltas = new ArrayList<>(size);
		for(int i = 0; i < size; ++i) {
			deltas.add(new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		}
		return new BeamPacket(source, deltas, buf.readUnsignedMedium(), buf.readUnsignedShort(), buf.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addBeams(source, deltas, color, lifetime, fadeout);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendBeams(Vector3d source, List<Vector3d> deltas, int color, int lifetime, boolean fadeout, RegistryKey<World> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(source.x, source.y, source.z, range, dimension)), new BeamPacket(source, deltas, color, lifetime, fadeout));
	}

	public static void sendBeams(ServerPlayerEntity player, Vector3d source, List<Vector3d> deltas, int color, int lifetime, boolean fadeout) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new BeamPacket(source, deltas, color, lifetime, fadeout));
	}
}
