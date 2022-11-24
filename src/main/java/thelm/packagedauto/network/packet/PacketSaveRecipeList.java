package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketSaveRecipeList implements ISelfHandleMessage<IMessage> {

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

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerEncoder) {
				ContainerEncoder container = (ContainerEncoder)player.openContainer;
				container.tile.saveRecipeList(single);
			}
		});
		return null;
	}
}
