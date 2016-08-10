package me.desht.modularrouters.client.fx;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.world.World;

import java.awt.*;

public class ParticleBeam {
	private static final int MAX_ITERS = 500;

    public static void doParticleBeam(World world, Vector3 orig, Vector3 end) {
		if (!world.isRemote)
			return;

		Vector3 diff = end.subtract(orig);
		Vector3 movement = diff.normalize().multiply(0.05);
		int iters = Math.min(MAX_ITERS, (int) (diff.mag() / movement.mag()));
		float huePer = 1F / iters;
		float hueSum = (float) Math.random();

		Vector3 currentPos = orig;
		for(int i = 0; i < iters; i++) {
			float hue = i * huePer + hueSum;
			Color color = Color.getHSBColor(hue, 1F, 1F);
			float r = color.getRed() / 255F;
			float g = color.getGreen() / 255F;
			float b = color.getBlue() / 255F;

			ModularRouters.proxy.setSparkleFXNoClip(true);
            ModularRouters.proxy.sparkleFX(world, currentPos.x, currentPos.y, currentPos.z, r, g, b, 0.5F, 4);
            ModularRouters.proxy.setSparkleFXNoClip(false);
			currentPos = currentPos.add(movement);
		}
	}
}
