package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;

public class SetRecipePacket {

	private Int2ObjectMap<ItemStack> map;

	public SetRecipePacket(Int2ObjectMap<ItemStack> map) {
		this.map = map;
	}

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	public static void encode(SetRecipePacket pkt, PacketBuffer buf) {
		buf.writeByte(pkt.map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : pkt.map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			buf.writeItemStack(entry.getValue());
		}
	}

	public static SetRecipePacket decode(PacketBuffer buf) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int size = buf.readByte();
		for(int i = 0; i < size; ++i) {
			int index = buf.readByte();
			ItemStack stack = buf.readItemStack();
			map.put(index, stack);
		}
		return new SetRecipePacket(map);
	}

	public static void handle(SetRecipePacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.openContainer instanceof EncoderContainer) {
				if(player.openContainer instanceof EncoderContainer) {
					EncoderContainer container = (EncoderContainer)player.openContainer;
					container.patternItemHandler.setRecipe(pkt.map);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
