package javax.media.datasink;

import java.util.EventObject;
import javax.media.DataSink;
import javax.media.MediaEvent;

public class DataSinkEvent extends MediaEvent
{

    private String message;

    public DataSinkEvent(DataSink from)
    {
        super(from);
        message = new String("");
    }

    public DataSinkEvent(DataSink from, String reason)
    {
        super(from);
        message = new String(reason);
    }

    public DataSink getSourceDataSink()
    {
        return (DataSink)getSource();
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + getSource() + "] message: " + message;
    }
}
