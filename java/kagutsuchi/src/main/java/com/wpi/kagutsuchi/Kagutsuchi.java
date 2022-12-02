package com.wpi.kagutsuchi;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Kagutsuchi {

    public static void main(String[] args) {
        JFrameWindow window = new JFrameWindow();
        window.pack();
        window.setVisible(true);
        /*
        try {     
            BufferedImage image = ImageIO.read(new File("left_img_1.jpg"));
            BufferedImage theImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    int rgb = 255;
                    rgb = (rgb << 8) + 255;
                    rgb = (rgb << 8) + 255;
                    theImage.setRGB(i, j, image.getRGB(i, j));
                }
                
            }
            File outputFile = new File("output.jpg");
            ImageIO.write(theImage, "jpg", outputFile);
            
   
        } catch (IOException ex) {
            Logger.getLogger(Kagutsuchi.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }
}
