package utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;

public class ImageCustom {
    public static ImageIcon resize(ImageIcon imageIcon, int width, int height){
        Image image = imageIcon.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }
    public static byte[] toByteArray(String path) throws IOException{
        return FileUtils.readFileToByteArray(new File(path));
    }
    public static String toStringBase64(String path) throws IOException{
        return Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File(path)));
    }
    public static String bytesToB64(byte[] image){
        return Base64.getEncoder().encodeToString(image);
    }
    public static byte[] B64ToBytes(String image){
        return Base64.getDecoder().decode(image);
    }
}