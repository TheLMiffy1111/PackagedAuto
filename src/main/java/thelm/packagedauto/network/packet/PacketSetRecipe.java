package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketSetRecipe implements ISelfHandleMessage<IMessage> {

	private Int2ObjectMap<ItemStack> map;

	public PacketSetRecipe() {}

	public PacketSetRecipe(Int2ObjectMap<ItemStack> map) {
		this.map = map;
	}

	public PacketSetRecipe addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			MiscUtil.writeItemWithLargeCount(buf, entry.getValue());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int size = buf.readByte();
		map = new Int2ObjectOpenHashMap<>(size);
		for(int i = 0; i < size; ++i) {
			int index = buf.readByte();
			ItemStack stack = MiscUtil.readItemWithLargeCount(buf);
			map.put(index, stack);
		}
	}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerEncoder) {
				ContainerEncoder container = (ContainerEncoder)player.openContainer;
				container.patternInventory.setRecipe(map);
			}
		});
		return null;
	}
}
