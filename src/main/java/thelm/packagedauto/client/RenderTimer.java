package thelm.packagedauto.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class RenderTimer {

	public static final RenderTimer INSTANCE = new RenderTimer();

	private Minecraft mc;
	private int ticks;

	private RenderTimer() {
		mc = Minecraft.getInstance();
		MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
	}

	public int getTicks() {
		return ticks;
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.START || mc.level == null || mc.player == null || mc.isPaused()) {
			return;
		}
		ticks = (ticks+1) & 0x1FFFFF;
	}
}
