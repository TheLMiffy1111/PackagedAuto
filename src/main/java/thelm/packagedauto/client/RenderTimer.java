package thelm.packagedauto.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RenderTimer {

	public static final RenderTimer INSTANCE = new RenderTimer();

	private Minecraft mc;
	private int ticks;

	private RenderTimer() {
		mc = Minecraft.getMinecraft();
		MinecraftForge.EVENT_BUS.register(this);
	}

	public int getTicks() {
		return ticks;
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.START || mc.world == null || mc.player == null || mc.isGamePaused()) {
			return;
		}
		ticks = (ticks+1) & 0x1FFFFF;
	}
}
