package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class TextureMap implements IconRegister
{
    /** 0 = terrain.png, 1 = items.png */
    public final int textureType;
    public final String textureName;
    public final String basePath;
    public final String textureExt;
    private final HashMap mapTexturesStiched = new HashMap();
    private BufferedImage missingImage = new BufferedImage(64, 64, 2);
    private TextureStitched missingTextureStiched;
    private Texture atlasTexture;
    private final List listTextureStiched = new ArrayList();
    private final Map textureStichedMap = new HashMap();

    public TextureMap(int par1, String par2, String par3Str, BufferedImage par4BufferedImage)
    {
        this.textureType = par1;
        this.textureName = par2;
        this.basePath = par3Str;
        this.textureExt = ".png";
        this.missingImage = par4BufferedImage;
    }

    public void refreshTextures()
    {
        this.textureStichedMap.clear();
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPre, new Object[] {this});
        int var1;
        int var2;

        if (this.textureType == 0)
        {
            Block[] var3 = Block.blocksList;
            var1 = var3.length;

            for (var2 = 0; var2 < var1; ++var2)
            {
                Block var4 = var3[var2];

                if (var4 != null)
                {
                    var4.registerIcons(this);
                }
            }

            Minecraft.getMinecraft().renderGlobal.registerDestroyBlockIcons(this);
            RenderManager.instance.updateIcons(this);
            ConnectedTextures.updateIcons(this);
            NaturalTextures.updateIcons(this);
        }

        Item[] var24 = Item.itemsList;
        var1 = var24.length;

        for (var2 = 0; var2 < var1; ++var2)
        {
            Item var25 = var24[var2];

            if (var25 != null && var25.getSpriteNumber() == this.textureType)
            {
                var25.updateIcons(this);
            }
        }

        HashMap var26 = new HashMap();
        Stitcher var5 = TextureManager.instance().createStitcher(this.textureName);
        this.mapTexturesStiched.clear();
        this.listTextureStiched.clear();
        Texture var6 = TextureManager.instance().makeTexture("missingno", 2, this.missingImage.getWidth(), this.missingImage.getHeight(), 10496, 6408, 9728, 9728, false, this.missingImage);
        StitchHolder var7 = new StitchHolder(var6);
        var5.addStitchHolder(var7);
        var26.put(var7, Arrays.asList(new Texture[] {var6}));
        Iterator var8 = this.textureStichedMap.keySet().iterator();
        ArrayList var9 = new ArrayList();
        List var12;

        while (var8.hasNext())
        {
            String var10 = (String)var8.next();
            String var11 = this.makeFullTextureName(var10) + this.textureExt;
            var12 = TextureManager.instance().createNewTexture(var10, var11, (TextureStitched)null);
            var9.add(var12);
        }

        int var28 = this.getStandardTileSize(var9);
        Config.dbg("Tile size for " + this.textureName + ": " + var28);
        Iterator var27 = var9.iterator();

        while (var27.hasNext())
        {
            var12 = (List)var27.next();

            if (!var12.isEmpty())
            {
                this.scaleTextures(var12, var28);
            }
        }

        var27 = var9.iterator();

        while (var27.hasNext())
        {
            var12 = (List)var27.next();

            if (!var12.isEmpty())
            {
                StitchHolder var13 = new StitchHolder((Texture)var12.get(0));
                var5.addStitchHolder(var13);
                var26.put(var13, var12);
            }
        }

        try
        {
            var5.doStitch();
        }
        catch (StitcherException var23)
        {
            throw var23;
        }

        this.atlasTexture = var5.getTexture();
        this.atlasTexture.updateMaxMipmapLevel(var28);
        var8 = var5.getStichSlots().iterator();

        while (var8.hasNext())
        {
            StitchSlot var30 = (StitchSlot)var8.next();
            StitchHolder var31 = var30.getStitchHolder();
            Texture var32 = var31.func_98150_a();
            String var14 = var32.getTextureName();
            List var15 = (List)var26.get(var31);
            TextureStitched var16 = (TextureStitched)this.textureStichedMap.get(var14);
            boolean var17 = false;

            if (var16 == null)
            {
                var17 = true;
                var16 = TextureStitched.makeTextureStitched(var14);

                if (!var14.equals("missingno"))
                {
                    Minecraft.getMinecraft().getLogAgent().logWarning("Couldn\'t find premade icon for " + var14 + " doing " + this.textureName);
                }
            }

            var16.init(this.atlasTexture, var15, var30.getOriginX(), var30.getOriginY(), var31.func_98150_a().getWidth(), var31.func_98150_a().getHeight(), var31.isRotated());
            this.mapTexturesStiched.put(var14, var16);

            if (!var17)
            {
                this.textureStichedMap.remove(var14);
            }

            if (var15.size() > 1)
            {
                this.listTextureStiched.add(var16);
                String var18 = this.makeFullTextureName(var14) + ".txt";
                ITexturePack var19 = Minecraft.getMinecraft().texturePackList.getSelectedTexturePack();
                boolean var20 = !var19.func_98138_b("/" + this.basePath + var14 + ".png", false);

                try
                {
                    InputStream var21 = var19.func_98137_a("/" + var18, var20);
                    Minecraft.getMinecraft().getLogAgent().logInfo("Found animation info for: " + var18);
                    var16.readAnimationInfo(new BufferedReader(new InputStreamReader(var21)));
                }
                catch (IOException var22)
                {
                    ;
                }
            }
        }

        this.missingTextureStiched = (TextureStitched)this.mapTexturesStiched.get("missingno");
        var8 = this.textureStichedMap.values().iterator();

        while (var8.hasNext())
        {
            TextureStitched var29 = (TextureStitched)var8.next();
            var29.copyFrom(this.missingTextureStiched);
        }

        this.textureStichedMap.putAll(this.mapTexturesStiched);
        this.atlasTexture.writeImage("debug.stitched_" + this.textureName + ".png");
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPost, new Object[] {this});
        this.atlasTexture.createTexture();
    }

    public void updateAnimations()
    {
        if (this.listTextureStiched.size() > 0)
        {
            this.getTexture().bindTexture(0);
            this.atlasTexture.setTextureBound(true);
            Iterator var1 = this.listTextureStiched.iterator();

            while (var1.hasNext())
            {
                TextureStitched var2 = (TextureStitched)var1.next();

                if ((var2 != TextureUtils.iconWater && var2 != TextureUtils.iconWaterFlow || Config.isAnimatedWater()) && (var2 != TextureUtils.iconLava && var2 != TextureUtils.iconLavaFlow || Config.isAnimatedLava()) && (var2 != TextureUtils.iconFire0 && var2 != TextureUtils.iconFire1 || Config.isAnimatedFire()) && (var2 != TextureUtils.iconPortal || Config.isAnimatedPortal()))
                {
                    var2.updateAnimation();
                }
            }

            this.atlasTexture.setTextureBound(false);
        }
    }

    public Texture getTexture()
    {
        return this.atlasTexture;
    }

    public Icon registerIcon(String par1Str)
    {
        if (par1Str == null)
        {
            (new RuntimeException("Don\'t register null!")).printStackTrace();
        }

        TextureStitched var2 = (TextureStitched)this.textureStichedMap.get(par1Str);

        if (var2 == null)
        {
            var2 = TextureStitched.makeTextureStitched(par1Str);
            var2.setIndexInMap(this.textureStichedMap.size());
            this.textureStichedMap.put(par1Str, var2);
        }

        return var2;
    }

    public Icon getMissingIcon()
    {
        return this.missingTextureStiched;
    }

    private String makeFullTextureName(String var1)
    {
        int var2 = var1.indexOf(":");

        if (var2 > 0)
        {
            String var3 = var1.substring(0, var2);
            String var4 = var1.substring(var2 + 1);
            return "mods/" + var3 + "/" + this.basePath + var4;
        }
        else
        {
            return var1.startsWith("ctm/") ? var1 : this.basePath + var1;
        }
    }

    public TextureStitched getIconSafe(String var1)
    {
        return (TextureStitched)this.textureStichedMap.get(var1);
    }

    private int getStandardTileSize(List var1)
    {
        int[] var2 = new int[16];
        Iterator var3 = var1.iterator();
        int var6;

        while (var3.hasNext())
        {
            List var4 = (List)var3.next();

            if (!var4.isEmpty())
            {
                Texture var5 = (Texture)var4.get(0);

                if (var5 != null)
                {
                    var6 = TextureUtils.getPowerOfTwo(var5.getWidth());
                    int var7 = TextureUtils.getPowerOfTwo(var5.getHeight());
                    int var8 = Math.max(var6, var7);

                    if (var8 < var2.length)
                    {
                        ++var2[var8];
                    }
                }
            }
        }

        int var9 = 4;
        int var10 = 0;
        int var11;

        for (var11 = 0; var11 < var2.length; ++var11)
        {
            var6 = var2[var11];

            if (var6 > var10)
            {
                var9 = var11;
                var10 = var6;
            }
        }

        if (var9 < 4)
        {
            var9 = 4;
        }

        var11 = TextureUtils.twoToPower(var9);
        return var11;
    }

    private void scaleTextures(List var1, int var2)
    {
        if (!var1.isEmpty())
        {
            Texture var3 = (Texture)var1.get(0);
            int var4 = Math.max(var3.getWidth(), var3.getHeight());

            if (var4 < var2)
            {
                for (int var5 = 0; var5 < var1.size(); ++var5)
                {
                    Texture var6 = (Texture)var1.get(var5);
                    var6.scaleUp(var2);
                }
            }
        }
    }

    public TextureStitched getTextureExtry(String var1)
    {
        return (TextureStitched)this.textureStichedMap.get(var1);
    }

    public boolean setTextureEntry(String var1, TextureStitched var2)
    {
        if (!this.textureStichedMap.containsKey(var1))
        {
            this.textureStichedMap.put(var1, var2);
            return true;
        }
        else
        {
            return false;
        }
    }
}
