package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void setUp() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        db = new WeatherDbHelper(mContext).getWritableDatabase();
    }

    @Override
    protected void tearDown() {
        if (cursor != null) { cursor.close(); }
        if (db != null) { db.close(); }
    }

    private long insertRow(String tableName, ContentValues testValues) {
        return db.insert(tableName, null, testValues);
    }

    private Cursor getAllColumnsAndRows(String tableName) {
        return db.query(tableName, null, null, null, null, null, null);
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<>();

        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        assertEquals(true, db.isOpen());

        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: The database has not been created correctly", cursor.moveToFirst());

        do {
            tableNameHashSet.remove(cursor.getString(0));
        } while( cursor.moveToNext() );

        assertTrue("Error: Your database was created without both the location and weather tables",
                tableNameHashSet.isEmpty());

        cursor = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                cursor.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(cursor.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
    }

    public void testLocationTable() {
        ContentValues locationValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId = insertRow(
                WeatherContract.LocationEntry.TABLE_NAME,
                locationValues
        );

        assertTrue("Error: Location Not Inserted Correctly", locationRowId != -1);

        cursor = getAllColumnsAndRows(WeatherContract.LocationEntry.TABLE_NAME);

        assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, locationValues);

        assertFalse("Error: More than one record returned from location query",
                cursor.moveToNext());
    }

    public void testWeatherTable() {
        ContentValues locationValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId = insertRow(
                WeatherContract.LocationEntry.TABLE_NAME,
                locationValues
        );

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        long weatherRowId = insertRow(
                WeatherContract.WeatherEntry.TABLE_NAME,
                weatherValues
        );

        assertTrue("Error: Weather Not Inserted Correctly", weatherRowId != -1);

        cursor = getAllColumnsAndRows(WeatherContract.WeatherEntry.TABLE_NAME);

        assertTrue("Error: No Records returned from weather query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(
                "testInsertReadDb weatherEntry failed to validate",
                cursor,
                weatherValues
        );

        assertFalse("Error: More than one record returned from weather query",
                cursor.moveToNext());
    }
}