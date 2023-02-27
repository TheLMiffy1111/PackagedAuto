package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.slot.FalseCopySlot;

public class SetItemStackPacket {

	private short containerSlot;
	private ItemStack stack;

	public SetItemStackPacket(short containerSlot, ItemStack stack) {
		this.containerSlot = containerSlot;
		this.stack = stack;
	}

	public static void encode(SetItemStackPacket pkt, PacketBuffer buf) {
		buf.writeShort(pkt.containerSlot);
		buf.writeItem(pkt.stack);
	}

	public static SetItemStackPacket decode(PacketBuffer buf) {
		return new SetItemStackPacket(buf.readShort(), buf.readItem());
	}

	public static void handle(SetItemStackPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			Container container = player.containerMenu;
			if(container != null) {
				if(pkt.containerSlot >= 0 && pkt.containerSlot < container.slots.size()) {
					Slot slot = container.getSlot(pkt.containerSlot);
					if(slot instanceof FalseCopySlot) {
						ItemStackHandler handler = (ItemStackHandler)((FalseCopySlot)slot).getItemHandler();
						handler.setStackInSlot(slot.getSlotIndex(), pkt.stack);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
