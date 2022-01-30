package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;

public class SetRecipePacket {

	private Int2ObjectMap<ItemStack> map;

	public SetRecipePacket(Int2ObjectMap<ItemStack> map) {
		this.map = map;
	}

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	public static void encode(SetRecipePacket pkt, FriendlyByteBuf buf) {
		buf.writeByte(pkt.map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : pkt.map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			buf.writeItem(entry.getValue());
		}
	}

	public static SetRecipePacket decode(FriendlyByteBuf buf) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int size = buf.readByte();
		for(int i = 0; i < size; ++i) {
			int index = buf.readByte();
			ItemStack stack = buf.readItem();
			map.put(index, stack);
		}
		return new SetRecipePacket(map);
	}

	public static void handle(SetRecipePacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.patternItemHandler.setRecipe(pkt.map);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
