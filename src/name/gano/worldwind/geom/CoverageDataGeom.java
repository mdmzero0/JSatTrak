/**
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
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

package name.gano.worldwind.geom;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import javax.media.opengl.GL;
import jsattrak.coverage.CoverageAnalyzer;

/**
 *
 * @author sgano
 */
public class CoverageDataGeom implements Renderable 
{
    
//    Globe globe;
    CoverageAnalyzer ca;
    
    public CoverageDataGeom(CoverageAnalyzer ca)
    {
        this.ca = ca;
    }
    
//    public CoverageDataGeom(Globe globe)
//    {
//        this.globe = globe;
//    }
    
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        javax.media.opengl.GL gl = dc.getGL();
        
        gl.glEnable(GL.GL_TEXTURE_2D);        
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
              
      
            Color satColor = Color.white;
            double alpha = 0.1;
            gl.glColor4d(satColor.getRed()/255.0 , satColor.getGreen()/255.0 , satColor.getBlue()/255.0,alpha ); // COLOR
            
            Double nanDbl = new Double(Double.NaN);
            
            double alt = 100000;
            
            if( ca != null)
            {
            
            for(int i=0;i<ca.getLongPanels();i++)
            {
                for(int j=0;j<ca.getLatPanels();j++)
                {
                    Vec4 pos1 = dc.getGlobe().computePointFromPosition(
                            Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                            Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                            alt);
                    Vec4 pos2 = dc.getGlobe().computePointFromPosition(
                            Angle.fromDegrees(ca.getLatGridPoints()[j+1]), // lat
                            Angle.fromDegrees(ca.getLonGridPoints()[i]), // lon
                            alt);
                    Vec4 pos3 = dc.getGlobe().computePointFromPosition(
                            Angle.fromDegrees(ca.getLatGridPoints()[j+1]), // lat
                            Angle.fromDegrees(ca.getLonGridPoints()[i+1]), // lon
                            alt);
                    Vec4 pos4 = dc.getGlobe().computePointFromPosition(
                            Angle.fromDegrees(ca.getLatGridPoints()[j]), // lat
                            Angle.fromDegrees(ca.getLonGridPoints()[i + 1]), // lon
                            alt);
                    
                    satColor = ca.getColorForIndex(j, i);
                    gl.glColor4d(satColor.getRed()/255.0 , satColor.getGreen()/255.0 , satColor.getBlue()/255.0,alpha ); // COLOR

                    gl.glBegin(GL.GL_QUADS);  // counter clock wise?
                    //gl.glTexCoord2f(0, 0);

                    gl.glVertex3d(pos1.x, pos1.y, pos1.z);
                    //gl.glTexCoord2f(0, 1);
                    gl.glVertex3d(pos2.x, pos2.y, pos2.z);
                    //gl.glTexCoord2f(1, 1);
                    gl.glVertex3d(pos3.x, pos3.y, pos3.z);
                    //gl.glTexCoord2f(1, 0);
                    gl.glVertex3d(pos4.x, pos4.y, pos4.z);
                    gl.glEnd();
                }
            }
            }
            
            double latlon = 5;
            //double alt = 100000;
            
           Vec4 pos1 = dc.getGlobe().computePointFromPosition(
                Angle.fromDegrees(-latlon), // lat
                Angle.fromDegrees(latlon), // lon
                alt);
           Vec4 pos2 = dc.getGlobe().computePointFromPosition(
                Angle.fromDegrees(-latlon), // lat
                Angle.fromDegrees(-latlon), // lon
                alt);
           Vec4 pos3 = dc.getGlobe().computePointFromPosition(
                Angle.fromDegrees(latlon), // lat
                Angle.fromDegrees(-latlon), // lon
                alt);
           Vec4 pos4 = dc.getGlobe().computePointFromPosition(
                Angle.fromDegrees(latlon), // lat
                Angle.fromDegrees(latlon), // lon
                alt);
           
           gl.glBegin(GL.GL_QUADS);  // counter clock wise?
        //gl.glTexCoord2f(0, 0);
        gl.glVertex3d(pos1.x,pos1.y,pos1.z);
        //gl.glTexCoord2f(0, 1);
        gl.glVertex3d(pos2.x,pos2.y,pos2.z);
        //gl.glTexCoord2f(1, 1);
        gl.glVertex3d(pos3.x,pos3.y,pos3.z);
        //gl.glTexCoord2f(1, 0);
        gl.glVertex3d(pos4.x,pos4.y,pos4.z);
        gl.glEnd();
            
         float SIZE = 12000000;
        gl.glBegin(GL.GL_QUADS);  // counter clock wise?
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(-SIZE / 2, SIZE / 2, 0);
        gl.glTexCoord2f(0, 1);
        gl.glVertex3f(-SIZE / 2, -SIZE / 2, 0);
        gl.glTexCoord2f(1, 1);
        gl.glVertex3f(SIZE / 2, -SIZE / 2, 0);
        gl.glTexCoord2f(1, 0);
        gl.glVertex3f(SIZE / 2, SIZE / 2, 0);
        gl.glEnd();
            
          
//                // plot lag orbit
//                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
//                for (int i = 0; i < sat.getNumGroundTrackLagPts(); i++)
//                {
//                    // add next Mean of Date vertex
//                    double[] xyz = sat.getGroundTrackXyzLagPt(i);
//                    if(!nanDbl.equals(xyz[0])) // NaN check
//                    {
//                        gl.glVertex3d(-xyz[0], xyz[2], xyz[1]);
//                    }
//                }
//                gl.glEnd();


        
        gl.glPopAttrib();
        
     } // render
}
