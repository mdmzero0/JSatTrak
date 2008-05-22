package javax.media;

import javax.media.format.FormatChangeEvent;

// Referenced classes of package javax.media:
//            Controller

public class SizeChangeEvent extends FormatChangeEvent
{

    protected int width;
    protected int height;
    protected float scale;

    public SizeChangeEvent(Controller from, int width, int height, float scale)
    {
        super(from);
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public float getScale()
    {
        return scale;
    }
}
