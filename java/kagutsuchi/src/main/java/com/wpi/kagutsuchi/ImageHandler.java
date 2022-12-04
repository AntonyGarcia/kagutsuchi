package com.wpi.kagutsuchi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ImageHandler {

    private JSONObject mainSettingsFile;
    private JSONObject pathSettingsFile;
    private List<String> imageList;
    private List<ExecutorService> services = new ArrayList<>();
    private int blackThreshold = 20;
    private int threadLimit = 10;
    private double blackPixelsThreshold = 0.9;

    public ImageHandler(JSONObject mainSettingsFile, JSONObject pathSettingsFile, List<String> imageList) {
        this.mainSettingsFile = mainSettingsFile;
        this.pathSettingsFile = pathSettingsFile;
        this.imageList = imageList;
    }

    public JSONObject getMainSettingsFile() {
        return mainSettingsFile;
    }

    public void setMainSettingsFile(JSONObject mainSettingsFile) {
        this.mainSettingsFile = mainSettingsFile;
    }

    public JSONObject getPathSettingsFile() {
        return pathSettingsFile;
    }

    public void setPathSettingsFile(JSONObject pathSettingsFile) {
        this.pathSettingsFile = pathSettingsFile;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public void cleanOutputPath() {
        try ( Stream<Path> paths = Files.walk(Paths.get("imgs"))) {
            paths.filter(Files::isRegularFile).forEach(a -> {
                if (a.getFileName().toString().contains(".jpg")) {
                    a.toFile().delete();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deadCellsSearch() {

        services.clear();
        int res = Integer.parseInt("" + pathSettingsFile.get("cell_size"));
        int width = res;
        int height = width;
        int xCells = Integer.parseInt("" + pathSettingsFile.get("x_cells"));
        int yCells = Integer.parseInt("" + pathSettingsFile.get("y_cells"));
        int colorScale = Integer.parseInt("" + pathSettingsFile.get("output_color_scale"));
        int pixelsPerImage = width * height;
        blackThreshold = Integer.parseInt("" + pathSettingsFile.get("black_pixel_color_threshold"));
        blackPixelsThreshold = Integer.parseInt("" + pathSettingsFile.get("black_pixel_count_limit")) * 0.01;

        if (colorScale == 0) {
            colorScale = BufferedImage.TYPE_INT_RGB;
        } else {
            colorScale = BufferedImage.TYPE_BYTE_GRAY;
        }

        for (int i = 0; i < imageList.size(); i++) {
            try {

                int h = height;
                int w = width;
                int vG = 0;
                int hG = 0;
                int cS = colorScale;
                int n = i;

                while (services.size() >= threadLimit) {
                    services = services.stream().filter(a -> !a.isTerminated()).collect(Collectors.toList());
                }

                BufferedImage image = ImageIO.read(new File(mainSettingsFile.get("images_path") + "\\" + imageList.get(i)));
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    int cellIndex = 0;
                    JSONArray array = new JSONArray();
                    for (int k = 0; k < yCells; k++) {
                        for (int j = 0; j < xCells; j++) {
                            BufferedImage theImage = new BufferedImage(w, h, cS);
                            for (int y = k * h; y < (h * k) + h; y++) {
                                for (int x = w * j; x < (w * j) + w; x++) {
                                    theImage.setRGB(hG + (x - (w * j)), vG + (y - (k * h)), image.getRGB(x, y));
                                }
                            }
                            int blackPixels = 0;
                            for (int l = 0; l < theImage.getWidth(); l++) {
                                for (int m = 0; m < theImage.getHeight(); m++) {
                                    int color = theImage.getRGB(l, m);
                                    int blue = color & 0xff;
                                    int green = (color & 0xff00) >> 8;
                                    int red = (color & 0xff0000) >> 16;
                                    if ((blue < blackThreshold) && (red < blackThreshold) && (green < blackThreshold)) {
                                        blackPixels++;
                                    }
                                }
                            }
                            //  System.out.println(blackPixels+" "+(blackPixelsThreshold * pixelsPerImage));

                            if (blackPixels > (blackPixelsThreshold * pixelsPerImage)) {
                                JSONArray positiveArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(imageList.get(n));
                                boolean posFlag = false;
                                for (int l = 0; l < positiveArray.size(); l++) {
                                    if (String.valueOf(positiveArray.get(l)).equals(cellIndex + "")) {
                                        posFlag = true;
                                        break;
                                    }
                                }
                                //   System.out.println(imageList.get(n) + " " + cellIndex + " " + positiveArray + " " + posFlag);
                                if (!posFlag) {
                                    array.add(cellIndex);
                                }
                            }
                            cellIndex++;
                        }
                        ((JSONObject) ((JSONArray) pathSettingsFile.get("black_cells")).get(0)).put(imageList.get(n), array);
                    }

                });

                executor.shutdown();
                services.add(executor);

            } catch (IOException ex) {
                Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void test() throws IOException {
        long startTime = System.currentTimeMillis();

        cleanOutputPath();

        int vGap = 0;
        int hGap = 0;
        int res = Integer.parseInt("" + pathSettingsFile.get("cell_size"));
        int width = res;
        int height = width;
        int xCells = Integer.parseInt("" + pathSettingsFile.get("x_cells"));
        int yCells = Integer.parseInt("" + pathSettingsFile.get("y_cells"));
        int finalImageSize = Integer.parseInt("" + pathSettingsFile.get("final_image_size"));
        int fillingColor = Integer.parseInt("" + pathSettingsFile.get("filling_color"));
        int fillindMode = Integer.parseInt("" + pathSettingsFile.get("filling_mode"));
        int fillingGap = Integer.parseInt("" + pathSettingsFile.get("filling_gap"));
        int colorScale = Integer.parseInt("" + pathSettingsFile.get("output_color_scale"));

        int frameRotation = Integer.parseInt("" + pathSettingsFile.get("frame_rotation"));
        int frameFlip = Integer.parseInt("" + pathSettingsFile.get("frame_flip"));
        int frameFlipRotation = Integer.parseInt("" + pathSettingsFile.get("frame_flip_rotation"));

        if (colorScale == 0) {
            colorScale = BufferedImage.TYPE_INT_RGB;
        } else {
            colorScale = BufferedImage.TYPE_BYTE_GRAY;
        }

        switch (fillindMode) {
            case 1:
                vGap = (int) (fillingGap * 0.5);
                hGap = (int) (fillingGap * 0.5);
                break;
            case 2:
                vGap = 0;
                hGap = 0;
                break;
            case 3:
                hGap = fillingGap;
                vGap = 0;
                break;
            case 4:
                hGap = 0;
                vGap = fillingGap;
                break;
            case 5:
                hGap = fillingGap;
                vGap = fillingGap;
                break;
            case 6:
                hGap = 0;
                vGap = 0;
                width = finalImageSize;
                height = finalImageSize;
                break;
        }

        services.clear();
        for (int i = 0; i < imageList.size(); i++) {
            int n = i;
            int cS = colorScale;
            int h = height;
            int w = width;
            int vG = vGap;
            int hG = hGap;

            while (services.size() >= threadLimit) {
                services = services.stream().filter(a -> !a.isTerminated()).collect(Collectors.toList());
            }

            JSONArray array = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(imageList.get(i));
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    List<Integer> positiveIndexes = new ArrayList<>();
                    for (int j = 0; j < array.size(); j++) {
                        positiveIndexes.add(Integer.parseInt(array.get(j) + ""));
                    }
                    BufferedImage image = ImageIO.read(new File(mainSettingsFile.get("images_path") + "\\" + imageList.get(n)));

                    if (fillindMode == 6) {
                        Image img = image.getScaledInstance(xCells * finalImageSize, yCells * finalImageSize, Image.SCALE_SMOOTH);
                        BufferedImage dimg = new BufferedImage(xCells * finalImageSize, yCells * finalImageSize, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = dimg.createGraphics();
                        g2d.drawImage(img, 0, 0, null);
                        g2d.dispose();
                        image = dimg;
                    }

                    int cellIndex = 0;
                    for (int k = 0; k < yCells; k++) {
                        for (int j = 0; j < xCells; j++) {

                            BufferedImage theImage = new BufferedImage(finalImageSize, finalImageSize, BufferedImage.TYPE_INT_RGB);
                            for (int x = 0; x < finalImageSize; x++) {
                                for (int y = 0; y < finalImageSize; y++) {
                                    if (fillingColor == 0) {
                                        theImage.setRGB(x, y, new Color(0, 0, 0).getRGB());
                                    } else {
                                        theImage.setRGB(x, y, new Color(255, 255, 255).getRGB());
                                    }
                                }
                            }
                            for (int y = k * h; y < (h * k) + h; y++) {
                                for (int x = w * j; x < (w * j) + w; x++) {
                                    theImage.setRGB(hG + (x - (w * j)), vG + (y - (k * h)), image.getRGB(x, y));
                                }
                            }

                            if (positiveIndexes.contains(cellIndex)) {
                                Image img = theImage.getScaledInstance(theImage.getWidth(), theImage.getHeight(), Image.SCALE_SMOOTH);

                                for (int l = 0; l <= 3; l++) {
                                    BufferedImage dimg = new BufferedImage(theImage.getWidth(), theImage.getHeight(), cS);
                                    Graphics2D g2d = dimg.createGraphics();
                                    g2d.rotate(Math.toRadians(l * 90), theImage.getWidth() / 2, theImage.getHeight() / 2);
                                    g2d.drawImage(img, 0, 0, null);
                                    g2d.dispose();
                                    exportImage(dimg, "imgs/" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_rot_" + l + "_pos.jpg");
                                    if (frameRotation == 0) {
                                        break;
                                    }
                                }

                                if (frameFlip == 1) {
                                    BufferedImage flipImg = new BufferedImage(theImage.getWidth(), theImage.getHeight(), cS);
                                    BufferedImage flipImg2 = new BufferedImage(theImage.getWidth(), theImage.getHeight(), cS);
                                    Graphics2D g2dGraphics = flipImg2.createGraphics();
                                    g2dGraphics.drawImage(img, 0, 0, null);
                                    g2dGraphics.dispose();
                                    for (int l = 0; l < flipImg2.getWidth(); l++) {
                                        for (int m = 0; m < flipImg2.getHeight(); m++) {
                                            flipImg.setRGB(l, m, flipImg2.getRGB(flipImg2.getWidth() - l - 1, m));
                                        }
                                    }
                                    // exportImage(dimg, "imgs/" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_flip.jpg");

                                    for (int l = 0; l <= 3; l++) {
                                        BufferedImage dimg = new BufferedImage(flipImg.getWidth(), flipImg.getHeight(), cS);
                                        Graphics2D g2d = dimg.createGraphics();
                                        g2d.rotate(Math.toRadians(l * 90), flipImg.getWidth() / 2, flipImg.getHeight() / 2);
                                        g2d.drawImage(flipImg, 0, 0, null);
                                        g2d.dispose();
                                        exportImage(dimg, "imgs/" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_flip_rot_" + l + "_pos.jpg");
                                        if (frameFlipRotation == 0) {
                                            break;
                                        }
                                    }
                                }
                            } else {

                                Image img = theImage.getScaledInstance(theImage.getWidth(), theImage.getHeight(), Image.SCALE_SMOOTH);
                                BufferedImage dimg = new BufferedImage(theImage.getWidth(), theImage.getHeight(), cS);
                                Graphics2D g2d = dimg.createGraphics();
                                g2d.drawImage(img, 0, 0, null);
                                g2d.dispose();
                                exportImage(dimg, "imgs/" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_neg.jpg");
                            }

                            cellIndex++;
                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            executor.shutdown();
            services.add(executor);
            break;
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: " + estimatedTime);
    }

    public void exportImage(BufferedImage dimg, String path) throws IOException {
        File outputFile = new File(path);
        ImageIO.write(dimg, "jpg", outputFile);
    }
}
