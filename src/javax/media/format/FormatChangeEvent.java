package javax.media.format;

import javax.media.*;

public class FormatChangeEvent extends ControllerEvent
{

    protected Format oldFormat;
    protected Format newFormat;

    public FormatChangeEvent(Controller source)
    {
        super(source);
    }

    public FormatChangeEvent(Controller source, Format oldFormat, Format newFormat)
    {
        super(source);
        this.oldFormat = oldFormat;
        this.newFormat = newFormat;
    }

    public Format getOldFormat()
    {
        return oldFormat;
    }

    public Format getNewFormat()
    {
        return newFormat;
    }
}
