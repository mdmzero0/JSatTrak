/*
 * SatelliteProps.java
 *=====================================================================
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
 * Created on July 25, 2007, 3:14 PM
 *
 * Holds TLE and all satellite properties
 */

package jsattrak.objects;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.awt.Color;
import java.util.Random;
import javax.swing.JOptionPane;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.propogators.sdp4.SDP4;
import jsattrak.utilities.TLE;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;

/**
 * 
 *
 * @author ganos
 */
public class SatelliteTleSGP4 extends AbstractSatellite
{
    private TLE tle;
    private SDP4 sdp4Prop; // propogator
    
    // current time - julian date
    double currentJulianDate = -1;
    
    // TLE epoch -- used to calculate how old is TLE - Julian Date
    double tleEpochJD = -1; // no age
    
    // J2000 position and velocity vectors
    private double[] j2kPos = new double[3];
    private double[] j2kVel = new double[3];
    // MOD Mean of Date (or actually mean of Epoch Date)
    private double[] posMOD = new double[3];  // mean of date position for LLA calcs
    private double[] velMOD = new double[3];
    
    // lat,long,alt  [radians, radians, km/m ?]
    private double[] lla = new double[3];
    
    // plot options 
    private boolean plot2d = true;
    private Color satColor = Color.RED; // randomize in future
    private boolean plot2DFootPrint = true;
    private boolean fillFootPrint = true;
    private int numPtsFootPrint = 101; // number of points in footprint
    
    // ground track options  -- grounds tracks draw to asending nodes, re-calculated at acending nodes
    boolean showGroundTrack = true;
    private int grnTrkPointsPerPeriod = 121; // equally space in time >=2
    private double groundTrackLeadPeriodMultiplier = 2.0;  // how far forward to draw ground track - in terms of periods
    private double groundTrackLagPeriodMultiplier = 1.0;  // how far behind to draw ground track - in terms of periods
    double[][] latLongLead; // leading lat/long coordinates for ground track
    double[][] latLongLag; // laging lat/long coordinates for ground track
    private double[][] modPosLead; // leading Mean of date position coordinates for ground track
    private double[][] modPosLag; // laging Mean of date position coordinates for ground track
    private double[]   timeLead; // array for holding times associated with lead coordinates (Jul Date)
    private double[]   timeLag; // array - times associated with lag coordinates (Jul Date)
    boolean groundTrackIni = false; // if ground track has been initialized    
    
    private boolean showName2D = true; // show name in 2D plots
    
    // 3D Options
    private boolean show3DOrbitTrace = true;
    private boolean show3DFootprint = true;
    private boolean show3DName = true; // not implemented to change yet
    private boolean show3D = true; // no implemented to change yet, or to modify showing of sat
    private boolean showGroundTrack3d = false;
    private boolean show3DOrbitTraceECI = true; // show orbit in ECI mode otherwise , ECEF
    
        // 3D model parameters
    private boolean use3dModel = false; // use custom 3D model (or default sphere)
    private String threeDModelPath = ""; // path to the custom model
    WWModel3D_new threeDModel;
    
    /** Creates a new instance of SatelliteProps - default properties with given name and TLE lines
     * @param name name of satellite
     * @param tleLine1 first line of two line element
     * @param tleLine2 second line of two line element
     */
    public SatelliteTleSGP4(String name, String tleLine1, String tleLine2)
    {
        // create internal TLE object
        tle = new TLE(name,tleLine1,tleLine2);
        
        // initialize sdp4 propogator
        sdp4Prop = new SDP4();
        sdp4Prop.Init();
        
        // randomly pick color for satellite
        // === pick a random color
        Random generator = new Random();
        switch( generator.nextInt(6) )
        {
            case 0: satColor = Color.red; break;
            case 1: satColor = Color.blue; break;
            case 2: satColor = Color.green; break;
            case 3: satColor = Color.white; break;
            case 4: satColor = Color.yellow; break;
            case 5: satColor = Color.orange; break;
            default: satColor = Color.red; break;
        } // random color switch
        
        
        // try to load TLE into propogator
        try
        {
            sdp4Prop.NoradLoadTLE(name, tleLine1,tleLine2);  // call to load TLE from strings (internal)
             
            // calculate TLE age
            tleEpochJD = sdp4Prop.itsEpochJD+2450000;
            
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error Reading TLE from Satellite: " + name);
            e.printStackTrace();
        }
    }
    
