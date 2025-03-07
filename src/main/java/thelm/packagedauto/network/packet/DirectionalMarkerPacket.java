package thelm.packagedauto.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.network.PacketHandler;

public record DirectionalMarkerPacket(List<DirectionalGlobalPos> positions, int color, int lifetime) {

	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(positions.size());
		for(DirectionalGlobalPos globalPos : positions) {
			buf.writeResourceLocation(globalPos.dimension().location());
			buf.writeInt(globalPos.x());
			buf.writeInt(globalPos.y());
			buf.writeInt(globalPos.z());
			buf.writeByte(globalPos.direction().get3DDataValue());
		}
		buf.writeMedium(color);
		buf.writeShort(lifetime);
	}

	public static DirectionalMarkerPacket decode(FriendlyByteBuf buf) {
		int size = buf.readByte();
		List<DirectionalGlobalPos> positions = new ArrayList<>(size);
		for(int i = 0; i < size; ++i) {
			ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
			BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			Direction direction = Direction.from3DDataValue(buf.readByte());
			positions.add(new DirectionalGlobalPos(dimension, pos, direction));
		}
		return new DirectionalMarkerPacket(positions, buf.readUnsignedMedium(), buf.readUnsignedShort());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			WorldOverlayRenderer.INSTANCE.addDirectionalMarkers(positions, color, lifetime);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sendDirectionalMarkers(ServerPlayer player, List<DirectionalGlobalPos> positions, int color, int lifetime) {
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new DirectionalMarkerPacket(positions, color, lifetime));
	}
}
