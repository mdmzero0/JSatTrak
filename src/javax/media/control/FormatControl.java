package javax.media.control;

import javax.media.Control;
import javax.media.Format;

public interface FormatControl
    extends Control
{

    public abstract Format getFormat();

    public abstract Format setFormat(Format format);

    public abstract Format[] getSupportedFormats();

    public abstract boolean isEnabled();

    public abstract void setEnabled(boolean flag);
}
