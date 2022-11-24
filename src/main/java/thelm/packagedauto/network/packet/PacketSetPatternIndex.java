package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketSetPatternIndex implements ISelfHandleMessage<IMessage> {

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

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerEncoder) {
				ContainerEncoder container = (ContainerEncoder)player.openContainer;
				container.tile.setPatternIndex(index);
				container.setupSlots();
			}
		});
		return null;
	}
}
