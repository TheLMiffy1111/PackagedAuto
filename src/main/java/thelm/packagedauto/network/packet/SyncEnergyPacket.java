package thelm.packagedauto.network.packet;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.tile.BaseTile;

public class SyncEnergyPacket {

	private BlockPos pos;
	private int energy;

	public SyncEnergyPacket(BlockPos pos, int energy) {
		this.pos = pos;
		this.energy = energy;
	}

	public static void encode(SyncEnergyPacket pkt, PacketBuffer buf) {
		buf.writeBlockPos(pkt.pos);
		buf.writeInt(pkt.energy);
	}

	public static SyncEnergyPacket decode(PacketBuffer buf) {
		return new SyncEnergyPacket(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(SyncEnergyPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			ClientWorld world = Minecraft.getInstance().world;
			if(world.isBlockLoaded(pkt.pos)) {
				TileEntity te = world.getTileEntity(pkt.pos);
				if(te instanceof BaseTile) {
					((BaseTile)te).getEnergyStorage().setEnergyStored(pkt.energy);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void syncEnergy(BlockPos pos, int energy, RegistryKey<World> dimension, double range) {
		PacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(()->new TargetPoint(pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, range, dimension)), new SyncEnergyPacket(pos, energy));
	}
}
