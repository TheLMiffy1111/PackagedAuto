package thelm.packagedauto.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class PacketStreamCodecs {

	private PacketStreamCodecs() {}

	public static final StreamCodec<ByteBuf, Integer> UNSIGNED_BYTE = StreamCodec.of(ByteBuf::writeByte, b->(int)b.readUnsignedByte());
	public static final StreamCodec<ByteBuf, Integer> UNSIGNED_MEDIUM = StreamCodec.of(ByteBuf::writeMedium, ByteBuf::readUnsignedMedium);
}
