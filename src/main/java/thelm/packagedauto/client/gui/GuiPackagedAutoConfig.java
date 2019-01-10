package thelm.packagedauto.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import thelm.packagedauto.config.PackagedAutoConfig;

public class GuiPackagedAutoConfig extends GuiConfig {

	public GuiPackagedAutoConfig(GuiScreen parent) {
		super(parent, getConfigElements(), "packagedauto", false, false, getAbridgedConfigPath(PackagedAutoConfig.config.toString()));
	}

	private static List<IConfigElement> getConfigElements() {
		ArrayList<IConfigElement> list = new ArrayList<>();
		for(String category : PackagedAutoConfig.config.getCategoryNames()) {
			list.add(new ConfigElement(PackagedAutoConfig.config.getCategory(category)));
		}
		return list;
	}
}
