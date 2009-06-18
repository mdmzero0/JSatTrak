
package name.gano.astro.propogators.sgp4_cssi;

import name.gano.astro.MathUtils;

/**
 *
 * @author sgano
 */
public class TestSGP4_CSSI
{
    public static void main(String[] args)
    {
        // new sat data object
        SGP4SatData data = new SGP4SatData();

        // tle data
        String name = "ISS (ZARYA)";
        String line1 = "1 25544U 98067A   09161.51089941  .00015706  00000-0  11388-3 0   112";
        String line2 = "2 25544  51.6406 341.1646 0009228  98.8703 312.6668 15.73580432604904";

        // options
        char opsmode = SGP4io.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

        // read in data and ini SGP4 data
        boolean result1 = SGP4io.readTLEandIniSGP4(name, line1, line2, opsmode, gravconsttype, data);
        if(!result1)
        {
            System.out.println("Error Reading / Ini Data, error code: " + data.error);
            return;
        }

        // prop to a given date
        double propJD = 2454994.0; // JD to prop to
        double minutesSinceEpoch = (propJD-data.jdsatepoch)*24.0*60.0;
        double[] pos = new double[3];
        double[] vel = new double[3];

        boolean result = SGP4unit.sgp4(data, minutesSinceEpoch, pos, vel);
        if(!result1)
        {
            System.out.println("Error in Sat Prop");
            return;
        }

        // output
        System.out.println("Epoch of TLE (JD): " + data.jdsatepoch);
        System.out.println(minutesSinceEpoch + ", " + pos[0]+ ", " + pos[1]+ ", " + pos[2]+ ", " + vel[0]+ ", " + vel[1]+ ", " + vel[2]);

        double[] stkResults = new double[] {-2881017.428533447,-3207508.188455666,-5176685.907342243};
        double[] cCodeResults = new double[] {}; //? Vallado's C Code comparison
        double dX = MathUtils.norm( MathUtils.sub(MathUtils.scale(pos,1000.0), stkResults) );
        System.out.println("Error from STk (m) : " + dX);

    }
}
