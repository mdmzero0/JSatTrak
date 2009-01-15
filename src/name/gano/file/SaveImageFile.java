/*
 * SaveImageFile.java
 * Utility class to help save images to a file -- lets you specify compression if the format supports it
 * 
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */

package name.gano.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author Shawn Gano, 13 November 2008
 */
public class SaveImageFile 
{
    public static float DEFAULT_COMPRESSION = 0.75f; 
    
    /**
     * saves image, returns Eception if everything went fine this is null, otherwise it is the exception
     * @param format image type, e.g.: jpg, jpeg, gif, png
     * @param file what to save the image as
     * @param buff image
     * @return
     */
    public static Exception saveImage(String format,File file, BufferedImage buff)
    {  
        return saveImage(format,file, buff, SaveImageFile.DEFAULT_COMPRESSION);
    }
    
    /**
     * saves image, returns Eception if everything went fine this is null, otherwise it is the exception
     * @param format image type, e.g.: jpg, jpeg, gif, png
     * @param file what to save the image as
     * @param buff image
     * @param compressionQuality  0.0f-1.0f , 1 = best quality
     * @return
     */
    public static Exception saveImage(String format,File file, BufferedImage buff, float compressionQuality)
    {        
        Iterator iter = ImageIO.getImageWritersByFormatName(format);
        
        ImageWriter writer = (ImageWriter)iter.next();
        // instantiate an ImageWriteParam object with default compression options
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        
        if(format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("JPEG") )
        {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(compressionQuality);   // an integer between 0 and 1
        // 1 specifies minimum compression and maximum quality
        }
        
        // write file
        try
        {
            FileImageOutputStream output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage image = new IIOImage(buff, null, null);
            writer.write(null, image, iwp);
            output.close(); // Fixed SEG - 22 Dec 2008
        }
        catch(Exception e)
        {
            return e;
        }
        
        // everything went smoothly
        return null;
    }

}
