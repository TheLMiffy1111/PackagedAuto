package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.util.MiscHelper;

public class SetRecipePacket {

	private final Int2ObjectMap<ItemStack> map;

	public SetRecipePacket(Int2ObjectMap<ItemStack> map) {
		this.map = map;
	}

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	public void encode(PacketBuffer buf) {
		buf.writeByte(map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			MiscHelper.INSTANCE.writeItemWithLargeCount(buf, entry.getValue());
		}
	}

	public static SetRecipePacket decode(PacketBuffer buf) {
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
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof EncoderContainer) {
				if(player.containerMenu instanceof EncoderContainer) {
					EncoderContainer container = (EncoderContainer)player.containerMenu;
					container.patternItemHandler.setRecipe(map);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
