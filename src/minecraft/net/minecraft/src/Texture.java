package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.Dimension;

public class Texture
{
    private int glTextureId;
    private int textureId;
    private int textureType;

    /** Width of this texture in pixels. */
    private int width;

    /** Height of this texture in pixels. */
    private int height;
    private final int textureDepth;
    private final int textureFormat;
    private final int textureTarget;
    private final int textureMinFilter;
    private final int textureMagFilter;
    private final int textureWrap;
    private final boolean mipmapActive;
    private final String textureName;
    private Rect2i textureRect;
    private boolean transferred;
    private boolean autoCreate;
    private boolean textureCreated;
    private ByteBuffer textureData;
    private boolean textureBound;
    public ByteBuffer[] mipmapDatas;
    public Dimension[] mipmapDimensions;

    private Texture(String par1Str, int par2, int par3, int par4, int par5, int par6, int par7, int par8, int par9)
    {
        this.textureName = par1Str;
        this.textureType = par2;
        this.width = par3;
        this.height = par4;
        this.textureDepth = par5;
        this.textureFormat = par7;

        if (Config.isUseMipmaps() && isMipMapTexture(this.textureName))
        {
            par8 = Config.getMipmapType();
        }

        this.textureMinFilter = par8;
        this.textureMagFilter = par9;
        this.textureWrap = par6;
        this.textureRect = new Rect2i(0, 0, par3, par4);

        if (par4 == 1 && par5 == 1)
        {
            this.textureTarget = 3552;
        }
        else if (par5 == 1)
        {
            this.textureTarget = 3553;
        }
        else
        {
            this.textureTarget = 32879;
        }

        this.mipmapActive = par8 != 9728 && par8 != 9729 || par9 != 9728 && par9 != 9729;

        if (par2 != 2)
        {
            this.glTextureId = GL11.glGenTextures();
            GL11.glBindTexture(this.textureTarget, this.glTextureId);
            GL11.glTexParameteri(this.textureTarget, GL11.GL_TEXTURE_MIN_FILTER, par8);
            GL11.glTexParameteri(this.textureTarget, GL11.GL_TEXTURE_MAG_FILTER, par9);

            if (this.mipmapActive)
            {
                this.updateMaxMipmapLevel(16);
            }

            GL11.glTexParameteri(this.textureTarget, GL11.GL_TEXTURE_WRAP_S, par6);
            GL11.glTexParameteri(this.textureTarget, GL11.GL_TEXTURE_WRAP_T, par6);
        }
        else
        {
            this.glTextureId = -1;
        }

        this.textureId = TextureManager.instance().getNextTextureId();
    }

    public Texture(String par1Str, int par2, int par3, int par4, int par5, int par6, int par7, int par8, BufferedImage par9BufferedImage)
    {
        this(par1Str, par2, par3, par4, 1, par5, par6, par7, par8, par9BufferedImage);
    }

    public Texture(String par1Str, int par2, int par3, int par4, int par5, int par6, int par7, int par8, int par9, BufferedImage par10BufferedImage)
    {
        this(par1Str, par2, par3, par4, par5, par6, par7, par8, par9);

        if (par10BufferedImage == null)
        {
            if (par3 != -1 && par4 != -1)
            {
                byte[] var11 = new byte[par3 * par4 * par5 * 4];

                for (int var12 = 0; var12 < var11.length; ++var12)
                {
                    var11[var12] = 0;
                }

                this.textureData = GLAllocation.createDirectByteBuffer(var11.length);
                this.textureData.clear();
                this.textureData.put(var11);
                this.textureData.position(0).limit(var11.length);

                if (this.autoCreate)
                {
                    this.createTexture();
                }
                else
                {
                    this.textureCreated = false;
                }
            }
            else
            {
                this.transferred = false;
            }
        }
        else
        {
            this.transferred = true;
            this.transferFromImage(par10BufferedImage);

            if (par2 != 2)
            {
                this.createTexture();
                this.autoCreate = false;
            }
        }
    }

    public final Rect2i getTextureRect()
    {
        return this.textureRect;
    }

