package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerPackager;
import thelm.packagedauto.container.ContainerPackagerExtension;

public class PacketChangePackaging implements IMessage {

	public PacketChangePackaging() {}

	@Override
	public void toBytes(ByteBuf buf) {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerPackager) {
			ContainerPackager container = (ContainerPackager)player.openContainer;
			container.tile.changePackagingMode();
		}
		if(player.openContainer instanceof ContainerPackagerExtension) {
			ContainerPackagerExtension container = (ContainerPackagerExtension)player.openContainer;
			container.tile.changePackagingMode();
		}
		return null;
	}
}
