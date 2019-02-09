package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketChangeBlocking implements ISelfHandleMessage<IMessage> {

	public PacketChangeBlocking() {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerUnpackager) {
				ContainerUnpackager container = (ContainerUnpackager)player.openContainer;
				container.tile.changeBlockingMode();
			}
		});
		return null;
	}
}
