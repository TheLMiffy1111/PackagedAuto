package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.network.PacketHandler;

public class SyncEnergyPacket {

	private BlockPos pos;
	private int energy;

	public SyncEnergyPacket(BlockPos pos, int energy) {
		this.pos = pos;
		this.energy = energy;
	}

	public static void encode(SyncEnergyPacket pkt, FriendlyByteBuf buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeInt(pkt.energy);
	}

	public static SyncEnergyPacket decode(FriendlyByteBuf buf) {
		return new SyncEnergyPacket(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(SyncEnergyPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			ClientLevel level = Minecraft.getInstance().level;
			if(level.isLoaded(pkt.pos)) {
				BlockEntity be = level.getBlockEntity(pkt.pos);
				if(be instanceof BaseBlockEntity bbe) {
					bbe.getEnergyStorage().setEnergyStored(pkt.energy);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void syncEnergy(BlockPos pos, int energy, ResourceKey<Level> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, range, dimension)), new SyncEnergyPacket(pos, energy));
	}
}
