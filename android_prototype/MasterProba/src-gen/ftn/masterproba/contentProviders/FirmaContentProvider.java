package ftn.masterproba.contentProviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import ftn.masterproba.databases.DatabaseOpenHelper;
import ftn.masterproba.databases.FirmaTable;
import ftn.masterproba.databases.NaseljenoMestoTable;
import ftn.masterproba.impl.databases.DatabaseOpenHelperImpl;

public class FirmaContentProvider extends ContentProvider {

	// database
		private DatabaseOpenHelperImpl database;

		// used for the UriMacher
		// Nikad ne koristimo pribavljanje svih knjizara (za sada)
		// Otkomentarisati ovo ukoliko ih budemo koristili
		private static final int MULTIPLE = 10;
		// private static final int FROM_LANAC_KNIZARA = 15;
		private static final int SINGLE = 20;

		private static final String AUTHORITY = "ftn.masterProba.firmaContentProvider";
		private static final String BASE_PATH = "firma";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + BASE_PATH);

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/todos";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/todo";

		private static final UriMatcher sURIMatcher = new UriMatcher(
				UriMatcher.NO_MATCH);
		static {
			sURIMatcher.addURI(AUTHORITY, BASE_PATH, MULTIPLE);
			sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SINGLE);
		}

		@Override
		public boolean onCreate() {
			database = new DatabaseOpenHelperImpl(getContext());
			return false;
		}

		@Override
		public Cursor query(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

			// checkColumns(projection);

			queryBuilder.setTables(FirmaTable.TABLE_NAME 
					+ " LEFT OUTER JOIN "
					+ NaseljenoMestoTable.TABLE_NAME + " naseljeno_mesto " + " ON " 
					+ "naseljeno_mesto" + "." + NaseljenoMestoTable.COLUMN_ID + " = "
					+ FirmaTable.TABLE_NAME + "." + FirmaTable.COLUMN_ID_NASELJENO_MESTO
					+ " LEFT OUTER JOIN "
					+ FirmaTable.TABLE_NAME + " nadfirma " + " ON " 
					+ "nadfirma" + "." + FirmaTable.COLUMN_ID + " = "
					+ FirmaTable.TABLE_NAME + "." + FirmaTable.COLUMN_ID_NADFIRMA);
			// Proverava da li se URI zavrsava sa "/#", ako se zavrsava,
			// dodaje u Where klauzu taj Id.
			int uriType = sURIMatcher.match(uri);
			switch (uriType) {
			case MULTIPLE:
				break;
			case SINGLE:
				// adding the ID to the original query
				queryBuilder.appendWhere(FirmaTable.TABLE_NAME + "." + FirmaTable.COLUMN_ID + "="
						+ uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}

			SQLiteDatabase db = database.getWritableDatabase();
			Cursor cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			// make sure that potential listeners are getting notified
			cursor.setNotificationUri(getContext().getContentResolver(), uri);

			return cursor;
		}

		@Override
		public String getType(Uri uri) {
			return null;
		}

		@Override
		public Uri insert(Uri uri, ContentValues values) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase db = database.getWritableDatabase();
			Long id = (long) 0;
			switch (uriType) {
			case MULTIPLE:
				id = db.insertOrThrow(FirmaTable.TABLE_NAME, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(BASE_PATH + "/" + id);
		}

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase db = database.getWritableDatabase();
			int rowsDeleted = 0;
			switch (uriType) {
			case MULTIPLE:
				rowsDeleted = db.delete(FirmaTable.TABLE_NAME, selection,
						selectionArgs);
				break;
			case SINGLE:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsDeleted = db.delete(FirmaTable.TABLE_NAME,
							FirmaTable.COLUMN_ID + "=" + id, null);
				} else {
					rowsDeleted = db.delete(FirmaTable.TABLE_NAME,
							FirmaTable.COLUMN_ID + "=" + id + " and "
									+ selection, selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsDeleted;
		}

		@Override
		public int update(Uri uri, ContentValues values, String selection,
				String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsUpdated = 0;
			switch (uriType) {
			case MULTIPLE:
				rowsUpdated = sqlDB.update(FirmaTable.TABLE_NAME, values,
						selection, selectionArgs);
				break;
			case SINGLE:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsUpdated = sqlDB.update(FirmaTable.TABLE_NAME,
							values, FirmaTable.COLUMN_ID + "=" + id, null);
				} else {
					rowsUpdated = sqlDB.update(FirmaTable.TABLE_NAME,
							values, FirmaTable.COLUMN_ID + "=" + id
									+ " and " + selection, selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsUpdated;
		}

}