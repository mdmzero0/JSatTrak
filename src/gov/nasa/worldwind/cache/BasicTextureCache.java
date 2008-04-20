/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import com.sun.opengl.util.texture.Texture;

/**
 * @author tag
 * @version $Id: BasicTextureCache.java 2450 2007-07-27 17:50:43Z tgaskins $
 */
public class BasicTextureCache implements TextureCache
{
    public static class TextureEntry implements Cacheable
    {
        private final Texture texture;

        public TextureEntry(Texture texture)
        {
            this.texture = texture;
        }

        public Texture getTexture()
        {
            return texture;
        }

        public long getSizeInBytes()
        {
            long size = this.texture.getEstimatedMemorySize();

            // JOGL returns a zero estimated memory size for some textures, so calculate a size ourselves.
            if (size < 1)
                size = this.texture.getHeight() * this.texture.getWidth() * 4;

            return size;
        }
    }

    private final BasicMemoryCache textures;

    public BasicTextureCache(long loWater, long hiWater)
    {
        this.textures = new BasicMemoryCache(loWater, hiWater);
        this.textures.setName("Texture Cache");
        this.textures.addCacheListener(new MemoryCache.CacheListener()
        {
            public void entryRemoved(Object key, Object clientObject)
            {
                // Unbind a tile's texture when the tile leaves the cache.
                if (clientObject != null) // shouldn't be null, but check anyway
                {
                    ((TextureEntry) clientObject).texture.dispose();
                }
            }
        });
    }

    public void put(Object key, Texture texture)
    {
        TextureEntry te = new TextureEntry(texture);
        this.textures.add(key, te);
    }

    public Texture get(Object key)
    {
        TextureEntry entry = (TextureEntry) this.textures.getObject(key);
        return entry != null ? entry.texture : null;
    }

    public void remove(Object key)
    {
        textures.remove(key);
    }

    public int getNumObjects()
    {
        return textures.getNumObjects();
    }

    public long getCapacity()
    {
        return textures.getCapacity();
    }

    public long getUsedCapacity()
    {
        return textures.getUsedCapacity();
    }

    public long getFreeCapacity()
    {
        return textures.getFreeCapacity();
    }

    public boolean contains(Object key)
    {
        return textures.contains(key);
    }

    public void clear()
    {
        textures.clear();
    }
}
