package me.desht.modularrouters.client.fx;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Queue;

public class FXSparkle extends Particle {

    public static final ResourceLocation particles = new ResourceLocation(ModularRouters.MODID + ":textures/misc/particleblob.png");

    private static final Queue<FXSparkle> queuedRenders = new ArrayDeque<>();
    private static final Queue<FXSparkle> queuedCorruptRenders = new ArrayDeque<>();

    // Queue values
    private float partialTicks;
    private float rx;
    private float rz;
    private float ryz;
    private float rxy;
    private float rxz;

    public boolean noClip = false;

    public FXSparkle(World world, double x, double y, double z, float size, float red, float green, float blue, int m) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);

        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
        particleAlpha = 0.5F; // So MC renders us on the alpha layer, value not actually used
        particleGravity = 0;
        motionX = motionY = motionZ = 0;
//        particleScale *= size;
        maxAge = 3 * m;
        multiplier = m;
        noClip = false;
        setSize(0.01F, 0.01F);
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
    }

    static void dispatchQueuedRenders(Tessellator tessellator) {
        ParticleRenderDispatcher.sparkleFxCount = 0;
        ParticleRenderDispatcher.fakeSparkleFxCount = 0;

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.75F);
        Minecraft.getInstance().getTextureManager().bindTexture(particles);

        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for(FXSparkle sparkle : queuedRenders)
            sparkle.renderQueued(tessellator);
        tessellator.draw();

        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for(FXSparkle sparkle : queuedCorruptRenders)
            sparkle.renderQueued(tessellator);
        tessellator.draw();

        queuedRenders.clear();
        queuedCorruptRenders.clear();
    }

    private void renderQueued(Tessellator tessellator) {
        if(fake)
            ParticleRenderDispatcher.fakeSparkleFxCount++;
        else ParticleRenderDispatcher.sparkleFxCount++;

        int part = particle + age / multiplier;

        float var8 = part % 8 / 8.0F;
        float var9 = var8 + 0.0624375F*2;
        float var10 = part / 8 / 8.0F;
        float var11 = var10 + 0.0624375F*2;
        float var12 = 0.1F /** particleScale*/;
        if (shrink) var12 *= (maxAge-age+1)/(float)maxAge;
        float var13 = (float)(prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float var14 = (float)(prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float var15 = (float)(prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
        float var16 = 1.0F;

        tessellator.getBuffer().pos(var13 - rx * var12 - rxy * var12, var14 - rz * var12, var15 - ryz * var12 - rxz * var12).tex(var9, var11).color(particleRed * var16, particleGreen * var16, particleBlue * var16, 1).endVertex();
        tessellator.getBuffer().pos(var13 - rx * var12 + rxy * var12, var14 + rz * var12, var15 - ryz * var12 + rxz * var12).tex(var9, var10).color(particleRed * var16, particleGreen * var16, particleBlue * var16, 1).endVertex();
        tessellator.getBuffer().pos(var13 + rx * var12 + rxy * var12, var14 + rz * var12, var15 + ryz * var12 + rxz * var12).tex(var8, var10).color(particleRed * var16, particleGreen * var16, particleBlue * var16, 1).endVertex();
        tessellator.getBuffer().pos(var13 + rx * var12 - rxy * var12, var14 - rz * var12, var15 + ryz * var12 - rxz * var12).tex(var8, var11).color(particleRed * var16, particleGreen * var16, particleBlue * var16, 1).endVertex();

    }

    @Override
    public IParticleRenderType func_217558_b() {
        return IParticleRenderType.field_217605_e;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, ActiveRenderInfo entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        this.partialTicks = partialTicks;
        this.rx = rotationX;
        this.rz = rotationZ;
        this.ryz = rotationYZ;
        this.rxy = rotationXY;
        this.rxz = rotationXZ;

        if(corrupt)
            queuedCorruptRenders.add(this);
        else queuedRenders.add(this);
    }

    @Override
    public void tick() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (age++ >= maxAge)
            setExpired();

        motionY -= 0.04D * particleGravity;

        if (!noClip && !fake)
            wiggleAround(posX, (getBoundingBox().minY + getBoundingBox().maxY) / 2.0D, posZ);

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        if (slowdown) {
            motionX *= 0.908000001907348633D;
            motionY *= 0.908000001907348633D;
            motionZ *= 0.908000001907348633D;
        }

        if(fake && age > 1)
            setExpired();
    }

    public void setGravity(float value) {
        particleGravity = value;
    }

    // copy of Entity#func_213282_i (pushOutOfBlocks) with a couple of changes
    private void wiggleAround(double x, double y, double z) {
        BlockPos blockpos = new BlockPos(x, y, z);
        Vec3d vec3d = new Vec3d(x - (double)blockpos.getX(), y - (double)blockpos.getY(), z - (double)blockpos.getZ());
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        Direction direction = Direction.UP;
        double d0 = Double.MAX_VALUE;

        for(Direction direction1 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            blockpos$mutableblockpos.setPos(blockpos).move(direction1);
            if (!Block.isOpaque(this.world.getBlockState(blockpos$mutableblockpos).getCollisionShape(this.world, blockpos$mutableblockpos))) {
                double d1 = vec3d.func_216370_a(direction1.getAxis());
                double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;
                if (d2 < d0) {
                    d0 = d2;
                    direction = direction1;
                }
            }
        }

        float f = this.rand.nextFloat() *  0.05F + 0.01F;  // smaller multiplier & adder
        float f1 = (float)direction.getAxisDirection().getOffset();
        float secondary = (rand.nextFloat() - rand.nextFloat()) * 0.1F;  // extra secondary movement

        if (direction.getAxis() == Direction.Axis.X) {
            motionX += (double)(f1 * f);
            motionY = motionZ = secondary;
        } else if (direction.getAxis() == Direction.Axis.Y) {
            motionY += (double)(f1 * f);
            motionX = motionZ = secondary;
        } else if (direction.getAxis() == Direction.Axis.Z) {
            motionZ += (double)(f1 * f);
            motionX = motionY = secondary;
        }

    }

    public boolean corrupt = false;
    public boolean fake = false;
    private int multiplier = 2;
    private final boolean shrink = true;
    public final int particle = 16;
    public boolean tinkle = false;
    public final boolean slowdown = true;
    public int currentColor = 0;
}