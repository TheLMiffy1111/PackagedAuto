package thelm.packagedauto.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

public class RenderTimer {

	public static final RenderTimer INSTANCE = new RenderTimer();

	private Minecraft mc;
	private int ticks;

	private RenderTimer() {
		mc = Minecraft.getMinecraft();
		FMLCommonHandler.instance().bus().register(this);
	}

	public int getTick() {
		return ticks;
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) {
			return;
		}
		ticks = (ticks+1) & 0x1FFFFF;
	}
}
