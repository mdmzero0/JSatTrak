/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.avlist;

/**
 * @author Tom Gaskins
 * @version $Id: AVKey.java 3735 2007-12-06 02:20:43Z tgaskins $
 */
public interface AVKey // TODO: Eliminate unused constants, if any
{
    final String BMNG_ONE_IMAGE_PATH = "gov.nasa.worldwind.avkey.BMNGOneImagePath";

    final String DATA_CACHE_NAME = "gov.nasa.worldwind.avkey.DataCacheNameKey";
    final String DATA_FILE_CACHE_CLASS_NAME = "gov.nasa.worldwind.avkey.DataFileCacheClassName";
    final String DATA_FILE_CACHE_CONFIGURATION_FILE_NAME =
        "gov.nasa.worldwind.avkey.DataFileCacheConfigurationFileName";
    final String DATASET_NAME = "gov.nasa.worldwind.avkey.DatasetNameKey";
    final String DISPLAY_NAME = "gov.nasa.worldwind.avkey.DisplayName";
    final String DISPLAY_ICON = "gov.nasa.worldwind.avkey.DisplayIcon";

    final String ELEVATION_MODEL = "gov.nasa.worldwind.avkey.ElevationModel";
    final String ELEVATION_TILE_CACHE_SIZE = "gov.nasa.worldwind.avkey.ElevationTileCacheSize";
    final String EXTENT = "gov.nasa.worldwind.avkey.Extent";
    final String EXPIRY_TIME = "gov.nasa.worldwind.avkey.ExpiryTime";

    final String FORMAT_SUFFIX = "gov.nasa.worldwind.avkey.FormatSuffixKey";
    final String FOV = "gov.nasa.worldwind.avkey.FieldOfView";

    final String GLOBE = "gov.nasa.worldwind.avkey.GlobeObject";
    final String GLOBE_CLASS_NAME = "gov.nasa.worldwind.avkey.GlobeClassName";

    final String IMAGE_FORMAT = "gov.nasa.worldwind.avkey.ImageFormat";
    final String INACTIVE_LEVELS = "gov.nasa.worldwind.avkey.InactiveLevels";
    final String INITIAL_ALTITUDE = "gov.nasa.worldwind.avkey.InitialAltitude";
    final String INITIAL_HEADING = "gov.nasa.worldwind.avkey.InitialHeading";
    final String INITIAL_LATITUDE = "gov.nasa.worldwind.avkey.InitialLatitude";
    final String INITIAL_LONGITUDE = "gov.nasa.worldwind.avkey.InitialLongitude";
    final String INITIAL_PITCH = "gov.nasa.worldwind.avkey.InitialPitch";
    final String INPUT_HANDLER_CLASS_NAME = "gov.nasa.worldwind.avkey.InputHandlerClassName";

    final String LAYER = "gov.nasa.worldwind.avkey.LayerObject";
    final String LAYER_NAMES = "gov.nasa.worldwind.avkey.LayerNames";
    final String LAYERS = "gov.nasa.worldwind.avkey.LayersObject";
    final String LAYERS_CLASS_NAMES = "gov.nasa.worldwind.avkey.LayerClassNames";
    final String LEVEL_NAME = "gov.nasa.worldwind.avkey.LevelNameKey";
    final String LEVEL_NUMBER = "gov.nasa.worldwind.avkey.LevelNumberKey";
    final String LEVEL_ZERO_TILE_DELTA = "gov.nasa.worldwind.avkey.LevelZeroTileDelta";
    final String LOGGER_NAME = "gov.nasa.worldwind.avkey.LoggerName";

    final String MAX_ABSENT_TILE_ATTEMPTS = "gov.nasa.worldwind.avkey.MaxAbsentTileAttempts";
    final String MEMORY_CACHE_SET_CLASS_NAME = "gov.nasa.worldwind.avkey.MemoryCacheSetClassName";
    final String MIN_ABSENT_TILE_CHECK_INTERVAL = "gov.nasa.worldwind.avkey.MinAbsentTileCheckInterval";
    final String MODEL = "gov.nasa.worldwind.avkey.ModelObject";
    final String MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.ModelClassName";

    final String NETWORK_STATUS_CLASS_NAME = "gov.nasa.worldwind.avkey.NetworkStatusClassName";
    final String NUM_EMPTY_LEVELS = "gov.nasa.worldwind.avkey.NumEmptyLevels";
    final String NUM_LEVELS = "gov.nasa.worldwind.avkey.NumLevels";

    final String OFFLINE_MODE = "gov.nasa.worldwind.avkey.OfflineMode";

