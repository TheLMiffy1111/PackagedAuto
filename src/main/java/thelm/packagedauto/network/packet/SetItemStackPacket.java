package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.MiscHelper;

public record SetItemStackPacket(int containerSlot, ItemStack stack) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeShort(containerSlot);
		MiscHelper.INSTANCE.writeItemWithLargeCount(buf, stack);
	}

	public static SetItemStackPacket decode(FriendlyByteBuf buf) {
		return new SetItemStackPacket(buf.readUnsignedShort(), MiscHelper.INSTANCE.readItemWithLargeCount(buf));
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
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
		ctx.get().setPacketHandled(true);
	}
}