    public void updateTleData(TLE newTLE)
    {
        this.tle = newTLE; // save new TLE
        
        // new spg4 object
        sdp4Prop = new SDP4();
        sdp4Prop.Init();
        
        // read TLE
        try
        {
            sdp4Prop.NoradLoadTLE(tle.getSatName(), tle.getLine1(),tle.getLine2());  // call to load TLE from strings (internal)
             
            // calculate TLE age
            tleEpochJD = sdp4Prop.itsEpochJD+2450000;
            
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error Updating TLE data in current Satellite: " + tle.getSatName());
            e.printStackTrace();
        }
        
        // ground track needs to be redone with new data
        groundTrackIni = false;
        
        //System.out.println("Updated " + tle.getSatName() );
    }
    
    public void propogate2JulDate(double julDate)
    {
        // save date
        this.currentJulianDate = julDate;
        
        // propogate satellite to given date
        sdp4Prop.GetPosVelJulDate(julDate);  //okay using JulDate because function uses time diff between jultDate of ephemeris
        
        //print differene TT-UT
        //System.out.println("TT-UT [days]= " + SDP4TimeUtilities.DeltaT(julDate-2450000)*24.0*60*60);
        
        // get position information back out - convert to J2000
        j2kPos = CoordinateConversion.EquatorialEquinoxToJ2K(julDate-2400000.5, sdp4Prop.itsR);
        j2kVel = CoordinateConversion.EquatorialEquinoxToJ2K(julDate-2400000.5, sdp4Prop.itsV);
        
        //System.out.println("Date: " + julDate +", Pos: " + sdp4Prop.itsR[0] + ", " + sdp4Prop.itsR[1] + ", " + sdp4Prop.itsR[2]);
        
        
        // correct scaling factor of lenths
        
        for(int i=0;i<3;i++)
        {
            // J2000
            j2kPos[i] = j2kPos[i]*1000000000.0;
            j2kVel[i] = j2kVel[i]*1000.0;
            // MOD
             posMOD[i] = sdp4Prop.itsR[i]*1000000000.0;
             velMOD[i] = sdp4Prop.itsV[i]*1000.0;
        }
        
        // save old lat/long for ascending node check
        double[] oldLLA = lla.clone(); // copy old LLA
        
        // calculate Lat,Long,Alt - must use Mean of Date (MOD) Position
        lla = GeoFunctions.GeodeticLLA(posMOD,julDate-AstroConst.JDminusMJD); // j2kPos
        
        // Check to see if the ascending node has been passed
        if(showGroundTrack==true)
        {
            if(groundTrackIni == false ) // update ground track needed
            {
                initializeGroundTrack();
            }
            else if( oldLLA[0] < 0 && lla[0] >=0) // check for ascending node pass
            {
                //System.out.println("Ascending NODE passed: " + tle.getSatName() );
                initializeGroundTrack(); // for new ini each time
                
            } // ascending node passed
            
        } // if show ground track is true
               
    } // propogate2JulDate
    
    
    
