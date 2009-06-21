/*
 * TextureUtils.java
 *=====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * Created on September 23, 2007, 1:13 PM
 *
 * Collection of static methods dealing with JOGL textures
 */

package name.gano.worldwind.texture;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import javax.media.opengl.GL;

/**
 *
 * @author Shawn
 */
public class TextureUtils
{
    
    public static Texture loadTexture(String fnm)
    {
        String fileName = "images/" + fnm;
        Texture tex = null;
        try
        {
            tex = TextureIO.newTexture( new File(fileName), false);
            tex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            tex.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        }
        catch(Exception e)
        { 
            System.out.println("Error loading texture " + fileName);  
            e.printStackTrace();
        }
        
        return tex;
    }  // end of loadTexture()

    
}
