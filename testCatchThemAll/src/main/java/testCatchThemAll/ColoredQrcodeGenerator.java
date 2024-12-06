package testCatchThemAll;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class ColoredQrcodeGenerator {
	public static void main(String[] args) throws WriterException, IOException {
        String data = "https://maxime-ichoung.github.io/principal/19/ocean.html";

        // encode
        ByteMatrix matrix = generateMatrix(data);
        
        String pokemonName = "togepi";
        
        String imageFormat = "png";
        String outputFileName = "Z:/qrcode/"+ pokemonName + "." + imageFormat;
        String inputFileName = "Z:/silhouette/"+ pokemonName + ".png";
        Color color1 = new Color(0x589fb8);
        Color color2 = new Color(0xcf623c);
        int ratio = 3;
        int x = 150;
        int y = 270 ;

        // write in a file
        writeImage(outputFileName, inputFileName, color1, color2, ratio, x, y, imageFormat, matrix);
      
    }

    private static ByteMatrix generateMatrix(final String data) throws WriterException {
        QRCode qr = new QRCode();
        Encoder.encode(data, ErrorCorrectionLevel.L, qr);
        ByteMatrix matrix = qr.getMatrix();
        return matrix;
    }
    
    private static void writeImage(String outputFileName, String inputFileName, Color color1, Color color2, int ratio, int x, int y, String imageFormat, ByteMatrix matrix) throws IOException {

        Color transparent = new Color(0, 0, 0, Transparency.TRANSLUCENT);
        BufferedImage silhouette = ImageIO.read(new File(inputFileName));
        final int size = silhouette.getHeight();
        
        // Java 2D Traitement de Area
        // Futurs modules
        Area a = new Area();
        Area module = new Area(new Rectangle.Float(0, 0, 1, 1));

        // Deplacement du module
        AffineTransform at = new AffineTransform(); 
        int width = matrix.getWidth();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix.get(j, i) == 1) {
                    // Ajout du module
                    a.add(module);
                }
                // Decalage a droite
                at.setToTranslation(1, 0); 
                module.transform(at);
            }

            // Ligne suivante
            at.setToTranslation(-width, 1); 
            module.transform(at);
        }

        // Quietzone : 4 modules de bordures autour du QR Code (zone vide pour bien identifier le code dans la page imprimee)

        at.setToTranslation(1, 1);

        a.transform(at);

        // On agrandit le tour a la taille souhaitee.
        at.setToScale(ratio, ratio); 
        a.transform(at);
        
        int sizeQrCode = (width*ratio)+(ratio*2);
        BufferedImage imageQrCode = new BufferedImage(sizeQrCode, sizeQrCode, BufferedImage.TYPE_INT_RGB);
        Graphics2D gQrCode = (Graphics2D) imageQrCode.getGraphics();
        
        gQrCode.setBackground(color2);
        gQrCode.clearRect(0, 0, sizeQrCode, sizeQrCode);
        
        gQrCode.setPaint(color1);
        gQrCode.fill(a);
        
        // Java 2D Traitement l'image
        BufferedImage im = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) im.getGraphics();
        
        g.setBackground(transparent);
        g.clearRect(0, 0, size, size);
        g.drawImage(imageQrCode, x, y, null);
        
        Random rand = new Random();
        
        for (int i = 0; i < size; i+=ratio) {
            for (int j = 0; j < size; j+=ratio) {
            	if (im.getRGB(i, j) != color1.getRGB() && im.getRGB(i, j) != color2.getRGB()) {
                    Color randomColor = rand.nextBoolean() ? color1 : color2;
                    g.setColor(randomColor);
                    g.fillRect(i, j, ratio, ratio);
            	}
            }
        }

        // Libérer les ressources
        g.dispose();
        
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        
        // Parcourir chaque pixel de la silhouette
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int silhouettePixel = silhouette.getRGB(i, j);

                // Vérifier si le pixel de la silhouette est blanc (intérieur)
                if (silhouettePixel != Color.BLACK.getRGB()) {
                	result.setRGB(i, j, im.getRGB(i, j));
                }
            }
        }
        
        // Ecriture sur le disque
        File f = new File(outputFileName);
        f.setWritable(true);
        ImageIO.write(result, imageFormat, f);
        f.createNewFile();
    }
    
}
