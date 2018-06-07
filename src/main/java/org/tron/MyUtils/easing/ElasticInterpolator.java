package org.tron.MyUtils.easing;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ElasticInterpolator extends EasingInterpolator {

    /**
     * The amplitude.
     */
    private DoubleProperty amplitude = new SimpleDoubleProperty(this, "amplitude", 1);

    /**
     * The number of oscillations.
     */
    private DoubleProperty oscillations = new SimpleDoubleProperty(this, "oscillations", 3);

    /**
     * Default constructor. Initializes the interpolator with ease out mode.
     */
    public ElasticInterpolator() {
        this(EasingMode.EASE_OUT);
    }

    /**
     * Constructs the interpolator with a specific easing mode.
     *
     * @param easingMode The easing mode.
     */
    public ElasticInterpolator(EasingMode easingMode) {
        super(easingMode);
    }

    /**
     * Sets the easing mode.
     *
     * @param easingMode The easing mode.
     * @see #easingModeProperty()
     */
    public ElasticInterpolator(EasingMode easingMode, double amplitude, double oscillations) {
        super(easingMode);
        this.amplitude.set(amplitude);
        this.oscillations.set(oscillations);
    }

    /**
     * The oscillations property. Defines number of oscillations.
     *
     * @return The property.
     * @see #getOscillations()
     * @see #setOscillations(double)
     */
    public DoubleProperty oscillationsProperty() {
        return oscillations;
    }

    /**
     * The amplitude. The minimum value is 1. If this value is < 1 it will be set to 1 during animation.
     *
     * @return The property.
     * @see #getAmplitude()
     * @see #setAmplitude(double)
     */
    public DoubleProperty amplitudeProperty() {
        return amplitude;
    }

    /**
     * Gets the amplitude.
     *
     * @return The amplitude.
     * @see #amplitudeProperty()
     */
    public double getAmplitude() {
        return amplitude.get();
    }

    /**
     * Sets the amplitude.
     *
     * @param amplitude The amplitude.
     * @see #amplitudeProperty()
     */
    public void setAmplitude(final double amplitude) {
        this.amplitude.set(amplitude);
    }

    /**
     * Gets the number of oscillations.
     *
     * @return The oscillations.
     * @see #oscillationsProperty()
     */
    public double getOscillations() {
        return oscillations.get();
    }

    /**
     * Sets the number of oscillations.
     *
     * @param oscillations The oscillations.
     * @see #oscillationsProperty()
     */
    public void setOscillations(final double oscillations) {
        this.oscillations.set(oscillations);
    }

    @Override
    protected double baseCurve(double v) {
        if (v == 0) {
            return 0;
        }
        if (v == 1) {
            return 1;
        }
        double p = 1.0 / oscillations.get();
        double a = amplitude.get();
        double s;
        if (a < Math.abs(1)) {
            a = 1;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(1 / a);
        }
        return -(a * Math.pow(2, 10 * (v -= 1)) * Math.sin((v - s) * (2 * Math.PI) / p));
    }
}
