package com.astoev.cave.survey.activity.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;

/**
 * Created by astoev on 12/31/13.
 */
public class MapUtilities {
    private static final int[] COLORS = new int[]{Color.YELLOW, Color.RED, Color.GRAY, Color.GREEN, Color.BLUE};

    public static int getNextGalleryColor(int currentCountArg) {
        // assure predictable colors for the galleries, start repeating colors if too many galleries
        int colorIndex = currentCountArg % COLORS.length;
        return COLORS[colorIndex];
    }

    public static Bitmap combineBitmaps(Bitmap first, Bitmap second) {
        Bitmap bmOverlay;
        if (first != null) {
            bmOverlay = Bitmap.createBitmap(first.getWidth(), first.getHeight(), first.getConfig());
        } else {
            bmOverlay = Bitmap.createBitmap(second.getWidth(), second.getHeight(), second.getConfig());
        }
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(first, new Matrix(), null);
        if (second != null) {
            canvas.drawBitmap(second, 0, 0, null);
        }

        return bmOverlay;
    }

    public static Float getMiddleAngle(Float aFirstAzimuth, Float aSecondAzimuth) {
        if (aFirstAzimuth.equals(aSecondAzimuth)) {
            return aFirstAzimuth;
        } else if (Math.abs(aFirstAzimuth - aSecondAzimuth) > Option.MAX_VALUE_AZIMUTH_DEGREES/2) {
            // average, then flip the direction if sum goes above 360
            return addDegrees((aFirstAzimuth + aSecondAzimuth)/2, Option.MAX_VALUE_AZIMUTH_DEGREES/2);
        } else {
            // average for small angles
            return (aFirstAzimuth + aSecondAzimuth)/2;
        }
    }

    public static Float getAzimuthInDegrees(Float anAzimuth) {
        if (null == anAzimuth) {
            return null;
        }

        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS))) {
            return anAzimuth;
        } else {
            // convert from grads to degrees
            return anAzimuth * Constants.GRAD_TO_DEC;
        }
    }

    public static Float getSlopeInDegrees(Float aSlope) {
        if (null == aSlope) {
            return null;
        }

        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_SLOPE_UNITS))) {
            return aSlope;
        } else {
            // convert from grads to degrees
            return aSlope * Constants.GRAD_TO_DEC;
        }
    }

    public static Float applySlopeToDistance(Float aDistance, Float aSlope) {
        if (aSlope == null) {
            return aDistance;
        }
        return Double.valueOf(aDistance * Math.cos(Math.toRadians(aSlope))).floatValue();
    }

    public static Float add90Degrees(Float anAzimuth) {
        return addDegrees(anAzimuth, 90);
    }

    private static Float addDegrees(Float anAzimuth, int numDegrees) {
        float newAngle = anAzimuth + numDegrees;

        if (newAngle < Option.MAX_VALUE_AZIMUTH_DEGREES) {
            return newAngle;
        } else if (newAngle == Option.MAX_VALUE_AZIMUTH_DEGREES) {
            return Float.valueOf(Option.MIN_VALUE_AZIMUTH);
        } else {
            return newAngle - Option.MAX_VALUE_AZIMUTH_DEGREES;
        }
    }

    public static Float minus90Degrees(Float anAzimuth) {

        if (anAzimuth >= 90) {
            return anAzimuth - 90;
        } else {
            return Option.MAX_VALUE_AZIMUTH_DEGREES + anAzimuth - 90;
        }
    }

    public static boolean isSlopeValid(Float aSlope) {
        if (null != aSlope) {
            String currSlopeMeasure = Options.getOptionValue(Option.CODE_SLOPE_UNITS);
            int maxValue, minValue;
            if (Option.UNIT_DEGREES.equals(currSlopeMeasure)) {
                maxValue = Option.MAX_VALUE_SLOPE_DEGREES;
                minValue = Option.MIN_VALUE_SLOPE_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_SLOPE_GRADS;
                minValue = Option.MIN_VALUE_SLOPE_GRADS;
            }
            if (aSlope > maxValue || aSlope < minValue) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAzimuthValid(Float anAzimuth) {
        if (null != anAzimuth) {
            if (anAzimuth < 0) {
                return false;
            }

            String currAzimuthMeasure = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            int maxValue;
            if (Option.UNIT_DEGREES.equals(currAzimuthMeasure)) {
                maxValue = Option.MAX_VALUE_AZIMUTH_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_AZIMUTH_GRADS;
            }
            if (anAzimuth > maxValue) {
                return false;
            }
        }

        return true;
    }
}
