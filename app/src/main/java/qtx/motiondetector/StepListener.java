package qtx.motiondetector;

/**
 * Created by qtxdev on 6/18/2016.
 */
public interface StepListener {

    /**
     * Called when a step has been detected.  Given the time in nanoseconds at
     * which the step was detected.
     */
    public void step(long timeNs);

}
