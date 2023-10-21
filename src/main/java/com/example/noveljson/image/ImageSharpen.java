package com.example.noveljson.image;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageSharpen {

    /**
     * 图像锐化 二阶微分锐化，拉普拉斯算子 定义一个3*3滤波器，计算中心像素与上下左右四个像素差值
     *
     * @param image
     */
    public static BufferedImage lapLaceSharpDeal(BufferedImage image) {
        BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
//            Stream.iterate(1, item -> item+1).limit(image.getWidth()-2)
//                    .parallel().forEach( i -> {
        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {
                int rgb = image.getRGB(i, j);
                int rgb1 = image.getRGB(i - 1, j);
                int rgb2 = image.getRGB(i + 1, j);
                int rgb3 = image.getRGB(i, j - 1);
                int rgb4 = image.getRGB(i, j + 1);
                int[] R = new int[]{(rgb1 >> 16) & 0xff, (rgb2 >> 16) & 0xff, (rgb3 >> 16) & 0xff,
                        (rgb4 >> 16) & 0xff, (rgb >> 16) & 0xff};
                int[] G = new int[]{(rgb1 >> 8) & 0xff, (rgb2 >> 8) & 0xff, (rgb3 >> 8) & 0xff, (rgb4 >> 8) & 0xff,
                        (rgb >> 8) & 0xff};
                int[] B = new int[]{rgb1 & 0xff, rgb2 & 0xff, rgb3 & 0xff, rgb4 & 0xff, rgb & 0xff};
                double dR = R[0] + R[1] + R[2] + R[3] - 4 * R[4];
                double dG = G[0] + G[1] + G[2] + G[3] - 4 * G[4];
                double dB = B[0] + B[1] + B[2] + B[3] - 4 * B[4];

                double r = R[4] - dR;
                double g = G[4] - dG;
                double b = B[4] - dB;

                rgb = (255 & 0xff) << 24 | (clamp((int) r) & 0xff) << 16 | (clamp((int) g) & 0xff) << 8
                        | (clamp((int) b) & 0xff);
                tempImage.setRGB(i, j, rgb);
            }
        }
        return tempImage;
    }

    /**
     * 一阶微分梯度锐化
     */
    public static BufferedImage degreeSharpDeal(BufferedImage image) {
        BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 1; i < image.getWidth() - 1; i++) {
            for (int j = 1; j < image.getHeight() - 1; j++) {
                List<Integer> rList = new ArrayList<>();
                List<Integer> gList = new ArrayList<>();
                List<Integer> bList = new ArrayList<>();
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        int rgb = image.getRGB(i + x, j + y);
                        int R = (rgb >> 16) & 0xff;
                        int G = (rgb >> 8) & 0xff;
                        int B = rgb & 0xff;
                        rList.add(R);
                        gList.add(G);
                        bList.add(B);
                    }
                }
                int r = getResult(rList);
                int g = getResult(gList);
                int b = getResult(bList);
                r = rList.get(4) + r / 4;
                g = gList.get(4) + g / 4;
                b = bList.get(4) + b / 4;
                int rgb = (255 & 0xff) << 24 | (clamp(r) & 0xff) << 16 | (clamp(g) & 0xff) << 8 | (clamp(b) & 0xff);
                tempImage.setRGB(i, j, rgb);
            }
        }
        return tempImage;
    }

    // 执行一阶微分计算
    private static int getResult(List<Integer> list) {
        int result = Math.abs(list.get(0) + list.get(3) + list.get(6) - list.get(2) - list.get(5) - list.get(8))
                + Math.abs(list.get(0) + list.get(1) + list.get(2) - list.get(6) - list.get(7) - list.get(8));
        return result;
    }

    // 判断a,r,g,b值，大于256返回256，小于0则返回0,0到256之间则直接返回原始值
    private static int clamp(int rgb) {
        if (rgb > 255)
            return 255;
        if (rgb < 0)
            return 0;
        return rgb;
    }
}