    public void fillRect(Rect2i par1Rect2i, int par2)
    {
        if (this.textureTarget != 32879)
        {
            Rect2i var3 = new Rect2i(0, 0, this.width, this.height);
            var3.intersection(par1Rect2i);
            this.textureData.position(0);

            for (int var4 = var3.getRectY(); var4 < var3.getRectY() + var3.getRectHeight(); ++var4)
            {
                int var5 = var4 * this.width * 4;

                for (int var6 = var3.getRectX(); var6 < var3.getRectX() + var3.getRectWidth(); ++var6)
                {
                    this.textureData.put(var5 + var6 * 4 + 0, (byte)(par2 >> 24 & 255));
                    this.textureData.put(var5 + var6 * 4 + 1, (byte)(par2 >> 16 & 255));
                    this.textureData.put(var5 + var6 * 4 + 2, (byte)(par2 >> 8 & 255));
                    this.textureData.put(var5 + var6 * 4 + 3, (byte)(par2 >> 0 & 255));
                }
            }

            if (par1Rect2i.getRectX() == 0 && par1Rect2i.getRectY() == 0 && par1Rect2i.getRectWidth() == this.width && par1Rect2i.getRectHeight() == this.height)
            {
                this.textureCreated = false;
            }

            if (this.autoCreate)
            {
                this.createTexture();
            }
            else
            {
                this.textureCreated = false;
            }
        }
    }

    public void writeImage(String par1Str)
    {
        BufferedImage var2 = new BufferedImage(this.width, this.height, 2);
        ByteBuffer var3 = this.getTextureData();
        byte[] var4 = new byte[this.width * this.height * 4];
        var3.position(0);
        var3.get(var4);

        for (int var5 = 0; var5 < this.width; ++var5)
        {
            for (int var6 = 0; var6 < this.height; ++var6)
            {
                int var7 = var6 * this.width * 4 + var5 * 4;
                byte var8 = 0;
                int var9 = var8 | (var4[var7 + 2] & 255) << 0;
                var9 |= (var4[var7 + 1] & 255) << 8;
                var9 |= (var4[var7 + 0] & 255) << 16;
                var9 |= (var4[var7 + 3] & 255) << 24;
                var2.setRGB(var5, var6, var9);
            }
        }

        this.textureData.position(this.width * this.height * 4);

        try
        {
            ImageIO.write(var2, "png", new File(Minecraft.getMinecraftDir(), par1Str));
        }
        catch (IOException var10)
        {
            var10.printStackTrace();
        }
    }

    public void copyFrom(int par1, int par2, Texture par3Texture, boolean par4)
    {
        if (this.textureTarget != 32879)
        {
            ByteBuffer var5;

            if (this.textureCreated)
            {
                if (!this.textureBound)
                {
                    return;
                }

                var5 = par3Texture.getTextureData();
                var5.position(0);
                GL11.glTexSubImage2D(this.textureTarget, 0, par1, par2, par3Texture.getWidth(), par3Texture.getHeight(), this.textureFormat, GL11.GL_UNSIGNED_BYTE, var5);

                if (this.mipmapActive)
                {
                    if (par3Texture.mipmapDatas == null)
                    {
                        par3Texture.generateMipMapData();
                    }

                    ByteBuffer[] var13 = par3Texture.mipmapDatas;
                    Dimension[] var14 = par3Texture.mipmapDimensions;

                    if (var13 != null && var14 != null)
                    {
                        this.registerMipMapsSub(par1, par2, var13, var14);
                    }
                }

                return;
            }

            var5 = par3Texture.getTextureData();
            this.textureData.position(0);
            var5.position(0);

            for (int var6 = 0; var6 < par3Texture.getHeight(); ++var6)
            {
                int var7 = par2 + var6;
                int var8 = var6 * par3Texture.getWidth() * 4;
                int var9 = var7 * this.width * 4;

                if (par4)
                {
                    var7 = par2 + (par3Texture.getHeight() - var6);
                }

                for (int var10 = 0; var10 < par3Texture.getWidth(); ++var10)
                {
                    int var11 = var9 + (var10 + par1) * 4;
                    int var12 = var8 + var10 * 4;

                    if (par4)
                    {
                        var11 = par1 + var10 * this.width * 4 + var7 * 4;
                    }

                    this.textureData.put(var11 + 0, var5.get(var12 + 0));
                    this.textureData.put(var11 + 1, var5.get(var12 + 1));
                    this.textureData.put(var11 + 2, var5.get(var12 + 2));
                    this.textureData.put(var11 + 3, var5.get(var12 + 3));
                }
            }

            this.textureData.position(this.width * this.height * 4);

            if (this.autoCreate)
            {
                this.createTexture();
            }
            else
            {
                this.textureCreated = false;
            }
        }
    }

