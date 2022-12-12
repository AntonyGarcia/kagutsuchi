package com.wpi.kagutsuchi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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

    private static JSONObject mainSettingsFile;
    private static JSONObject pathSettingsFile;
    private List<String> imageList;
    private List<ExecutorService> services = new ArrayList<>();
    private int blackThreshold = 20;
    private int threadLimit = 10;
    private double blackPixelsThreshold = 0.9;
    private static List<Integer> outputPixelList = new ArrayList<>();

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

    public void cleanOutputPath(String path) {
        try ( Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(a -> {
                if (a.getFileName().toString().contains(".jpg")) {
                    a.toFile().delete();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void splitImagesIntoDatasets() {
        try {
            int testDatasetSize = 0;
            int trainDatasetSize = 0;
            int positiveCells = 0;
            int negativeCells = 0;
            int totalCells = 0;
            double posPerc = 0;
            double negPerc = 0;
            int flip = 0;
            int rotate = 0;
            int flipAndRotate = 0;
            int positiveTestFrames = 0;
            int cellCount = Integer.parseInt(pathSettingsFile.get("total_cells") + "");
            int blackRemovalRate = Integer.parseInt(pathSettingsFile.get("black_cell_removal_rate") + "");

            String path = String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "\\exported_images");
            List<File> files = Files.list(Paths.get(path))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().contains(".jpg"))
                    .collect(Collectors.toList());

            positiveCells = Integer.parseInt(pathSettingsFile.get("positive_cells_to_export") + "");
            negativeCells = Integer.parseInt(pathSettingsFile.get("negative_cells_to_export") + "");

            testDatasetSize = Integer.parseInt(pathSettingsFile.get("test_dataset_size") + "");
            trainDatasetSize = Integer.parseInt(pathSettingsFile.get("train_dataset_size") + "");

            rotate = Integer.parseInt(pathSettingsFile.get("frame_rotation") + "");
            flip = Integer.parseInt(pathSettingsFile.get("frame_flip") + "");
            flipAndRotate = Integer.parseInt(pathSettingsFile.get("frame_flip_rotation") + "");

            totalCells = positiveCells + negativeCells;
            posPerc = positiveCells * Math.pow(totalCells, -1);
            negPerc = negPerc * Math.pow(totalCells, -1);

            positiveTestFrames = (int) (posPerc * testDatasetSize);

            List<String> testDatasetImages = new ArrayList<>();
            List<String> trainDatasetImages = new ArrayList<>();

            double minimumEquityDifference = 100;
            int testPositiveFrames = 0;
            int testNegativeFrames = 0;
            int epochs = 0;
            
            System.out.println("->"+positiveTestFrames);
            
            while (true) {
                int posFrames = 0;
                int negFrames = 0;
                List<String> allImages = new ArrayList<>();
                imageList.forEach(a -> allImages.add(new String(a.toString() + "")));
                List<String> testImages = new ArrayList<>();
                while (true) {
                    Random r = new Random();
                    int low = 0;
                    int high = allImages.size();
                    int result = r.nextInt(high - low) + low;
                    testImages.add(new String(allImages.get(result) + ""));
                    allImages.remove(result);
                    boolean outFlag = false;
                    negFrames = 0;
                    posFrames = 0;
                    for (int i = 0; i < testImages.size(); i++) {
                        String img = testImages.get(i);
                        JSONArray positiveArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(img);
                        int pf = positiveArray.size();
                        negFrames = negFrames + cellCount - pf;
                        posFrames = posFrames + pf + (pf * flip) + (pf * flipAndRotate * 3) + (pf * rotate * 3);
                    }
                    if ((posFrames > positiveTestFrames) && (negFrames > positiveTestFrames)) {
                        break;
                    }
                }

                double equityPerc = posFrames * Math.pow(posFrames + negFrames, -1) * 100;
                if (Math.abs(equityPerc - 50) < minimumEquityDifference) {
                    minimumEquityDifference = Math.abs(equityPerc - 50);
                    testPositiveFrames = posFrames;
                    testNegativeFrames = negFrames;
                }
        
                if (minimumEquityDifference < 10) {
                    System.out.println("Equity: "+minimumEquityDifference+" pos: "+posFrames+" neg: "+negFrames);
                    testDatasetImages = testImages;
                    trainDatasetImages = allImages;
                    break;
                }
                epochs++;

                /*if (epochs > 10000) {
                    break;
                }*/
            }

            List<File> testDataset = new ArrayList<>();
            for (int i = 0; i < testDatasetImages.size(); i++) {
                String img = testDatasetImages.get(i).replace(".jpg", "_");
                files.stream().filter(a -> a.getAbsoluteFile().toString().contains(img)).forEach(b -> testDataset.add(b));
            }

            List<File> trainDataset = new ArrayList<>();
            for (int i = 0; i < trainDatasetImages.size(); i++) {
                String img = trainDatasetImages.get(i).replace(".jpg", "_");
                files.stream().filter(a -> a.getAbsoluteFile().toString().contains(img)).forEach(b -> trainDataset.add(b));
            }

            for (int i = 0; i < trainDataset.size(); i++) {
                File f = trainDataset.get(i);
                Files.copy(f.toPath(), new File(f.getParent() + "\\train_dataset\\" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            for (int i = 0; i < testDataset.size(); i++) {
                File f = testDataset.get(i);
                Files.copy(f.toPath(), new File(f.getParent() + "\\test_dataset\\" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            List<File> testingFiles = Files.list(Paths.get(path + "\\test_dataset"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().contains(".jpg"))
                    .collect(Collectors.toList());

            int positiveTestingFrames = testingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_pos.jpg")).collect(Collectors.toList()).size();
            int negativeTestingFrames = testingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_neg.jpg")).collect(Collectors.toList()).size();

            int difference = Math.abs(positiveTestingFrames - negativeTestingFrames);
            if (positiveTestingFrames > negativeTestingFrames) {
                testingFiles = Files.list(Paths.get(path + "\\test_dataset"))
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .filter(a -> a.getName().contains(".jpg"))
                        .collect(Collectors.toList());

                Collections.shuffle(testingFiles);
                testingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_pos.")).collect(Collectors.toList()).subList(0, difference).forEach(a -> a.delete());
            } else {
                testingFiles = Files.list(Paths.get(path + "\\test_dataset"))
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .filter(a -> a.getName().contains(".jpg"))
                        .collect(Collectors.toList());

                Collections.shuffle(testingFiles);
                testingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_neg.")).collect(Collectors.toList()).subList(0, difference).forEach(a -> a.delete());
            }

            List<File> trainingFiles = Files.list(Paths.get(path + "\\train_dataset"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().contains(".jpg"))
                    .collect(Collectors.toList());

            int positiveTrainingFrames = trainingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_pos.jpg")).collect(Collectors.toList()).size();
            int negativeTrainingFrames = trainingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_neg.jpg")).collect(Collectors.toList()).size();
            difference = Math.abs(positiveTrainingFrames - negativeTrainingFrames);
            if (positiveTrainingFrames > negativeTrainingFrames) {
                for (int i = 0; i < difference; i++) {
                    trainingFiles.stream()
                            .filter(a -> a.getAbsoluteFile().toString().contains("_pos."))
                            .filter(a -> a.getAbsoluteFile().toString().contains("flip") || a.getAbsoluteFile().toString().contains("rot")).findFirst().get().delete();

                    trainingFiles = Files.list(Paths.get(path + "\\train_dataset"))
                            .map(Path::toFile)
                            .filter(File::isFile)
                            .filter(a -> a.getName().contains(".jpg"))
                            .collect(Collectors.toList());

                    Collections.shuffle(trainingFiles);
                }
            } else {
                int blackCells = (int) trainingFiles.stream()
                        .filter(a -> a.getAbsoluteFile().toString().contains("_neg."))
                        .filter(a -> a.getAbsoluteFile().toString().contains("_black_")).count();

                blackCells = (int) (blackCells * (blackRemovalRate * 0.1));

                if (blackCells <= difference) {

                    trainingFiles = Files.list(Paths.get(path + "\\train_dataset"))
                            .map(Path::toFile)
                            .filter(File::isFile)
                            .filter(a -> a.getName().contains(".jpg"))
                            .collect(Collectors.toList());

                    Collections.shuffle(trainingFiles);

                    trainingFiles.stream()
                            .filter(a -> a.getAbsoluteFile().toString().contains("_neg."))
                            .filter(a -> a.getAbsoluteFile().toString().contains("_black_")).collect(Collectors.toList()).subList(0, blackCells).forEach(a -> a.delete());
                }

                positiveTrainingFrames = blackCells = (int) trainingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_pos.")).count();
                negativeTrainingFrames = blackCells = (int) trainingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_neg.")).count();

                difference = negativeTrainingFrames - positiveTrainingFrames;

                trainingFiles = Files.list(Paths.get(path + "\\train_dataset"))
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .filter(a -> a.getName().contains(".jpg"))
                        .collect(Collectors.toList());

                Collections.shuffle(trainingFiles);
                trainingFiles.stream().filter(a -> a.getAbsoluteFile().toString().contains("_neg.")).collect(Collectors.toList()).subList(0, difference).forEach(a -> a.delete());

            }

            files.forEach(a -> a.delete());

            //  buildCsvDatasets();
        } catch (IOException ex) {
            Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deadCellsSearch() {

        services.clear();

        if (pathSettingsFile.get("black_cells") == null) {
            JSONArray arr = new JSONArray();
            arr.add(new JSONObject());
            pathSettingsFile.put("black_cells", arr);
        }

        int xC = 0;
        int yC = 0;
        int res = 0;
        int colS = 0;
        try {
            res = Integer.parseInt("" + pathSettingsFile.get("cell_size"));
            xC = Integer.parseInt("" + pathSettingsFile.get("x_cells"));
            yC = Integer.parseInt("" + pathSettingsFile.get("y_cells"));
            colS = Integer.parseInt("" + pathSettingsFile.get("output_color_scale"));
        } catch (Exception e) {
            pathSettingsFile.put("cell_size", "1");
            pathSettingsFile.put("x_cells", "1");
            pathSettingsFile.put("y_cells", "1");
            pathSettingsFile.put("output_color_scale", "1");
            res = 1;
            xC = 1;
            yC = 1;
            colS = 0;
        }

        int xCells = xC;
        int yCells = yC;
        int colorScale = colS;

        int width = res;
        int height = width;
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

                            if (blackPixels > (blackPixelsThreshold * pixelsPerImage)) {

                                JSONArray positiveArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(imageList.get(n));
                                boolean posFlag = false;
                                for (int l = 0; l < positiveArray.size(); l++) {
                                    if (String.valueOf(positiveArray.get(l)).equals(cellIndex + "")) {
                                        posFlag = true;
                                        break;
                                    }
                                }

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

        cleanOutputPath(String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images\\train_dataset");
        cleanOutputPath(String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images\\test_dataset");
        cleanOutputPath(String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images");

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

            JSONArray positiveArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(imageList.get(i));
            JSONArray blackArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("black_cells")).get(0)).get(imageList.get(i));

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    List<Integer> positiveIndexes = new ArrayList<>();
                    for (int j = 0; j < positiveArray.size(); j++) {
                        positiveIndexes.add(Integer.parseInt(positiveArray.get(j) + ""));
                    }

                    List<Integer> blackIndexes = new ArrayList<>();
                    for (int j = 0; j < blackArray.size(); j++) {
                        blackIndexes.add(Integer.parseInt(blackArray.get(j) + ""));
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

                                    exportImage(dimg, String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images\\" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_rot_" + l + "_pos.jpg");
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
                                    for (int l = 0; l <= 3; l++) {
                                        BufferedImage dimg = new BufferedImage(flipImg.getWidth(), flipImg.getHeight(), cS);
                                        Graphics2D g2d = dimg.createGraphics();
                                        g2d.rotate(Math.toRadians(l * 90), flipImg.getWidth() / 2, flipImg.getHeight() / 2);
                                        g2d.drawImage(flipImg, 0, 0, null);
                                        g2d.dispose();
                                        exportImage(dimg, String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images\\" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + "_flip_rot_" + l + "_pos.jpg");
                                        if (frameFlipRotation == 0) {
                                            break;
                                        }
                                    }
                                }
                            } else {

                                String str = "";
                                if (blackIndexes.contains(cellIndex)) {
                                    str = "_black_";
                                }

                                Image img = theImage.getScaledInstance(theImage.getWidth(), theImage.getHeight(), Image.SCALE_SMOOTH);
                                BufferedImage dimg = new BufferedImage(theImage.getWidth(), theImage.getHeight(), cS);
                                Graphics2D g2d = dimg.createGraphics();
                                g2d.drawImage(img, 0, 0, null);
                                g2d.dispose();
                                exportImage(dimg, String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "") + "\\exported_images\\" + imageList.get(n).replace(".jpg", "") + "_" + cellIndex + str + "_neg.jpg");
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
        }
        while (services.stream().filter(a -> !a.isTerminated()).count() > 0);

        splitImagesIntoDatasets();

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: " + estimatedTime);
    }

    public void exportImage(BufferedImage dimg, String path) throws IOException {
        File outputFile = new File(path);
        ImageIO.write(dimg, "jpg", outputFile);
    }

    public void buildCsvDatasets() {
        try {
            String path = String.valueOf(mainSettingsFile.get("images_path")).replace("\\split_images\\ir_images", "\\exported_images");

            String testPath = String.valueOf(mainSettingsFile.get("images_path")).replace("\\images\\split_images\\ir_images", "\\datasets\\test.csv");
            String trainPath = String.valueOf(mainSettingsFile.get("images_path")).replace("\\images\\split_images\\ir_images", "\\datasets\\train.csv");

            new File(testPath + "").delete();
            new File(trainPath + "").delete();

            List<File> trainingFiles = Files.list(Paths.get(path + "\\train_dataset"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().contains(".jpg"))
                    .collect(Collectors.toList());
            List<File> testingFiles = Files.list(Paths.get(path + "\\test_dataset"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().contains(".jpg"))
                    .collect(Collectors.toList());

            Collections.shuffle(trainingFiles);
            Collections.shuffle(testingFiles);

            services.clear();

            for (int i = 0; i < testingFiles.size(); i++) {
                while (services.size() >= threadLimit) {
                    services = services.stream().filter(a -> !a.isTerminated()).collect(Collectors.toList());
                }
                int n = i;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    BufferedImage image;
                    try {
                        image = ImageIO.read(testingFiles.get(n).getAbsoluteFile());
                        int h = image.getHeight();
                        int w = image.getWidth();
                        List<Integer> pixels = new ArrayList<>();
                        pixels.add(n);
                        if (testingFiles.get(n).getAbsoluteFile().toString().contains("_pos.")) {
                            pixels.add(1);
                        } else {
                            pixels.add(0);
                        }
                        for (int j = 0; j < h; j++) {
                            for (int k = 0; k < w; k++) {
                                int color = image.getRGB(k, j);
                                int blue = color & 0xff;
                                int green = (color & 0xff00) >> 8;
                                int red = (color & 0xff0000) >> 16;
                                pixels.add(blue);
                            }
                        }
                        appendLineToOutput(pixels, "test.csv");
                    } catch (IOException ex) {
                        Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                executor.shutdown();
                services.add(executor);
            }

            for (int i = 0; i < trainingFiles.size(); i++) {
                while (services.size() >= threadLimit) {
                    services = services.stream().filter(a -> !a.isTerminated()).collect(Collectors.toList());
                }
                int n = i;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    BufferedImage image;
                    try {
                        image = ImageIO.read(trainingFiles.get(n).getAbsoluteFile());
                        int h = image.getHeight();
                        int w = image.getWidth();
                        List<Integer> pixels = new ArrayList<>();
                        pixels.add(n);
                        if (trainingFiles.get(n).getAbsoluteFile().toString().contains("_pos.")) {
                            pixels.add(1);
                        } else {
                            pixels.add(0);
                        }
                        for (int j = 0; j < h; j++) {
                            for (int k = 0; k < w; k++) {
                                int color = image.getRGB(k, j);
                                int blue = color & 0xff;
                                int green = (color & 0xff00) >> 8;
                                int red = (color & 0xff0000) >> 16;
                                pixels.add(blue);
                            }
                        }
                        appendLineToOutput(pixels, "train.csv");
                    } catch (IOException ex) {
                        Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                executor.shutdown();
                services.add(executor);
            }

        } catch (IOException ex) {
            Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void appendLineToOutput(List<Integer> pixels, String fileName) {
        try {
            String path = String.valueOf(mainSettingsFile.get("images_path")).replace("\\images\\split_images\\ir_images", "\\datasets\\" + fileName);
            String content = "";
            for (int i = 0; i < pixels.size(); i++) {
                content += pixels.get(i);
                content += ",";
            }
            content += "*";
            content = content.replace(",*", "\n");

            Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
