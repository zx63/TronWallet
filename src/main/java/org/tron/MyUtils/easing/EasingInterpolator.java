package org.tron.MyUtils.easing;

import javafx.animation.Interpolator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class EasingInterpolator extends Interpolator {

    /**
     * The easing mode.
     */
    private ObjectProperty<EasingMode> easingMode = new SimpleObjectProperty<>(EasingMode.EASE_OUT);

    /**
     * Constructs the interpolator with a specific easing mode.
     *
     * @param easingMode The easing mode.
     */
    public EasingInterpolator(EasingMode easingMode) {
        this.easingMode.set(easingMode);
    }

    /**
     * The easing mode property.
     *
     * @return The property.
     * @see #getEasingMode()
     * @see #setEasingMode(EasingMode)
     */
    public ObjectProperty<EasingMode> easingModeProperty() {
        return easingMode;
    }

    /**
     * Gets the easing mode.
     *
     * @return The easing mode.
     * @see #easingModeProperty()
     */
    public EasingMode getEasingMode() {
        return easingMode.get();
    }

    /**
     * Sets the easing mode.
     *
     * @param easingMode The easing mode.
     * @see #easingModeProperty()
     */
    public void setEasingMode(EasingMode easingMode) {
        this.easingMode.set(easingMode);
    }

    /**
     * Defines the base curve for the interpolator.
     * The base curve is then transformed into an easing-in, easing-out easing-both curve.
     *
     * @param v The normalized value/time/progress of the interpolation (between 0 and 1).
     * @return The resulting value of the function, should return a value between 0 and 1.
     * @see Interpolator#curve(double)
     */
    protected abstract double baseCurve(final double v);

    /**
     * Curves the function depending on the easing mode.
     *
     * @param v The normalized value (between 0 and 1).
     * @return The resulting value of the function.
     */
    @Override
    protected final double curve(final double v) {
        switch (easingMode.get()) {
            case EASE_IN:
                return baseCurve(v);
            case EASE_OUT:
                return 1 - baseCurve(1 - v);
            case EASE_BOTH:
                if (v <= 0.5) {
                    return baseCurve(2 * v) / 2;
                } else {
                    return (2 - baseCurve(2 * (1 - v))) / 2;
                }

        }
        return baseCurve(v);
    }
}
