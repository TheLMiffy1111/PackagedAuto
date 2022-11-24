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

public class SetItemStackPacket {

	private short containerSlot;
	private ItemStack stack;

	public SetItemStackPacket(short containerSlot, ItemStack stack) {
		this.containerSlot = containerSlot;
		this.stack = stack;
	}

	public static void encode(SetItemStackPacket pkt, FriendlyByteBuf buf) {
		buf.writeShort(pkt.containerSlot);
		buf.writeItem(pkt.stack);
	}

	public static SetItemStackPacket decode(FriendlyByteBuf buf) {
		return new SetItemStackPacket(buf.readShort(), buf.readItem());
	}

	public static void handle(SetItemStackPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			AbstractContainerMenu container = player.containerMenu;
			if(container != null) {
				if(pkt.containerSlot >= 0 && pkt.containerSlot < container.slots.size()) {
					Slot slot = container.getSlot(pkt.containerSlot);
					if(slot instanceof FalseCopySlot fSlot) {
						ItemStackHandler handler = (ItemStackHandler)fSlot.getItemHandler();
						handler.setStackInSlot(slot.getSlotIndex(), pkt.stack);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
