import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
 
public class OpacityGradient {
   
   final static int SIDE = 500;
   final static int MARGIN = 20;
   private JPanel panel;
   private BufferedImage topImage, bottomImage, mask;
   // Change for non-Windows sytems
   final static String PIC_DIR = "C:/Documents and Settings/All Users/" +
         "Documents/My Pictures/Sample Pictures";
   
   private File getFile() {
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new File(PIC_DIR));
      int retVal = chooser.showOpenDialog(null);
      if(retVal == JFileChooser.APPROVE_OPTION) {
         return chooser.getSelectedFile();
      }
      return null;
   }
   
   private void makePics() {
      File topFile = getFile();
      if (null == topFile) {
         System.exit(1);
      }
      
      File bottomFile = getFile();
      if (null == bottomFile) {
         System.exit(1);
      }
      
      mask = new BufferedImage(SIDE, SIDE,
            BufferedImage.TYPE_INT_ARGB);
      Graphics2D maskG2D = mask.createGraphics();
      
      Point center = new Point(SIDE / 2, SIDE / 2);
      int radius = SIDE / 2 - MARGIN;
      float[] fractions = {0.5f, 1.0f};
      Color[] colors = {Color.WHITE, new Color(0, true)};
      RadialGradientPaint paint = new RadialGradientPaint(center,
            radius, fractions, colors);
      maskG2D.setPaint(paint);
      maskG2D.fillOval(MARGIN, MARGIN, radius * 2, radius * 2);
      
      try {
         topImage = new BufferedImage(SIDE, SIDE,
               BufferedImage.TYPE_INT_ARGB);
         Graphics2D topG2D = topImage.createGraphics();
         topG2D.drawImage(ImageIO.read(topFile),
               0, 0, SIDE, SIDE, null);
         
         AlphaComposite ac =
               AlphaComposite.getInstance
               (AlphaComposite.DST_IN);
         topG2D.setRenderingHint
               (RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
         topG2D.setComposite(ac);
         topG2D.drawImage(mask, 0, 0, SIDE, SIDE, null);
         
         bottomImage = new BufferedImage(SIDE, SIDE,
               BufferedImage.TYPE_INT_RGB);
         Graphics2D bottomG2D = bottomImage.createGraphics();
         bottomG2D.drawImage(ImageIO.read(bottomFile),
               0, 0, SIDE, SIDE, null);
         
      } catch (IOException ex) {
         ex.printStackTrace();
         System.exit(1);
      }
   }
   
   public void makeUI() {
      makePics();
      ImagePanel panel = new ImagePanel();
      
      JFrame frame = new JFrame("OpacityGradient");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
   }
   
   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new OpacityGradient().makeUI();
         }
      });
   }
   class ImagePanel extends JPanel {
      
      ImagePanel() {
         setPreferredSize(new Dimension(SIDE, SIDE));
      }
      
      @Override
      protected void paintComponent(Graphics g) {
         //super.paintComponent(g);
         g.drawImage(bottomImage, 0, 0, this);
         g.drawImage(topImage, 0, 0, this);
      }
   }
}
