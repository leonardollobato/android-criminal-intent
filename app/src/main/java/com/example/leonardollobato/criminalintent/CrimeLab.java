package com.example.leonardollobato.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.leonardollobato.criminalintent.database.CrimeBaseHelper;
import com.example.leonardollobato.criminalintent.database.CrimeCursorWrapper;
import com.example.leonardollobato.criminalintent.database.CrimeDbSchema;
import com.example.leonardollobato.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by leonardollobato on 5/30/16.
 */
public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mApplicationContext;
    private SQLiteDatabase mDatabase;



    public static CrimeLab get(Context context){
        if(sCrimeLab == null)
            sCrimeLab = new CrimeLab(context);
        return sCrimeLab;
    }

    private CrimeLab(Context context){
        mApplicationContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mApplicationContext)
            .getWritableDatabase();
    }

    public List<Crime> getCrimes(){
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null,null);

        try{
            cursor.moveToFirst();

            while(!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally{
            cursor.close();
        }

        return crimes;
    }

    public void addCrime(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void removeCrime(Crime crime){
        StringBuilder whereClauses = new StringBuilder(CrimeTable.Cols.UUID);
        whereClauses.append(" = ? ");

        mDatabase.delete(CrimeTable.NAME,
                whereClauses.toString(),
                new String[] { crime.getId().toString()});
    }

    public void updateCrime(Crime crime){
        ContentValues values = getContentValues(crime);
        StringBuilder whereClauses = new StringBuilder(CrimeTable.Cols.UUID);
        whereClauses.append(" = ? ");

        mDatabase.update(CrimeTable.NAME,
                        values,
                        whereClauses.toString(),
                        new String[] { crime.getId().toString()});
    }

    public Crime getCrime(UUID id){
        CrimeCursorWrapper cursor = queryCrimes(CrimeTable.Cols.UUID + " = ?", new String[]{id.toString()});

        try {
            if(cursor.getCount() ==0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();

        }finally {
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();

        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.TIME, crime.getTitle());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }
}
