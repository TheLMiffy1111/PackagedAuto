package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerEncoder;

public class PacketSaveRecipeList implements IMessage {

	private boolean single;

	public PacketSaveRecipeList() {}

	public PacketSaveRecipeList(boolean single) {
		this.single = single;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(single);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		single = buf.readBoolean();
	}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerEncoder) {
			ContainerEncoder container = (ContainerEncoder)player.openContainer;
			container.tile.saveRecipeList(single);
		}
		return null;
	}
}
