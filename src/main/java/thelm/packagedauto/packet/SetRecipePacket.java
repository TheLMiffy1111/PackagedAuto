package thelm.packagedauto.packet;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record SetRecipePacket(Int2ObjectMap<ItemStack> map) implements CustomPacketPayload {

	public static final Type<SetRecipePacket> TYPE = new Type<>(ResourceLocation.parse("packagedauto:set_recipe"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetRecipePacket> STREAM_CODEC = ByteBufCodecs.
			map(SetRecipePacket::createMap, PacketStreamCodecs.UNSIGNED_BYTE, ItemStack.OPTIONAL_STREAM_CODEC).
			map(SetRecipePacket::new, SetRecipePacket::map);

	@Override
	public Type<SetRecipePacket> type() {
		return TYPE;
	}

	public SetRecipePacket addItem(int index, ItemStack stack) {
		map.put(index, stack);
		return this;
	}

	public static Int2ObjectMap<ItemStack> createMap(int capacity) {
		return new Int2ObjectArrayMap<>(capacity);
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.patternItemHandler.setRecipe(map);
				}
			});
		}
	}
}
