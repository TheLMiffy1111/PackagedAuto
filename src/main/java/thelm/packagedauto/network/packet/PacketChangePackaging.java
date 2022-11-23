package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerPackager;
import thelm.packagedauto.container.ContainerPackagerExtension;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketChangePackaging implements ISelfHandleMessage<IMessage> {

	public PacketChangePackaging() {}

	@Override
	public void toBytes(ByteBuf buf) {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerPackager) {
				ContainerPackager container = (ContainerPackager)player.openContainer;
				container.tile.changePackagingMode();
			}
			if(player.openContainer instanceof ContainerPackagerExtension) {
				ContainerPackagerExtension container = (ContainerPackagerExtension)player.openContainer;
				container.tile.changePackagingMode();
			}
		});
		return null;
	}
}
