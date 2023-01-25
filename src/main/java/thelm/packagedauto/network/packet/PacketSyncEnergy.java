package thelm.packagedauto.network.packet;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.tile.TileBase;

public class PacketSyncEnergy implements IMessage {

	private int x;
	private int y;
	private int z;
	private int energy;

	public PacketSyncEnergy() {}

	public PacketSyncEnergy(int x, int y, int z, int energy) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.energy = energy;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(energy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		energy = buf.readInt();
	}

	public IMessage handle(MessageContext ctx) {
		Minecraft.getMinecraft().func_152344_a(()->{
			WorldClient world = Minecraft.getMinecraft().theWorld;
			if(world.blockExists(x, y, z)) {
				TileEntity te = world.getTileEntity(x, y, z);
				if(te instanceof TileBase) {
					((TileBase)te).getEnergyStorage().setEnergyStored(energy);
				}
			}
		});
		return null;
	}

	public static void syncEnergy(int x, int y, int z, int energy, int dimension, double range) {
		PacketHandler.INSTANCE.sendToAllAround(new PacketSyncEnergy(x, y, z, energy), new TargetPoint(dimension, x+0.5, y+0.5, z+0.5, range));
	}
}
