
package name.gano.astro.propogators.sgp4_cssi;

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
        char opsmode = SGP4io.OPSMODE_IMPROVED;
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs84;

        // read in data and ini SGP4 data
        boolean result1 = SGP4io.readTLEandIniSGP4(name, line1, line2, opsmode, gravconsttype, data);

        // prop to a given date
        double daysSinceEpoch = 0;
        double[] pos = new double[3];
        double[] vel = new double[3];

        boolean result = SGP4unit.sgp4(data, opsmode, pos, vel);

        // output
        System.out.println(daysSinceEpoch + ", " + pos[0]+ ", " + pos[1]+ ", " + pos[2]+ ", " + vel[0]+ ", " + vel[1]+ ", " + vel[2]);

    }
}
