package thelm.packagedauto.packet;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SetRecipePacket(Int2ObjectMap<ItemStack> map) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:set_recipe");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(map.size());
		for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
			buf.writeByte(entry.getIntKey());
			buf.writeItem(entry.getValue());
		}
	}

	public static SetRecipePacket read(FriendlyByteBuf buf) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int size = buf.readByte();
		for(int i = 0; i < size; ++i) {
			int index = buf.readByte();
			ItemStack stack = buf.readItem();
			map.put(index, stack);
		}
		return new SetRecipePacket(map);
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.patternItemHandler.setRecipe(map);
				}
			});
		}
	}
}
