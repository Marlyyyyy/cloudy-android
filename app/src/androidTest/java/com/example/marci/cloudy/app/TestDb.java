package com.example.marci.cloudy.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.marci.cloudy.app.data.WeatherContract;
import com.example.marci.cloudy.app.data.WeatherContract.WeatherEntry;
import com.example.marci.cloudy.app.data.WeatherContract.LocationEntry;
import com.example.marci.cloudy.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Marci on 14/08/2014.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertAndReadDB(){


        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create a new map of test-values, where column names are the keys
        ContentValues locationValues = getLocationContentValues();

        long locationRowId;
        // insert() returns the newly inserted row's id on success; -1 otherwise
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);

        // verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "new row id: " + locationRowId);

        // -- data should be inserted by now

        // a cursor is the primary interface to the query result
        Cursor locationCursor = db.query(
                LocationEntry.TABLE_NAME, // table to query
                null, // want all columns
                null, // columns for the where clause
                null, // values for the  where clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(locationCursor, locationValues);

        // -- location data should be checked by now

        // add some weather data
        ContentValues weatherValues = getWeatherContentValues(locationRowId);

        long weatherRowId;
        weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME, // table to query
                null, // leaving columns null, returns all the columns
                null, // columns for the where clause
                null, // values for the  where clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(weatherCursor, weatherValues);

        dbHelper.close();

    }

    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE = "20141205";

    static ContentValues getLocationContentValues(){

        ContentValues values = new ContentValues();

        double testLatitude = 64.772;
        double testLongitude = -147.355;

        values.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        return values;
    }

    static ContentValues getWeatherContentValues(long locationRowId){

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;

    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues){

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}
