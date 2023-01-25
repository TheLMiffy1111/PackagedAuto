package thelm.packagedauto.network.packet;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.container.ContainerEncoder;

public class PacketSetRecipe implements IMessage {

	private Map<Integer, ItemStack> map = new HashMap<>();

	public PacketSetRecipe() {}

	public PacketSetRecipe(Map<Integer, ItemStack> map) {
		this.map.putAll(map);
	}

	public PacketSetRecipe addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(map.size());
		for(Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
			buf.writeByte(entry.getKey());
			ByteBufUtils.writeItemStack(buf, entry.getValue());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		map.clear();
		int size = buf.readByte();
		for(int i = 0; i < size; ++i) {
			int index = buf.readByte();
			ItemStack stack = ByteBufUtils.readItemStack(buf);
			map.put(index, stack);
		}
	}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		if(player.openContainer instanceof ContainerEncoder) {
			ContainerEncoder container = (ContainerEncoder)player.openContainer;
			container.patternInventory.setRecipe(map);
		}
		return null;
	}
}