    // initalize the ground track from any starting point, as long as Juldate !=-1
    private void initializeGroundTrack()
    {
        if(currentJulianDate == -1)
        {
            // nothing to do yet, we haven't been given an initial time
            return;
        }
        
        // find time of last acending node crossing
        
        // initial guess -- the current time        
        double lastAscendingNodeTime = currentJulianDate; // time of last ascending Node Time
        
        // calculate period - in minutes
        double periodMin = Kepler.CalculatePeriod(AstroConst.GM_Earth,j2kPos,j2kVel)/(60.0);
        //System.out.println("period [min] = "+periodMin);
        
        // time step divisions (in fractions of a day)
        double fracOfPeriod = 15.0;
        double timeStep = (periodMin/(60.0*24.0)) / fracOfPeriod;
        
        // first next guess
        double newGuess1 = lastAscendingNodeTime - timeStep;
        
        // latitude variables
        double lat0 =  lla[0]; //  current latitude
        double lat1 = (calculateLatLongAltXyz(newGuess1))[0]; // calculate latitude values       
        
        // bracket the crossing using timeStep step sizes
        while( !( lat0>=0 && lat1<0 ) )
        {
            // move back a step
            lastAscendingNodeTime = newGuess1;
            lat0 = lat1;
            
            // next guess
            newGuess1 = lastAscendingNodeTime - timeStep;
            
            // calculate latitudes of the new value
            lat1 = (calculateLatLongAltXyz(newGuess1))[0];
        } // while searching for ascending node
        
              
        // secand method -- determine within a second!
        double outJul = secantMethod(lastAscendingNodeTime-timeStep, lastAscendingNodeTime, 1.0/(60.0*60.0*24.0), 20);
        //System.out.println("Guess 1:" + (lastAscendingNodeTime-timeStep) );
        //System.out.println("Guess 2:" + (lastAscendingNodeTime));
        //System.out.println("Answer: " + outJul);
        
        // update times: Trust Period Calculations for how far in the future and past to calculate out to
        // WARNING: period calculation is based on osculating elements may not be 100% accurate
        //          as this is just for graphical updates should be okay (no mid-course corrections assumed)
        lastAscendingNodeTime = outJul;
        double leadEndTime = lastAscendingNodeTime + groundTrackLeadPeriodMultiplier*periodMin/(60.0*24); // Julian Date for last lead point (furthest in future)
        double lagEndTime = lastAscendingNodeTime - groundTrackLagPeriodMultiplier*periodMin/(60.0*24); // Julian Date for the last lag point (furthest in past)
        
        // fill in lead/lag arrays
        fillGroundTrack(lastAscendingNodeTime,leadEndTime,lagEndTime);
        
        groundTrackIni = true;
        return;
        
    } // initializeGroundTrack
    
    // fill in the Ground Track given Jul Dates for 
    // 
    private void fillGroundTrack(double lastAscendingNodeTime, double leadEndTime, double lagEndTime)
    {
        // points in the lead direction
        int ptsLead = (int)Math.ceil(grnTrkPointsPerPeriod*groundTrackLeadPeriodMultiplier);
        latLongLead = new double[ptsLead][3];        
        modPosLead =  new double[ptsLead][3];
        timeLead = new double[ptsLead];
                
        for(int i=0;i<ptsLead;i++)
        {
            double ptTime = lastAscendingNodeTime + i*(leadEndTime-lastAscendingNodeTime)/(ptsLead-1);
            
           // PUT HERE calculate lat lon
            double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);
            
            latLongLead[i][0] = ptLlaXyz[0]; // save lat
            latLongLead[i][1] = ptLlaXyz[1]; // save long
            latLongLead[i][2] = ptLlaXyz[2]; // save altitude
            
            modPosLead[i][0] = ptLlaXyz[3]; // x
            modPosLead[i][1] = ptLlaXyz[4]; // y
            modPosLead[i][2] = ptLlaXyz[5]; // z
            
            timeLead[i] = ptTime; // save time
            
        } // for each lead point
        
        // points in the lag direction
        int ptsLag = (int)Math.ceil(grnTrkPointsPerPeriod*groundTrackLagPeriodMultiplier);
        latLongLag = new double[ptsLag][3];
        modPosLag = new double[ptsLag][3];
        timeLag = new double[ptsLag];
        
        for(int i=0;i<ptsLag;i++)
        {
            double ptTime = lastAscendingNodeTime + i*(lagEndTime-lastAscendingNodeTime)/(ptsLag-1);
            
            double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);
             
            latLongLag[i][0] = ptLlaXyz[0]; // save lat
            latLongLag[i][1] = ptLlaXyz[1]; // save long
            latLongLag[i][2] = ptLlaXyz[2]; // save alt
            
            modPosLag[i][0] = ptLlaXyz[3]; // x
            modPosLag[i][1] = ptLlaXyz[4]; // y
            modPosLag[i][2] = ptLlaXyz[5]; // z
            
