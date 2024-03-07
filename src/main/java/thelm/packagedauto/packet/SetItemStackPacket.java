package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.slot.FalseCopySlot;

public record SetItemStackPacket(short containerSlot, ItemStack stack) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:set_item_stack");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeShort(containerSlot);
		buf.writeItem(stack);
	}

	public static SetItemStackPacket read(FriendlyByteBuf buf) {
		return new SetItemStackPacket(buf.readShort(), buf.readItem());
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				AbstractContainerMenu container = player.containerMenu;
				if(container != null) {
					if(containerSlot >= 0 && containerSlot < container.slots.size()) {
						Slot slot = container.getSlot(containerSlot);
						if(slot instanceof FalseCopySlot fSlot) {
							ItemStackHandler handler = (ItemStackHandler)fSlot.getItemHandler();
							handler.setStackInSlot(slot.getSlotIndex(), stack);
						}
					}
				}
			});
		}
	}
}
