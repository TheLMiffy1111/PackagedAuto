package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.slot.SlotFalseCopy;

public class PacketSetItemStack implements ISelfHandleMessage<IMessage> {

	private int containerSlot;
	private ItemStack stack;

	public PacketSetItemStack() {}

	public PacketSetItemStack(int containerSlot, ItemStack stack) {
		this.containerSlot = containerSlot;
		this.stack = stack;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(containerSlot);
		MiscUtil.writeItemWithLargeCount(buf, stack);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		containerSlot = buf.readUnsignedShort();
		stack = MiscUtil.readItemWithLargeCount(buf);
	}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
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
		});
		return null;
	}
}
