package thelm.packagedauto.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class RenderTimer {

	public static final RenderTimer INSTANCE = new RenderTimer();

	private Minecraft mc;
	private int ticks;

	private RenderTimer() {
		mc = Minecraft.getInstance();
		NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
	}

	public int getTicks() {
		return ticks;
	}

	public void onClientTickPre(ClientTickEvent.Pre event) {
		if(mc.level == null || mc.player == null || mc.isPaused()) {
			return;
		}
		ticks = (ticks+1) & 0x1FFFFF;
	}
}