    public void transferFromImage(BufferedImage par1BufferedImage)
    {
        if (this.textureTarget != 32879)
        {
            int var2 = par1BufferedImage.getWidth();
            int var3 = par1BufferedImage.getHeight();

            if (var2 <= this.width && var3 <= this.height)
            {
                int[] var4 = new int[] {3, 0, 1, 2};
                int[] var5 = new int[] {3, 2, 1, 0};
                int[] var6 = this.textureFormat == 32993 ? var5 : var4;
                int[] var7 = new int[this.width * this.height];
                int var8 = par1BufferedImage.getTransparency();
                par1BufferedImage.getRGB(0, 0, this.width, this.height, var7, 0, var2);
                byte[] var9 = new byte[this.width * this.height * 4];

                for (int var10 = 0; var10 < this.height; ++var10)
                {
                    for (int var11 = 0; var11 < this.width; ++var11)
                    {
                        int var12 = var10 * this.width + var11;
                        int var13 = var12 * 4;
                        var9[var13 + var6[0]] = (byte)(var7[var12] >> 24 & 255);
                        var9[var13 + var6[1]] = (byte)(var7[var12] >> 16 & 255);
                        var9[var13 + var6[2]] = (byte)(var7[var12] >> 8 & 255);
                        var9[var13 + var6[3]] = (byte)(var7[var12] >> 0 & 255);
                    }
                }

                this.textureData = GLAllocation.createDirectByteBuffer(var9.length);
                this.textureData.clear();
                this.textureData.put(var9);
                this.textureData.limit(var9.length);

                if (this.autoCreate)
                {
                    this.createTexture();
                }
                else
                {
                    this.textureCreated = false;
                }
            }
            else
            {
                Minecraft.getMinecraft().getLogAgent().logWarning("transferFromImage called with a BufferedImage with dimensions (" + var2 + ", " + var3 + ") larger than the Texture dimensions (" + this.width + ", " + this.height + "). Ignoring.");
            }
        }
    }

    public int getTextureId()
    {
        return this.textureId;
    }

    public int getGlTextureId()
    {
        return this.glTextureId;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public String getTextureName()
    {
        return this.textureName;
    }

    public void bindTexture(int par1)
    {
        if (this.textureDepth == 1)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        else
        {
            GL11.glEnable(GL12.GL_TEXTURE_3D);
        }

        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + par1);
        GL11.glBindTexture(this.textureTarget, this.glTextureId);

        if (!this.textureCreated)
        {
            this.createTexture();
        }
    }

    public void createTexture()
    {
        this.textureData.clear();

        if (this.height != 1 && this.textureDepth != 1)
        {
            GL12.glTexImage3D(this.textureTarget, 0, this.textureFormat, this.width, this.height, this.textureDepth, 0, this.textureFormat, GL11.GL_UNSIGNED_BYTE, this.textureData);
        }
        else if (this.height != 1)
        {
            GL11.glTexImage2D(this.textureTarget, 0, this.textureFormat, this.width, this.height, 0, this.textureFormat, GL11.GL_UNSIGNED_BYTE, this.textureData);

            if (this.mipmapActive)
            {
                this.generateMipMaps(true);
            }
        }
        else
        {
            GL11.glTexImage1D(this.textureTarget, 0, this.textureFormat, this.width, 0, this.textureFormat, GL11.GL_UNSIGNED_BYTE, this.textureData);
        }

        this.textureCreated = true;
    }

    public ByteBuffer getTextureData()
    {
        return this.textureData;
    }

    public void generateMipMapData()
    {
        this.generateMipMaps(false);
    }

