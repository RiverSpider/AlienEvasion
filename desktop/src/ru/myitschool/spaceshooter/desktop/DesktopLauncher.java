package ru.myitschool.spaceshooter.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.myitschool.spaceshooter.SpaceShooter;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new SpaceShooter(), config);
		config.width = SpaceShooter.SCR_WIDTH;
		config.height = SpaceShooter.SCR_HEIGHT;
	}
}
