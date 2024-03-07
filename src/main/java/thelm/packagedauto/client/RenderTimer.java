package thelm.packagedauto.client;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

public class RenderTimer {

	public static final RenderTimer INSTANCE = new RenderTimer();

	private Minecraft mc;
	private int ticks;

	private RenderTimer() {
		mc = Minecraft.getInstance();
		NeoForge.EVENT_BUS.register(this);
	}

	public int getTicks() {
		return ticks;
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.START || mc.level == null || mc.player == null || mc.isPaused()) {
			return;
		}
		ticks = (ticks+1) & 0x1FFFFF;
	}
}