    private void generateMipMaps(boolean var1)
    {
        if (this.mipmapDatas == null)
        {
            this.allocateMipmapDatas(this.width, this.height);
        }

        ByteBuffer var2 = this.textureData;
        int var3 = this.width;
        boolean var4 = true;

        for (int var5 = 0; var5 < this.mipmapDatas.length; ++var5)
        {
            ByteBuffer var6 = this.mipmapDatas[var5];
            int var7 = var5 + 1;
            Dimension var8 = this.mipmapDimensions[var5];
            int var9 = var8.getWidth();
            int var10 = var8.getHeight();

            if (var4)
            {
                var6.clear();
                var2.clear();

                for (int var11 = 0; var11 < var9; ++var11)
                {
                    for (int var12 = 0; var12 < var10; ++var12)
                    {
                        int var13 = var2.getInt((var11 * 2 + 0 + (var12 * 2 + 0) * var3) * 4);
                        int var14 = var2.getInt((var11 * 2 + 1 + (var12 * 2 + 0) * var3) * 4);
                        int var15 = var2.getInt((var11 * 2 + 1 + (var12 * 2 + 1) * var3) * 4);
                        int var16 = var2.getInt((var11 * 2 + 0 + (var12 * 2 + 1) * var3) * 4);
                        int var17 = this.alphaBlend(var13, var14, var15, var16);
                        var6.putInt((var11 + var12 * var9) * 4, var17);
                    }
                }

                var6.clear();
                var2.clear();
            }

            if (var1)
            {
                GL11.glTexImage2D(this.textureTarget, var7, this.textureFormat, var9, var10, 0, this.textureFormat, GL11.GL_UNSIGNED_BYTE, var6);
            }

            var2 = var6;
            var3 = var9;

            if (var9 <= 1 || var10 <= 1)
            {
                var4 = false;
            }
        }
    }

    private void registerMipMapsSub(int var1, int var2, ByteBuffer[] var3, Dimension[] var4)
    {
        int var5 = var1 / 2;
        int var6 = var2 / 2;

        for (int var7 = 0; var7 < var3.length; ++var7)
        {
            ByteBuffer var8 = var3[var7];
            int var9 = var7 + 1;
            Dimension var10 = var4[var7];
            int var11 = var10.getWidth();
            int var12 = var10.getHeight();
            var8.clear();
            GL11.glTexSubImage2D(this.textureTarget, var9, var5, var6, var11, var12, this.textureFormat, GL11.GL_UNSIGNED_BYTE, var8);
            var5 /= 2;
            var6 /= 2;
        }
    }

    private int alphaBlend(int var1, int var2, int var3, int var4)
    {
        int var5 = this.alphaBlend(var1, var2);
        int var6 = this.alphaBlend(var3, var4);
        int var7 = this.alphaBlend(var5, var6);
        return var7;
    }

    private int alphaBlend(int var1, int var2)
    {
        int var3 = (var1 & -16777216) >> 24 & 255;
        int var4 = (var2 & -16777216) >> 24 & 255;
        int var5 = (var3 + var4) / 2;

        if (var3 == 0 && var4 == 0)
        {
            var3 = 1;
            var4 = 1;
        }
        else
        {
            if (var3 == 0)
            {
                var1 = var2;
                var5 /= 2;
            }

            if (var4 == 0)
            {
                var2 = var1;
                var5 /= 2;
            }
        }

        int var6 = (var1 >> 16 & 255) * var3;
        int var7 = (var1 >> 8 & 255) * var3;
        int var8 = (var1 & 255) * var3;
        int var9 = (var2 >> 16 & 255) * var4;
        int var10 = (var2 >> 8 & 255) * var4;
        int var11 = (var2 & 255) * var4;
        int var12 = (var6 + var9) / (var3 + var4);
        int var13 = (var7 + var10) / (var3 + var4);
        int var14 = (var8 + var11) / (var3 + var4);
        return var5 << 24 | var12 << 16 | var13 << 8 | var14;
    }

    private int averageColor(int var1, int var2)
    {
        int var3 = (var1 & -16777216) >> 24 & 255;
        int var4 = (var2 & -16777216) >> 24 & 255;
        return (var3 + var4 >> 1 << 24) + ((var1 & 16711422) + (var2 & 16711422) >> 1);
    }