            timeLag[i] = ptTime;
            
        } // for each lag point
    } // fillGroundTrack
    
    // takes in JulDate
    private double[] calculateLatLongAltXyz(double ptTime)
    {
        sdp4Prop.GetPosVelJulDate(ptTime); // use sdp4 to prop to that time
        
        double[] ptPos = new double[3];
        
          
        // correct scaling factor of lenths
        for(int j=0;j<3;j++)
        {
            ptPos[j] = sdp4Prop.itsR[j]*1000000000.0; // lat/long calcualted using MOD position
        }
        
        // get lat and long
        double[] ptLla = GeoFunctions.GeodeticLLA(ptPos,ptTime-AstroConst.JDminusMJD);
        
        double[] ptLlaXyz = new double[] {ptLla[0],ptLla[1],ptLla[2],ptPos[0],ptPos[1],ptPos[2]};
        
        return ptLlaXyz;
    } // calculateLatLongAlt
    
    // 
    
    /**
     * Calculate J2K position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    public double[] calculateJ2KPositionFromUT(double julDate)
    {
        sdp4Prop.GetPosVelJulDate(julDate); // use sdp4 to prop to that time
        
        double[] j2kPosI = CoordinateConversion.EquatorialEquinoxToJ2K(julDate-2400000.5, sdp4Prop.itsR);
        
          
        // correct scaling factor of lenths
        for(int j=0;j<3;j++)
        {
            j2kPosI[j] = j2kPosI[j]*1000000000.0; // j2k position in meters
        }
        
        return j2kPosI;
        
    } // calculatePositionFromUT
    
    /**
     * Calculate MOD position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    public double[] calculateMODPositionFromUT(double julDate)
    {
        sdp4Prop.GetPosVelJulDate(julDate); // use sdp4 to prop to that time
                 
        double[] ptPos = new double[3];
        
        // correct scaling factor of lenths
        for(int j=0;j<3;j++)
        {
            ptPos[j] = sdp4Prop.itsR[j]*1000000000.0; // j2k position in meters
        }
        
        return ptPos;
        
    } // calculatePositionFromUT
    
    
    //---------------------------------------
    //  SECANT Routines to find Crossings of the Equator (hopefully Ascending Nodes)
    // xn_1 = date guess 1
    // xn date guess 2
    // tol = convergence tolerance
    // maxIter = maximum iterations allowed
    // RETURNS: double = julian date of crossing
    private double secantMethod(double xn_1, double xn, double tol, int maxIter)
    {

        double d;
        
        // calculate functional values at guesses
        double fn_1 = latitudeGivenJulianDate(xn_1);
        double fn = latitudeGivenJulianDate(xn);
        
        for (int n = 1; n <= maxIter; n++)
        {
            d = (xn - xn_1) / (fn - fn_1) * fn;
            if (Math.abs(d) < tol) // convergence check
            {
                //System.out.println("Iters:"+n);
                return xn;
            }
            
            // save past point
            xn_1 = xn;
            fn_1 = fn;
            
            // new point
            xn = xn - d;
            fn = latitudeGivenJulianDate(xn);
        }
        
        System.out.println("Warning: Secant Method - Max Iteration limit reached finding Asending Node.");
        
        return xn;
    } // secantMethod
    
    private double latitudeGivenJulianDate(double julDate)
    {
        // computer latiude of the spacecraft at a given date
        sdp4Prop.GetPosVelJulDate(julDate); // use sdp4 to prop to that time
        
        double[] ptPos = new double[3];
        
        // convert to J2000 coordinates
        //ptPos = CoordinateConversion.EquatorialEquinoxToJ2K(julDate-2400000.5, sdp4Prop.itsR);
        //SDP4TimeUtilities.Mean2J2000JulDate(1, julDate, sdp4Prop.itsR, ptPos )
        // LLA is based on MOD positions so don't convert to J2K
                
        // correct scaling factor of lenths
        for(int j=0;j<3;j++)
        {
            ptPos[j] =sdp4Prop.itsR[j]*1000000000.0;
        }
        
        // get lat and long
        double[] ptLla = GeoFunctions.GeodeticLLA(ptPos,julDate-AstroConst.JDminusMJD);
        
        return ptLla[0]; // pass back latitude
        
    } // latitudeGivenJulianDate

    //--------------------------------------
    
    public void setShowGroundTrack(boolean showGrndTrk)
    {
        showGroundTrack = showGrndTrk;
        
        if(showGrndTrk == false)
        {
            groundTrackIni = false; 
            latLongLead = new double[][] {{}}; // save some space
            latLongLag = new double[][] {{}}; // sace some space
            modPosLag = new double[][] {{}};
            modPosLead = new double[][] {{}};
            timeLead = new double[] {};
            timeLag = new double[] {};
        }
        else
        {
            // ground track needs to be initalized
            initializeGroundTrack();
        }
    }
    
    public boolean getShowGroundTrack()
    {
        return showGroundTrack;
    }
    
    public double getLatitude()
    {
        return lla[0];
    }
    
    public double getLongitude()
    {
        return lla[1];
    }
    
    public double getAltitude()
    {
        return lla[2];
    }
    
    public double[] getLLA()
    {
        return lla;
    }
    
    // TT or UTC?
    public double getSatTleEpochJulDate()
    {
        return (sdp4Prop.itsEpochJD + 2450000.0); // this is how the dates are defined in SDP4
    }
    
    public double getCurrentJulDate()
    {
        return currentJulianDate;
    }
    
    public double[] getJ2000Position()
    {
        return j2kPos;
    }
    
    public double[] getJ2000Velocity()
    {
        return j2kVel;
    }
    
    public boolean getPlot2D()
    {
        return plot2d;
    }
    
    public Color getSatColor()
    { 
        return satColor;
    }
    
    public boolean getPlot2DFootPrint()
    {
        return plot2DFootPrint;
    }
    
    public boolean getGroundTrackIni()
    {
        return groundTrackIni;
    }
    
    public void setGroundTrackIni2False()
    {
        // forces repaint of ground track next update
        groundTrackIni = false;
    }
    
    public int getNumGroundTrackLeadPts()
    {
        return latLongLead.length;
    }
        
    public int getNumGroundTrackLagPts()
    {
        return latLongLag.length;
    }
        
    public double[] getGroundTrackLlaLeadPt(int index)
    {
        return new double[] {latLongLead[index][0],latLongLead[index][1],latLongLead[index][2]};
    }
    
    public double[] getGroundTrackLlaLagPt(int index)
    {
        return new double[] {latLongLag[index][0],latLongLag[index][1],latLongLag[index][2]};
    }
    
    public double[] getGroundTrackXyzLeadPt(int index)
    {
        return new double[] {getModPosLead()[index][0],getModPosLead()[index][1],getModPosLead()[index][2]};
    }
    
    public double[] getGroundTrackXyzLagPt(int index)
    {
        return new double[] {getModPosLag()[index][0],getModPosLag()[index][1],getModPosLag()[index][2]};
    }
    
    
    
    // returns satellite's current perdiod based on current pos/vel in Minutes
    public double getPeriod()
    {
        return Kepler.CalculatePeriod(AstroConst.GM_Earth,j2kPos,j2kVel)/(60.0);
    }
    
    public String getName()
    {
        return tle.getSatName();
    }
    
    public double[] getKeplarianElements()
    {
        return Kepler.SingularOsculatingElements( AstroConst.GM_Earth, j2kPos, j2kVel ); 
    }
    
    public double getTleEpochJD()
    {
        return tleEpochJD;
    }
    
    public double getTleAgeDays()
    {
        return currentJulianDate - tleEpochJD;
    }

    public int getNumPtsFootPrint()
    {
        return numPtsFootPrint;
    }

    public void setNumPtsFootPrint(int numPtsFootPrint)
    {
        this.numPtsFootPrint = numPtsFootPrint;
    }

    public boolean isShowName2D()
    {
        return showName2D;
    }

    public void setShowName2D(boolean showName2D)
    {
        this.showName2D = showName2D;
    }

    public boolean isFillFootPrint()
    {
        return fillFootPrint;
    }

    public void setFillFootPrint(boolean fillFootPrint)
    {
        this.fillFootPrint = fillFootPrint;
    }

    public int getGrnTrkPointsPerPeriod()
    {
        return grnTrkPointsPerPeriod;
    }

    public void setGrnTrkPointsPerPeriod(int grnTrkPointsPerPeriod)
    {
        this.grnTrkPointsPerPeriod = grnTrkPointsPerPeriod;
    }

    public double getGroundTrackLeadPeriodMultiplier()
    {
        return groundTrackLeadPeriodMultiplier;
    }

    public void setGroundTrackLeadPeriodMultiplier(double groundTrackLeadPeriodMultiplier)
    {
        this.groundTrackLeadPeriodMultiplier = groundTrackLeadPeriodMultiplier;
    }

    public double getGroundTrackLagPeriodMultiplier()
    {
        return groundTrackLagPeriodMultiplier;
    }

    public void setGroundTrackLagPeriodMultiplier(double groundTrackLagPeriodMultiplier)
    {
        this.groundTrackLagPeriodMultiplier = groundTrackLagPeriodMultiplier;
    }

    public void setPlot2d(boolean plot2d)
    {
        this.plot2d = plot2d;
    }

    public void setSatColor(Color satColor)
    {
        this.satColor = satColor;
    }

    public void setPlot2DFootPrint(boolean plot2DFootPrint)
    {
        this.plot2DFootPrint = plot2DFootPrint;
    }

    public double[] getPosMOD()
    {
        return posMOD;
    }

    public boolean isShow3DOrbitTrace()
    {
        return show3DOrbitTrace;
    }

    public void setShow3DOrbitTrace(boolean show3DOrbitTrace)
    {
        this.show3DOrbitTrace = show3DOrbitTrace;
    }

    public boolean isShow3DFootprint()
    {
        return show3DFootprint;
    }

    public void setShow3DFootprint(boolean show3DFootprint)
    {
        this.show3DFootprint = show3DFootprint;
    }

    public boolean isShow3DName()
    {
        return show3DName;
    }

    public void setShow3DName(boolean show3DName)
    {
        this.show3DName = show3DName;
    }

    public boolean isShowGroundTrack3d()
    {
        return showGroundTrack3d;
    }

    public void setShowGroundTrack3d(boolean showGroundTrack3d)
    {
        this.showGroundTrack3d = showGroundTrack3d;
    }

    public boolean isShow3DOrbitTraceECI()
    {
        return show3DOrbitTraceECI;
    }

    public void setShow3DOrbitTraceECI(boolean show3DOrbitTraceECI)
    {
        this.show3DOrbitTraceECI = show3DOrbitTraceECI;
    }

    public boolean isShow3D()
    {
        return show3D;
    }

    public void setShow3D(boolean show3D)
    {
        this.show3D = show3D;
    }

    public // laging lat/long coordinates for ground track
    double[][] getModPosLead()
    {
        return modPosLead;
    }

    public // leading Mean of date position coordinates for ground track
    double[][] getModPosLag()
    {
        return modPosLag;
    }

    public // laging Mean of date position coordinates for ground track
    double[] getTimeLead()
    {
        return timeLead;
    }

    public // array for holding times associated with lead coordinates (Jul Date)
    double[] getTimeLag()
    {
        return timeLag;
    }
    
    // 3D model -------------------------
    public boolean isUse3dModel()
    {
        return use3dModel; 
    }
    
    public void setUse3dModel(boolean use3dModel)
    {
        this.use3dModel = use3dModel;
        
        if(use3dModel && threeDModel==null)
        {
            String path = "data/models/globalstar/Globalstar.3ds";
            //String path = "data/models/genesis/genesis.3ds";
            if(threeDModelPath.length()>0)
            {
                path = threeDModelPath;
            }
            loadNewModel(path);
        }
    }
    
    public String getThreeDModelPath()
    {
        return threeDModelPath;
    }
    
    public void setThreeDModelPath(String path)
    {
        if(use3dModel && !(path.equalsIgnoreCase(this.threeDModelPath)) )
        {
            // need to load the model
            loadNewModel(path);//"test/data/globalstar/Globalstar.3ds");
        }
        
        this.threeDModelPath = path; // save path no matter
    }
    
    private void loadNewModel(String path)
    {
        try
            {
                net.java.joglutils.model.geometry.Model model3DS = ModelFactory.createModel(path);
                model3DS.setUseLighting(false); // turn off lighting!

                threeDModel =  new WWModel3D_new(model3DS,
                        new Position(Angle.fromRadians(this.getLatitude()),
                        Angle.fromRadians(this.getLongitude()),
                        this.getAltitude()));

                threeDModel.setMaitainConstantSize(true);
                threeDModel.setSize(300000); // this needs to be a property!
            }catch(Exception e)
            {
                System.out.println("ERROR LOADING 3D MODEL");
            }
    }
    
    public WWModel3D_new getThreeDModel()
    {
        return threeDModel;
    }    
    
    public  double[] getMODVelocity()
    {
        return velMOD.clone();
    }
    
} // SatelliteProps
