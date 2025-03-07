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
import thelm.packagedauto.util.MiscHelper;

public class SetItemStackPacket {

	private final short containerSlot;
	private final ItemStack stack;

	public SetItemStackPacket(short containerSlot, ItemStack stack) {
		this.containerSlot = containerSlot;
		this.stack = stack;
	}

	public void encode(PacketBuffer buf) {
		buf.writeShort(containerSlot);
		MiscHelper.INSTANCE.writeItemWithLargeCount(buf, stack);
	}

	public static SetItemStackPacket decode(PacketBuffer buf) {
		return new SetItemStackPacket(buf.readShort(), MiscHelper.INSTANCE.readItemWithLargeCount(buf));
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			Container container = player.containerMenu;
			if(container != null) {
				if(containerSlot >= 0 && containerSlot < container.slots.size()) {
					Slot slot = container.getSlot(containerSlot);
					if(slot instanceof FalseCopySlot) {
						ItemStackHandler handler = (ItemStackHandler)((FalseCopySlot)slot).getItemHandler();
						handler.setStackInSlot(slot.getSlotIndex(), stack);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
