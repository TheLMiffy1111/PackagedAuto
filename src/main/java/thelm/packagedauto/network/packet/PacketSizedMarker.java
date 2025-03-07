package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.network.PacketHandler;

public class PacketSizedMarker implements ISelfHandleMessage<IMessage> {

	private Vec3d lowerCorner;
	private Vec3d size;
	private int color;
	private int lifetime;

	public PacketSizedMarker() {}

	public PacketSizedMarker(Vec3d lowerCorner, Vec3d size, int color, int lifetime) {
		this.lowerCorner = lowerCorner;
		this.size = size;
		this.color = color;
		this.lifetime = lifetime;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(lowerCorner.x);
		buf.writeDouble(lowerCorner.y);
		buf.writeDouble(lowerCorner.z);
		buf.writeDouble(size.x);
		buf.writeDouble(size.y);
		buf.writeDouble(size.z);
		buf.writeMedium(color);
		buf.writeShort(lifetime);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		lowerCorner = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		size = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		color = buf.readUnsignedMedium();
		lifetime = buf.readUnsignedShort();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(()->{
			WorldOverlayRenderer.INSTANCE.addSizedMarker(lowerCorner, size, color, lifetime);
		});
		return null;
	}

	public static void sendSizedMarker(EntityPlayerMP player, Vec3d lowerCorner, Vec3d size, int color, int lifetime) {
		PacketHandler.INSTANCE.sendTo(new PacketSizedMarker(lowerCorner, size, color, lifetime), player);
	}
}
