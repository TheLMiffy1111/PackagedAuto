package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.slot.SlotFalseCopy;

public class PacketSetItemStack implements IMessage {

	private short containerSlot;
	private ItemStack stack;

	public PacketSetItemStack() {}

	public PacketSetItemStack(short containerSlot, ItemStack stack) {
		this.containerSlot = containerSlot;
		this.stack = stack;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(containerSlot);
		ByteBufUtils.writeItemStack(buf, stack);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		containerSlot = buf.readShort();
		stack = ByteBufUtils.readItemStack(buf);
	}

	public IMessage handle(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		Container container = player.openContainer;
		if(container != null) {
			if(containerSlot >= 0 && containerSlot < container.inventorySlots.size()) {
				Slot slot = container.getSlot(containerSlot);
				if(slot instanceof SlotFalseCopy) {
					IInventory inventory = ((SlotFalseCopy)slot).inventory;
					inventory.setInventorySlotContents(slot.getSlotIndex(), stack);
				}
			}
		}
		return null;
	}
}
