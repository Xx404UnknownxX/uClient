package net.minecraft.src;
 
import net.minecraft.client.Minecraft;
 
public class GuiCustomButton extends GuiButton {
 
    public GuiCustomButton(int i, int j, int k, String s)
    {
        this(i, j, k, 120, 20, s);
    }
 
    public GuiCustomButton(int i, int j, int k, int l, int i1, String s)
    {
        super(i, j, k, l, i1, s);
    }
 
    protected int getHoverState(boolean flag)
    {
        byte byte0 = 1;
        if (!enabled)
        {
                byte0 = 0;
                }
        else if (flag)
        {
                byte0 = 2;
        }
        return byte0;
        }
        public long delay = 200L;
        long var1 = 200;
        long var2;
        long var3;
       
 
    public void onUpdate()
    {
    var2 = System.currentTimeMillis(); //Gets the current time.
    }
    public void drawButton(Minecraft mc, int mx, int my)
    {
        FontRenderer fontrenderer = mc.fontRenderer;
        boolean flag = mx >= xPosition && my >= yPosition && mx < xPosition + width && my < yPosition + height;  //Flag, tells if your mouse is hovering the button
        if (flag)
        { // Hovering
                drawBorderedRect(xPosition, yPosition, xPosition + width, yPosition + height, 0, 0x77808080, 0x77808080);
                drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xFF008000);
                System.currentTimeMillis();
        }
        else { // Normal
        	drawBorderedRect(xPosition, yPosition, xPosition + width, yPosition + height, 0, 0x77808080, 0x77808080);
                drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xFF00FF00);
        }
    }
        public EnumOptions returnEnumOptions()
    {
        return this.returnEnumOptions();
    }
}