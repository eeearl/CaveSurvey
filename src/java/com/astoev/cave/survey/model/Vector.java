package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 5/6/12
 * Time: 10:20 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "vectors")
public class Vector implements Serializable {

    private static final String COLUMN_POINT = "point_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_POINT)
    private Point mPoint;
    @DatabaseField(columnName = "distance")
    private Float mDistance;
    @DatabaseField(columnName = "azimuth")
    private Float mAzimuth;
    @DatabaseField(columnName = "slope")
    private Float mSlope;

    public Vector() {
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public Point getPoint() {
        return mPoint;
    }

    public void setPoint(Point aPoint) {
        mPoint = aPoint;
    }

    public Float getDistance() {
        return mDistance;
    }

    public void setDistance(Float aDistance) {
        mDistance = aDistance;
    }

    public Float getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(Float aAzimuth) {
        mAzimuth = aAzimuth;
    }

    public Float getSlope() {
        return mSlope;
    }

    public void setSlope(Float aSlope) {
        mSlope = aSlope;
    }
}