    private void allocateMipmapDatas(int var1, int var2)
    {
        var1 = TextureUtils.ceilPowerOfTwo(var1);
        var2 = TextureUtils.ceilPowerOfTwo(var2);
        int var3 = var1 * var2 * 4;
        ArrayList var4 = new ArrayList();
        ArrayList var5 = new ArrayList();
        int var6 = var1;
        int var7 = var2;

        while (true)
        {
            var6 /= 2;
            var7 /= 2;

            if (var6 <= 0 && var7 <= 0)
            {
                this.mipmapDatas = (ByteBuffer[])((ByteBuffer[])var4.toArray(new ByteBuffer[var4.size()]));
                this.mipmapDimensions = (Dimension[])((Dimension[])var5.toArray(new Dimension[var5.size()]));
                return;
            }

            if (var6 <= 0)
            {
                var6 = 1;
            }

            if (var7 <= 0)
            {
                var7 = 1;
            }

            int var8 = var6 * var7 * 4;
            ByteBuffer var9 = GLAllocation.createDirectByteBuffer(var8);
            var4.add(var9);
            Dimension var10 = new Dimension(var6, var7);
            var5.add(var10);
        }
    }

    private int getMaxMipmapLevel(int var1)
    {
        int var2;

        for (var2 = 0; var1 > 0; ++var2)
        {
            var1 /= 2;
        }

        return var2 - 1;
    }

    public static boolean isMipMapTexture(String var0)
    {
        return var0.equals("terrain");
    }

    public void scaleUp(int var1)
    {
        if (this.textureTarget == 3553)
        {
            int var2 = TextureUtils.ceilPowerOfTwo(var1);
            int var3 = Math.max(this.width, this.height);

            for (int var4 = TextureUtils.ceilPowerOfTwo(var3); var4 < var2; var4 *= 2)
            {
                this.scale2x();
            }
        }
    }

    private void scale2x()
    {
        int var1 = this.width;
        int var2 = this.height;
        byte[] var3 = new byte[this.width * this.height * 4];
        this.textureData.position(0);
        this.textureData.get(var3);
        this.width *= 2;
        this.height *= 2;
        this.textureRect = new Rect2i(0, 0, this.width, this.height);
        this.textureData = GLAllocation.createDirectByteBuffer(this.width * this.height * 4);
        this.copyScaled(var3, var1, this.textureData, this.width);
    }

    private void copyScaled(byte[] var1, int var2, ByteBuffer var3, int var4)
    {
        int var5 = var4 / var2;
        byte[] var6 = new byte[4];
        int var7 = var4 * var4;
        var3.clear();

        if (var5 > 1)
        {
            for (int var8 = 0; var8 < var2; ++var8)
            {
                int var9 = var8 * var2;
                int var10 = var8 * var5;
                int var11 = var10 * var4;

                for (int var12 = 0; var12 < var2; ++var12)
                {
                    int var13 = (var12 + var9) * 4;
                    var6[0] = var1[var13];
                    var6[1] = var1[var13 + 1];
                    var6[2] = var1[var13 + 2];
                    var6[3] = var1[var13 + 3];
                    int var14 = var12 * var5;
                    int var15 = var14 + var11;

                    for (int var16 = 0; var16 < var5; ++var16)
                    {
                        int var17 = var15 + var16 * var4;
                        var3.position(var17 * 4);

                        for (int var18 = 0; var18 < var5; ++var18)
                        {
                            var3.put(var6);
                        }
                    }
                }
            }
        }

        var3.position(0).limit(var4 * var4 * 4);
    }

    public void updateMaxMipmapLevel(int var1)
    {
        if (this.mipmapActive)
        {
            if (GLContext.getCapabilities().OpenGL12)
            {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
                int var2 = Config.getMipmapLevel();

                if (var2 >= 4)
                {
                    int var3 = Math.min(this.width, this.height);
                    var2 = this.getMaxMipmapLevel(var3);

                    if (var1 > 1)
                    {
                        int var4 = TextureUtils.getPowerOfTwo(var1);
                        var2 -= var4;
                    }

                    if (var2 < 0)
                    {
                        var2 = 0;
                    }
                }

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, var2);
            }
        }
    }

    public void setTextureBound(boolean var1)
    {
        this.textureBound = var1;
    }

    public boolean isTextureBound()
    {
        return this.textureBound;
    }
}
