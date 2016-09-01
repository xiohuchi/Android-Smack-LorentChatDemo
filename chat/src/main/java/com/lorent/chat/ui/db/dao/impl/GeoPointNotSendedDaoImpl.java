package com.lorent.chat.ui.db.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lorent.chat.ui.db.dao.PointNotSendedDAO;
import com.lorent.chat.ui.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class GeoPointNotSendedDaoImpl implements PointNotSendedDAO {
	
	private DBHelper dbHelper; 
	
	private static final String TABLE = "BLIT_OVERLAY_NOTSEND";
	
	public GeoPointNotSendedDaoImpl(Context context){
		
		dbHelper = new DBHelper(context);
		
	}
	
	@Override
	public void save(long rowid) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db.beginTransaction();
		
		ContentValues values = new ContentValues();
		
		values.put("not_send_rowid", rowid);
		
		db.insert(TABLE, null, values);
		
		db.setTransactionSuccessful();
		
		db.endTransaction();
		
	}

	@Override
	public void findByGeoPointRowId(long rowid) {
		
	}

	@Override
	public List<String> findAll() {
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT not_send_rowid FROM BLIT_OVERLAY_NOTSEND", null);
		
		List<String> rowids = new ArrayList<String>();
		
		long rowid = 0;
		
		while(cursor.moveToNext()){
			
			rowid = cursor.getLong(0);
			
			rowids.add(rowid+"");
			
		}
		
		return rowids;
	}

	@Override
	public boolean mHasNotSend() {
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT COUNT(1) FROM " + TABLE, null);
		
		cursor.moveToFirst();
		
		boolean isSend = cursor.getLong(0)>0?true:false;
		
		cursor.close();
				
		return isSend;
	}

	@Override
	public void closeDB() {
		dbHelper.close();
	}
	

}
