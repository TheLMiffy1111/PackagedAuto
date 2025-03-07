package thelm.packagedauto.network.packet;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.network.PacketHandler;

public class PacketDirectionalMarker implements ISelfHandleMessage<IMessage> {

	private List<DirectionalGlobalPos> positions;
	private int color;
	private int lifetime;

	public PacketDirectionalMarker() {}

	public PacketDirectionalMarker(List<DirectionalGlobalPos> positions, int color, int lifetime) {
		this.positions = positions;
		this.color = color;
		this.lifetime = lifetime;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(positions.size());
		for(DirectionalGlobalPos globalPos : positions) {
			buf.writeInt(globalPos.dimension());
			buf.writeInt(globalPos.x());
			buf.writeInt(globalPos.y());
			buf.writeInt(globalPos.z());
			buf.writeByte(globalPos.direction().getIndex());
		}
		buf.writeMedium(color);
		buf.writeShort(lifetime);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int size = buf.readByte();
		positions = new ArrayList<>(size);
		for(int i = 0; i < size; ++i) {
			int dimension = buf.readInt();
			BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			EnumFacing direction = EnumFacing.byIndex(buf.readByte());
			positions.add(new DirectionalGlobalPos(dimension, pos, direction));
		}
		color = buf.readUnsignedMedium();
		lifetime = buf.readUnsignedShort();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(()->{
			WorldOverlayRenderer.INSTANCE.addDirectionalMarkers(positions, color, lifetime);
		});
		return null;
	}

	public static void sendDirectionalMarkers(EntityPlayerMP player, List<DirectionalGlobalPos> positions, int color, int lifetime) {
		PacketHandler.INSTANCE.sendTo(new PacketDirectionalMarker(positions, color, lifetime), player);
	}
}
