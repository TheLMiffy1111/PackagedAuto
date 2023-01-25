package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerUnpackager;

public class PacketChangeBlocking implements IMessage {

	public PacketChangeBlocking() {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerUnpackager) {
			ContainerUnpackager container = (ContainerUnpackager)player.openContainer;
			container.tile.changeBlockingMode();
		}
		return null;
	}
}
