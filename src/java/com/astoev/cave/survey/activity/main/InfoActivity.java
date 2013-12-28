package com.astoev.cave.survey.activity.main;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.*;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.StringUtils;
import org.apache.poi.ss.usermodel.Drawing;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class InfoActivity extends MainMenuActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        try {
            // prepare labels
            TextView projectName = (TextView) findViewById(R.id.infoProjectName);
            projectName.setText(getWorkspace().getActiveProject().getName());

            TextView projectCreated = (TextView) findViewById(R.id.infoProjectCreated);
            projectCreated.setText(getWorkspace().getActiveProject().getCreationDateFormatted());

            List<Leg> legs = getWorkspace().getCurrProjectLegs();

            TextView projectNumLegs = (TextView) findViewById(R.id.infoNumLegs);
            projectNumLegs.setText("" + legs.size());

            TextView projectTotalLength = (TextView) findViewById(R.id.infoRawDistance);
            float totalLength = 0;
            int numNotes = 0, numDrawings = 0, numCoordinates = 0, numPhotos = 0;
            for (Leg l : legs) {
                if (l.getDistance() != null) {
                    totalLength += l.getDistance();
                }

                // notes
                List<Note> notes = getWorkspace().getDBHelper().getNoteDao().queryBuilder().where().eq(Note.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (notes != null && notes.size() >0) {
                    numNotes += notes.size();
                }

                // drawings
                List<Drawing> drawings = getWorkspace().getDBHelper().getSketchDao().queryBuilder().where().eq(Sketch.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (drawings != null && drawings.size() > 0){
                    numDrawings += drawings.size();
                }

                // gps
                List<Location> locations = getWorkspace().getDBHelper().getLocationDao().queryBuilder().where().eq(Location.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (locations != null && locations.size() >0) {
                    numCoordinates += locations.size();
                }

                // photo
                List<Photo>  photos = getWorkspace().getDBHelper().getPhotoDao().queryBuilder().where().eq(Photo.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (photos != null && photos.size() >0) {
                    numPhotos += photos.size();
                }
            }
            String lengthLabel = StringUtils.floatToLabel(totalLength) + " " + Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
            projectTotalLength.setText(lengthLabel);


            TextView projectNumNotes = (TextView) findViewById(R.id.infoNumNotes);

            projectNumNotes.setText(StringUtils.intToLabel(numNotes));
            TextView projectNumDrawings = (TextView) findViewById(R.id.infoNumDrawings);
            projectNumDrawings.setText(StringUtils.intToLabel(numDrawings));
            TextView projectNumCoordinates = (TextView) findViewById(R.id.infoNumCoordinates);
            projectNumCoordinates.setText(StringUtils.intToLabel(numCoordinates));
            TextView projectNumPhotos = (TextView) findViewById(R.id.infoNumPhotos);
            projectNumPhotos.setText(StringUtils.intToLabel(numPhotos));


            ((TextView)findViewById(R.id.infoDistanceIn)).setText(Options.getOptionValue(Option.CODE_DISTANCE_UNITS));
            ((TextView)findViewById(R.id.infoAzimuthIn)).setText(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS));
            ((TextView)findViewById(R.id.infoSlopeIn)).setText(Options.getOptionValue(Option.CODE_SLOPE_UNITS));


        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render info activity", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void exportProject() {
        try {
            Log.i(Constants.LOG_TAG_SERVICE, "Start excel export");

            // export legs

            ExcelExport export = new ExcelExport(getWorkspace(), this);
            String exportPath = export.runExport(getWorkspace().getActiveProject());

            UIUtilities.showNotification(this, R.string.export_done, exportPath);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
	 */
	@Override
	protected int getChildsOptionsMenu() {
		return R.menu.infomenu;
	}

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(Constants.LOG_TAG_UI, "Info activity's menu selected - " + item.toString());
		
		switch (item.getItemId()) {
			case R.id.info_action_export:{
				exportProject();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}		
	}
}
