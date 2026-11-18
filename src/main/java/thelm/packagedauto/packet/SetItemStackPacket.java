package thelm.packagedauto.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.slot.FalseCopySlot;

public record SetItemStackPacket(int containerSlot, ItemStack stack) implements CustomPacketPayload {

	public static final Type<SetItemStackPacket> TYPE = new Type<>(Identifier.parse("packagedauto:set_item_stack"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetItemStackPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.UNSIGNED_SHORT, SetItemStackPacket::containerSlot,
			ItemStack.OPTIONAL_STREAM_CODEC, SetItemStackPacket::stack,
			SetItemStackPacket::new);

	@Override
	public Type<SetItemStackPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		if(ctx.player() instanceof ServerPlayer player) {
			ctx.enqueueWork(()->{
				AbstractContainerMenu container = player.containerMenu;
				if(container != null) {
					if(containerSlot >= 0 && containerSlot < container.slots.size()) {
						Slot slot = container.getSlot(containerSlot);
						if(slot instanceof FalseCopySlot fSlot) {
							fSlot.slotModifier.set(slot.getSlotIndex(), ItemResource.of(stack), stack.getCount());
						}
					}
				}
			});
		}
	}
}
