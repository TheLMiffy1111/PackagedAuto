package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerEncoder;

public class PacketSetPatternIndex implements IMessage {

	private int index;

	public PacketSetPatternIndex() {}

	public PacketSetPatternIndex(int index) {
		this.index = index;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(index);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		index = buf.readByte();
	}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerEncoder) {
			ContainerEncoder container = (ContainerEncoder)player.openContainer;
			container.tile.setPatternIndex(index);
			container.setupSlots();
		}
		return null;
	}
}
