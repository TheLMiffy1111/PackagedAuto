package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerEncoder;

public class PacketLoadRecipeList implements IMessage {

	public PacketLoadRecipeList() {}

	@Override
	public void toBytes(ByteBuf buf) {}

	@Override
	public void fromBytes(ByteBuf buf) {}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerEncoder) {
			ContainerEncoder container = (ContainerEncoder)player.openContainer;
			container.tile.loadRecipeList();
		}
		return null;
	}
}
