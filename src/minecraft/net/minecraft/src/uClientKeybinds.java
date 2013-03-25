package net.minecraft.src;

import org.lwjgl.input.Keyboard;

public class uClientKeybinds {
public static void keybinds()
{
	if (Keyboard.getEventKey() == Keyboard.KEY_J) {
	    uClientToggles.waterbounce = !uClientToggles.waterbounce;
	    		
	}
	if (Keyboard.getEventKey() == Keyboard.KEY_R) {
	    uClientToggles.fly = !uClientToggles.fly;
	    		
	}
}
}
