package com.htmlhifive.testexplorer.image;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public abstract class ConvolveOpGenerator {
    protected static float[] getGaussianValues(double sigma) {
        int halfSize = (int) (sigma * 3);
        float[] result = new float[halfSize * 2 + 1];
        float sum = 0;
        for (int x = -halfSize; x <= halfSize; x++) {
            result[x + halfSize] = (float) Math.exp(-(x * x) / (2 * sigma * sigma));
            sum += result[x + halfSize];
        }
        for (int i = 0; i < halfSize * 2 + 1; i++)
            result[i] /= sum;
        return result;
    }

    public static ConvolveOp GenerateGaussianX(double sigma) {
        float[] gaussian1D = getGaussianValues(sigma);
        return new ConvolveOp(new Kernel(gaussian1D.length, 1, gaussian1D), ConvolveOp.EDGE_NO_OP, null);
    }

    public static ConvolveOp GenerateGaussianY(double sigma) {
        float[] gaussian1D = getGaussianValues(sigma);
        return new ConvolveOp(new Kernel(1, gaussian1D.length, gaussian1D), ConvolveOp.EDGE_NO_OP, null);
    }
}
