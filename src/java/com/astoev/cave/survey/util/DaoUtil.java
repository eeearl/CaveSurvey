/**
 *
 */
package com.astoev.cave.survey.util;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.Workspace;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author jmitrev
 */
public class DaoUtil {

    public static Note getActiveLegNote(Leg aActiveLeg) throws SQLException {
        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, aActiveLeg.getFromPoint().getId());
        return Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }

    public static Sketch getScetchByLeg(Leg legArg) throws SQLException {
        return getScetchByPoint(legArg.getFromPoint());
    }
    
    public static Photo getPhotoByLeg(Leg legALeg) throws SQLException{
    	return getPhotoByPoint(legALeg.getFromPoint());
    }

    public static Sketch getScetchByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Sketch, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Sketch.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryForFirst(query.prepare());
    }
    
    public static Photo getPhotoByPoint(Point pointArg) throws SQLException {
    	QueryBuilder<Photo, Integer> query = Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryBuilder();
    	query.where().eq(Photo.COLUMN_POINT_ID, pointArg.getId());
    	return Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryForFirst(query.prepare());
    }
    
    public static Location getLocationByPoint(Point pointArg) throws SQLException {
        Dao<Location, Integer> locationDao = Workspace.getCurrentInstance().getDBHelper().getLocationDao();
        QueryBuilder<Location, Integer> query = locationDao.queryBuilder();
        query.where().eq(Location.COLUMN_POINT_ID, pointArg.getId());
        return locationDao.queryForFirst(query.prepare());
    }

    public static Project getProject(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForId(aId);
    }

    public static Leg getLeg(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getLegDao().queryForId(aId);
    }


    public static Point getPoint(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getPointDao().queryForId(aId);
    }

    public static Gallery getGallery(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryForId(aId);
    }

    public static List<Leg> getCurrProjectLegs() throws SQLException {
        QueryBuilder<Leg, Integer> statementBuilder = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        statementBuilder.where().eq(Leg.COLUMN_PROJECT_ID, Workspace.getCurrentInstance().getActiveProjectId());
        statementBuilder.orderBy(Leg.COLUMN_GALLERY_ID, true);
        statementBuilder.orderBy(Leg.COLUMN_FROM_POINT, true);
        statementBuilder.orderBy(Leg.COLUMN_TO_POINT, true);
//        statementBuilder.orderBy(Leg.COLUMN_DISTANCE_FROM_START, true);

        return Workspace.getCurrentInstance().getDBHelper().getLegDao().query(statementBuilder.prepare());
    }

    public static void refreshPoint(Point aPoint) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getPointDao().refresh(aPoint);
    }

    public static Gallery createGallery(boolean isFirst) throws SQLException {
        Gallery gallery = new Gallery();
        Project currProject = Workspace.getCurrentInstance().getActiveProject();
        if (isFirst) {
            gallery.setName(Gallery.getFirstGalleryName());
        } else {
            gallery.setName(Gallery.generateNextGalleryName(currProject.getId()));
        }
        gallery.setProject(currProject);
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().create(gallery);
        return gallery;
    }

    public static Gallery getLastGallery(Integer aProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aProjectId);
        query.orderBy(Gallery.COLUMN_ID, false);
        return query.queryForFirst();
    }

    public static Leg getLegByToPoint(Point aToPoint) throws SQLException {
        // TODO this will work as soon as we keep a tree of legs. Once we start closing circles will break
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_TO_POINT, aToPoint.getId());
        return query.queryForFirst();
    }

    public static long getGalleriesCount(Integer aActiveProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aActiveProjectId);
        return query.countOf();
    }
    
    /**
     * DAO method that saves or update the location of Point based on GPS location 
     * 
     * @param parentPointArg - parent Point
     * @param gpsLocationArg - GPS Location
     * @throws SQLException if there is a problem working with the DB
     */
    public static void saveLocationToPoint(final Point parentPointArg, final android.location.Location gpsLocationArg)
        throws SQLException {
        
        ConnectionSource connetionSource = Workspace.getCurrentInstance().getDBHelper().getConnectionSource();
        TransactionManager.callInTransaction(connetionSource, new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Location oldLocation = getLocationByPoint(parentPointArg);
                if (oldLocation != null){
                    oldLocation.setLatitude(gpsLocationArg.getLatitude());
                    oldLocation.setLongitude(gpsLocationArg.getLongitude());
                    oldLocation.setAltitude((int)gpsLocationArg.getAltitude());
                    oldLocation.setAccuracy((int)gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().update(oldLocation);
                    
                    Log.i(Constants.LOG_TAG_DB, "Update location with id:" + oldLocation.getId() + " for point:" + parentPointArg.getId());
                    return oldLocation.getId();
                } else {
                    Location newLocation = new Location();
                    newLocation.setPoint(parentPointArg);
                    newLocation.setLatitude(gpsLocationArg.getLatitude());
                    newLocation.setLongitude(gpsLocationArg.getLongitude());
                    newLocation.setAltitude((int)gpsLocationArg.getAltitude());
                    newLocation.setAccuracy((int)gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().create(newLocation);
                    
                    Log.i(Constants.LOG_TAG_DB, "Creted location with id:" + newLocation.getId() + " for point:" + parentPointArg.getId());
                    return newLocation.getId();
                }
            }
        });
        
    }
}