    final String PICKED_OBJECT = "gov.nasa.worldwind.avkey.PickedObject";
    final String PICKED_OBJECT_ID = "gov.nasa.worldwind.avkey.PickedObject.ID";
    final String PICKED_OBJECT_PARENT_LAYER = "gov.nasa.worldwind.avkey.PickedObject.ParentLayer";
    final String PICKED_OBJECT_PARENT_LAYER_NAME = "gov.nasa.worldwind.avkey.PickedObject.ParentLayer.Name";
    final String PLACENAME_LAYER_CACHE_SIZE = "gov.nasa.worldwind.avkey.PlacenameLayerCacheSize";
    final String POSITION = "gov.nasa.worldwind.avkey.Position";

    final String RETRIEVAL_POOL_SIZE = "gov.nasa.worldwind.avkey.RetrievalPoolSize";
    final String RETRIEVAL_QUEUE_SIZE = "gov.nasa.worldwind.avkey.RetrievalQueueSize";
    final String RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT = "gov.nasa.worldwind.avkey.RetrievalStaleRequestLimit";
    final String RETRIEVAL_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.RetrievalServiceClassName";
    final String RETRIEVER_STATE = "gov.nasa.worldwind.avkey.RetrieverState";

    final String SCENE_CONTROLLER = "gov.nasa.worldwind.avkey.SceneControllerObject";
    final String SCENE_CONTROLLER_CLASS_NAME = "gov.nasa.worldwind.avkey.SceneControllerClassName";
    final String SECTOR = "gov.nasa.worldwind.avKey.Sector";
    final String SECTOR_GEOMETRY_CACHE_SIZE = "gov.nasa.worldwind.avkey.SectorGeometryCacheSize";
    final String SECTOR_RESOLUTION_LIMITS = "gov.nasa.worldwind.avkey.SectorResolutionLimits";
    final String SENDER = "gov.nasa.worldwind.avkey.Sender";
    final String SERVICE = "gov.nasa.worldwind.avkey.ServiceURLKey";
    final String STYLE_NAMES = "gov.nasa.worldwind.avkey.StyleNames";

    final String TESSELLATOR_CLASS_NAME = "gov.nasa.worldwind.avkey.TessellatorClassName";
    final String TEXTURE_CACHE_SIZE = "gov.nasa.worldwind.avkey.TextureCacheSize";
    final String TEXTURE_IMAGE_CACHE_SIZE = "gov.nasa.worldwind.avkey.TextureTileCacheSize";
    final String TASK_POOL_SIZE = "gov.nasa.worldwind.avkey.TaskPoolSize";
    final String TASK_QUEUE_SIZE = "gov.nasa.worldwind.avkey.TaskQueueSize";
    final String TASK_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.TaskServiceClassName";
    final String TEXT = "gov.nasa.worldwind.avkey.Text";
    final String TILE_DELTA = "gov.nasa.worldwind.avkey.TileDeltaKey";
    final String TILE_HEIGHT = "gov.nasa.worldwind.avkey.TileHeightKey";
    final String TILE_RETRIEVER = "gov.nasa.worldwind.avkey.TileRetriever";
    final String TILE_URL_BUILDER = "gov.nasa.worldwind.avkey.TileURLBuilder";
    final String TILE_WIDTH = "gov.nasa.worldwind.avkey.TileWidthKey";
    final String TITLE = "gov.nasa.worldwind.avkey.Title";

    final String URL = "gov.nasa.worldwind.avkey.URL";
    final String URL_CONNECT_TIMEOUT = "gov.nasa.worldwind.avkey.URLConnectTimeout";
    final String URL_PROXY_HOST = "gov.nasa.worldwind.avkey.UrlProxyHost";
    final String URL_PROXY_PORT = "gov.nasa.worldwind.avkey.UrlProxyPort";
    final String URL_PROXY_TYPE = "gov.nasa.worldwind.avkey.UrlProxyType";
    final String URL_READ_TIMEOUT = "gov.nasa.worldwind.avkey.URLReadTimeout";

    final String VERTICAL_EXAGGERATION = "gov.nasa.worldwind.avkey.VerticalExaggeration";
    final String VIEW = "gov.nasa.worldwind.avkey.ViewObject";
    final String VIEW_CLASS_NAME = "gov.nasa.worldwind.avkey.ViewClassName";
    final String VIEW_QUIET = "gov.nasa.worldwind.avkey.ViewQuiet";

    final String WORLD_WINDOW_CLASS_NAME = "gov.nasa.worldwind.avkey.WorldWindowClassName";
    final String WORLD_MAP_IMAGE_PATH = "gov.nasa.worldwind.avkey.WorldMapImagePath";
}
