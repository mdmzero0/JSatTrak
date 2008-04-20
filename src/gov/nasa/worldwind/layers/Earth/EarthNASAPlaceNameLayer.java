/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.placename.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Paul Collins
 * @version $Id: EarthNASAPlaceNameLayer.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class EarthNASAPlaceNameLayer extends PlaceNameLayer
{
    private static final double LEVEL_A = 0x1 << 25;
    private static final double LEVEL_B = 0x1 << 24;
    private static final double LEVEL_C = 0x1 << 23;
    private static final double LEVEL_D = 0x1 << 22;
    //  private static final double LEVEL_E = 0x1 << 21;
    private static final double LEVEL_F = 0x1 << 20;
    private static final double LEVEL_G = 0x1 << 19;
    //  private static final double LEVEL_H = 0x1 << 18;
    private static final double LEVEL_I = 0x1 << 17;
    private static final double LEVEL_J = 0x1 << 16;
    private static final double LEVEL_K = 0x1 << 15;
    private static final double LEVEL_L = 0x1 << 14;
    private static final double LEVEL_M = 0x1 << 13;
//  private static final double LEVEL_N = 0x1 << 12;

    private static final LatLon GRID_1x1 = new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d));
    //  private static final LatLon GRID_2x4 = new LatLon(Angle.fromDegrees(90d), Angle.fromDegrees(90d));
    private static final LatLon GRID_5x10 = new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d));
    private static final LatLon GRID_10x20 = new LatLon(Angle.fromDegrees(18d), Angle.fromDegrees(18d));
    private static final LatLon GRID_20x40 = new LatLon(Angle.fromDegrees(9d), Angle.fromDegrees(9d));

    public EarthNASAPlaceNameLayer()
    {
        super(makePlaceNameServiceSet());
    }

    private static PlaceNameServiceSet makePlaceNameServiceSet()
    {
        final String service = "http://worldwind25.arc.nasa.gov/geoservercache/geoservercache.aspx";
        final String fileCachePath = "Earth/NASA Geoserver Place Names";
        PlaceNameServiceSet placeNameServiceSet = new PlaceNameServiceSet();
        PlaceNameService placeNameService;

        // Oceans
        placeNameService = new PlaceNameService(service, "topp:wpl_oceans", fileCachePath, Sector.FULL_SPHERE, GRID_1x1,
            java.awt.Font.decode("Arial-BOLDITALIC-12"));
        placeNameService.setColor(new java.awt.Color(200, 200, 200));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_A);
        placeNameServiceSet.addService(placeNameService, false);
        // Continents
        placeNameService = new PlaceNameService(service, "topp:wpl_continents", fileCachePath, Sector.FULL_SPHERE,
            GRID_1x1,
            java.awt.Font.decode("Arial-BOLD-12"));
        placeNameService.setColor(new java.awt.Color(255, 255, 240));
        placeNameService.setMinDisplayDistance(LEVEL_G);
        placeNameService.setMaxDisplayDistance(LEVEL_A);
        placeNameServiceSet.addService(placeNameService, false);

        // Water Bodies
        placeNameService = new PlaceNameService(service, "topp:wpl_waterbodies", fileCachePath, Sector.FULL_SPHERE,
            GRID_5x10,
            java.awt.Font.decode("Arial-ITALIC-10"));
        placeNameService.setColor(java.awt.Color.cyan);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);
        // Trenches & Ridges
        placeNameService = new PlaceNameService(service, "topp:wpl_trenchesridges", fileCachePath, Sector.FULL_SPHERE,
            GRID_5x10,
            java.awt.Font.decode("Arial-BOLDITALIC-10"));
        placeNameService.setColor(java.awt.Color.cyan);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);
        // Deserts & Plains
        placeNameService = new PlaceNameService(service, "topp:wpl_desertsplains", fileCachePath, Sector.FULL_SPHERE,
            GRID_5x10,
            java.awt.Font.decode("Arial-BOLDITALIC-10"));
        placeNameService.setColor(java.awt.Color.orange);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);

        // Lakes & Rivers
        placeNameService = new PlaceNameService(service, "topp:wpl_lakesrivers", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-ITALIC-10"));
        placeNameService.setColor(java.awt.Color.cyan);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_C);
        placeNameServiceSet.addService(placeNameService, false);
        // Mountains & Valleys
        placeNameService = new PlaceNameService(service, "topp:wpl_mountainsvalleys", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-BOLDITALIC-10"));
        placeNameService.setColor(java.awt.Color.orange);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_C);
        placeNameServiceSet.addService(placeNameService, false);

        // Countries
        placeNameService = new PlaceNameService(service, "topp:countries", fileCachePath, Sector.FULL_SPHERE, GRID_5x10,
            java.awt.Font.decode("Arial-BOLD-10"));
        placeNameService.setColor(java.awt.Color.white);
        placeNameService.setMinDisplayDistance(LEVEL_G);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);
        // GeoNet World Capitals
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_pplc", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-BOLD-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);
        // US Cities (Population Over 500k)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover500k", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-BOLD-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (Population Over 100k)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover100k", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_F);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (Population Over 50k)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover50k", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_I);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (Population Over 10k)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover10k", fileCachePath, Sector.FULL_SPHERE,
            GRID_10x20,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_J);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (Population Over 1k)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover1k", fileCachePath, Sector.FULL_SPHERE,
            GRID_20x40,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_K);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (Population Over 0)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover0", fileCachePath, Sector.FULL_SPHERE,
            GRID_20x40,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_L);
        placeNameServiceSet.addService(placeNameService, false);

        // US Cities (No Population)
        placeNameService = new PlaceNameService(service, "topp:wpl_uscities0", fileCachePath, Sector.FULL_SPHERE,
            GRID_20x40,
            java.awt.Font.decode("Arial-PLAIN-10"));
        placeNameService.setColor(java.awt.Color.yellow);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_M);
        placeNameServiceSet.addService(placeNameService, false);

//        // US Anthropogenic Features
//        placeNameService = new PlaceNameService(service, "topp:wpl_us_anthropogenic", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-PLAIN-10"));
//        placeNameService.setColor(java.awt.Color.yellow);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);
//        // US Water Features
//        placeNameService = new PlaceNameService(service, "topp:wpl_us_water", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-PLAIN-10"));
//        placeNameService.setColor(java.awt.Color.cyan);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);
//        // US Terrain Features
//        placeNameService = new PlaceNameService(service, "topp:wpl_us_terrain", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-PLAIN-10"));
//        placeNameService.setColor(java.awt.Color.orange);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);

//        // GeoNET Administrative 1st Order
//        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_a_adm1", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-BOLD-10"));
//        placeNameService.setColor(java.awt.Color.yellow);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);
//        // GeoNET Administrative 2nd Order
//        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_a_adm2", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-BOLD-10"));
//        placeNameService.setColor(java.awt.Color.yellow);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);
//        // GeoNET Populated Place Administrative
//        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_ppla", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-BOLD-10"));
//        placeNameService.setColor(java.awt.Color.pink);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);
//        // GeoNET Populated Place
//        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_ppl", fileCachePath, Sector.FULL_SPHERE, GRID_20x40,
//            java.awt.Font.decode("Arial-PLAIN-10"));
//        placeNameService.setColor(java.awt.Color.pink);
//        placeNameService.setMinDisplayDistance(0d);
//        placeNameService.setMaxDisplayDistance(LEVEL_N);
//        placeNameServiceSet.addService(placeNameService, false);

        return placeNameServiceSet;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.PlaceName.Name");
    }
}
