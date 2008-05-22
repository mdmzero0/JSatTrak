package javax.media.control;

import javax.media.Control;

public interface H263Control
    extends Control
{

    public abstract boolean isUnrestrictedVectorSupported();

    public abstract boolean setUnrestrictedVector(boolean flag);

    public abstract boolean getUnrestrictedVector();

    public abstract boolean isArithmeticCodingSupported();

    public abstract boolean setArithmeticCoding(boolean flag);

    public abstract boolean getArithmeticCoding();

    public abstract boolean isAdvancedPredictionSupported();

    public abstract boolean setAdvancedPrediction(boolean flag);

    public abstract boolean getAdvancedPrediction();

    public abstract boolean isPBFramesSupported();

    public abstract boolean setPBFrames(boolean flag);

    public abstract boolean getPBFrames();

    public abstract boolean isErrorCompensationSupported();

    public abstract boolean setErrorCompensation(boolean flag);

    public abstract boolean getErrorCompensation();

    public abstract int getHRD_B();

    public abstract int getBppMaxKb();
}
