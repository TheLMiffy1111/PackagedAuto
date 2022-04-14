package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import thelm.packagedauto.menu.FluidPackageFillerMenu;

public class SetFluidAmountPacket {

	private int amount;

	public SetFluidAmountPacket(int amount) {
		this.amount = amount;
	}

	public static void encode(SetFluidAmountPacket pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.amount);
	}

	public static SetFluidAmountPacket decode(FriendlyByteBuf buf) {
		return new SetFluidAmountPacket(buf.readInt());
	}

	public static void handle(SetFluidAmountPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(()->{
			if(player.containerMenu instanceof FluidPackageFillerMenu menu) {
				menu.blockEntity.requiredAmount = pkt.amount;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
