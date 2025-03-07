package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketLoadRecipeList implements ISelfHandleMessage<IMessage> {

	private boolean single;
	private boolean clear;

	public PacketLoadRecipeList() {}

	public PacketLoadRecipeList(boolean single, boolean clear) {
		this.single = single;
		this.clear = clear;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(single);
		buf.writeBoolean(clear);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		single = buf.readBoolean();
		clear = buf.readBoolean();
	}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerEncoder) {
				ContainerEncoder container = (ContainerEncoder)player.openContainer;
				container.tile.loadRecipeList(single, clear);
			}
		});
		return null;
	}
}
