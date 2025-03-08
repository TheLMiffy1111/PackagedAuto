package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.util.MiscHelper;

public record SetRecipePacket(Int2ObjectMap<ItemStack> map) {

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			MiscHelper.INSTANCE.writeItemWithLargeCount(buf, entry.getValue());
		}
	}

	public static SetRecipePacket decode(FriendlyByteBuf buf) {
		int size = buf.readByte();
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>(size);
		for(int i = 0; i < size; ++i) {
			int index = buf.readUnsignedByte();
			ItemStack stack = MiscHelper.INSTANCE.readItemWithLargeCount(buf);
			map.put(index, stack);
		}
		return new SetRecipePacket(map);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderMenu menu) {
				menu.patternItemHandler.setRecipe(map);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
