package thelm.packagedauto.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.tile.TileBase;

public class PacketSyncEnergy implements ISelfHandleMessage<IMessage> {

	private long pos;
	private int energy;

	public PacketSyncEnergy() {}

	public PacketSyncEnergy(BlockPos pos, int energy) {
		this.pos = pos.toLong();
		this.energy = energy;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos);
		buf.writeInt(energy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = buf.readLong();
		energy = buf.readInt();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(()->{
			WorldClient world = Minecraft.getMinecraft().world;
			BlockPos pos = BlockPos.fromLong(this.pos);
			if(world.isBlockLoaded(pos)) {
				TileEntity te = world.getTileEntity(pos);
				if(te instanceof TileBase) {
					((TileBase)te).getEnergyStorage().setEnergyStored(energy);
				}
			}
		});
		return null;
	}

	public static void syncEnergy(BlockPos pos, int energy, int dimension, double range) {
		PacketHandler.INSTANCE.sendToAllAround(new PacketSyncEnergy(pos, energy), new TargetPoint(dimension, pos.getX(), pos.getY(), pos.getZ(), range));
	}
}
