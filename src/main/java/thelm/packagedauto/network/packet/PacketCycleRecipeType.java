package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import thelm.packagedauto.container.ContainerEncoder;

public class PacketCycleRecipeType implements IMessage {

	private boolean reverse;

	public PacketCycleRecipeType() {}

	public PacketCycleRecipeType(boolean reverse) {
		this.reverse = reverse;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(reverse);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		reverse = buf.readBoolean();
	}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerEncoder) {
			ContainerEncoder container = (ContainerEncoder)player.openContainer;
			container.patternInventory.cycleRecipeType(reverse);
			container.setupSlots();
		}
		return null;
	}
}
