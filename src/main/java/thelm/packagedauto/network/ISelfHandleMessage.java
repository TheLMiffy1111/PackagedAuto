package thelm.packagedauto.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface ISelfHandleMessage<REPLY extends IMessage> extends IMessage {

	REPLY onMessage(MessageContext ctx);
}
