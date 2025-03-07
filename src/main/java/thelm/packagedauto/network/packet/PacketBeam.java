package thelm.packagedauto.network.packet;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.network.PacketHandler;

public class PacketBeam implements ISelfHandleMessage<IMessage> {

	private Vec3d source;
	private List<Vec3d> deltas;
	private int color;
	private int lifetime;
	private boolean fadeout;

	public PacketBeam() {}

	public PacketBeam(Vec3d source, List<Vec3d> deltas, int color, int lifetime, boolean fadeout) {
		this.source = source;
		this.deltas = deltas;
		this.color = color;
		this.lifetime = lifetime;
		this.fadeout = fadeout;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(source.x);
		buf.writeDouble(source.y);
		buf.writeDouble(source.z);
		buf.writeByte(deltas.size());
		for(Vec3d delta : deltas) {
			buf.writeDouble(delta.x);
			buf.writeDouble(delta.y);
			buf.writeDouble(delta.z);
		}
		buf.writeMedium(color);
		buf.writeShort(lifetime);
		buf.writeBoolean(fadeout);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		source = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		int size = buf.readByte();
		deltas = new ArrayList<>(size);
		for(int i = 0; i < size; ++i) {
			deltas.add(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		}
		color = buf.readUnsignedMedium();
		lifetime = buf.readUnsignedShort();
		fadeout = buf.readBoolean();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(()->{
			WorldOverlayRenderer.INSTANCE.addBeams(source, deltas, color, lifetime, fadeout);
		});
		return null;
	}

	public static void sendBeams(Vec3d source, List<Vec3d> deltas, int color, int lifetime, boolean fadeout, int dimension, double range) {
		PacketHandler.INSTANCE.sendToAllAround(new PacketBeam(source, deltas, color, lifetime, fadeout), new TargetPoint(dimension, source.x, source.y, source.z, range));
	}

	public static void sendBeams(EntityPlayerMP player, Vec3d source, List<Vec3d> deltas, int color, int lifetime, boolean fadeout) {
		PacketHandler.INSTANCE.sendTo(new PacketBeam(source, deltas, color, lifetime, fadeout), player);
	}
}
