package com.example.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class MyProvider extends ContentProvider {
	public static final String FILES = "video";
	public static final String PROVIDER_AUTHORITY = "com.example.provider.FileProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_AUTHORITY + "/");
	public static final Uri FILES_URI = Uri.parse("content://"
			+ PROVIDER_AUTHORITY + "/" + FILES + "/");
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + PROVIDER_AUTHORITY + "." + FILES;
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + PROVIDER_AUTHORITY + "." + FILES;
	
	static final int FILE_ROOT = 1;
	static final int FILE_ID = 2;
	
	private final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static Context context;
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		String path = getPath(uri);
	    File file = new File(path);
	    if (file.isDirectory())
	      return CONTENT_TYPE;
	    else if (file.isFile())
	      return CONTENT_ITEM_TYPE;
	    return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		mMatcher.addURI(PROVIDER_AUTHORITY, FILES, FILE_ROOT);
		mMatcher.addURI(PROVIDER_AUTHORITY, FILES + "/#", FILE_ID);
		context = getContext();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;
		Log.i("DEBUG", "uri: " + uri); 
		switch (mMatcher.match(uri)) {
	    case FILE_ROOT: // directory
	    	Log.i("DEBUG", "get files");
	    	cursor = getListFiles();
	      break;
	    case FILE_ID: // Uri file ID
	    	//String id = uri.getLastPathSegment();
	    	//selection = selection + " AND " + FILE_ID + " = " + id;
	    	//int id = Integer.valueOf(uri.getLastPathSegment());
	    	//cursor = getListFiles();
	      break;
	    default:
	      throw new IllegalArgumentException("Wrong URI: " + uri);
	    }
	return cursor;
	}
	

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void addRow(MatrixCursor cursor, File file, int id, String path) {
	    String fileName = file.getName();
	    int fileSize = (int)file.length();
	    cursor.addRow(new Object[] {id, fileName, fileSize, path});
	    
	  }
	
	private void addNewRow(MatrixCursor cursor, String fileName, int id, String path) {
	   
	    cursor.addRow(new Object[] {id, fileName, path});
	    
	  }
	
	private Cursor getCursorForFiles(String path) {
		
		String[] columns = {"_id", "fileName", "fileSize", "path"};
		
		MatrixCursor c = new MatrixCursor(columns);
		File baseDir = new File(path);
		
		if (baseDir.isDirectory()) {
		      File[] files = baseDir.listFiles();
		      int id = 0;
		      for (File file: files) {          
		        addRow(c, file, id, file.getAbsolutePath());
		        id++;
		      }
		} else if (baseDir.isFile()) {
		      addRow(c, baseDir, 0, baseDir.getAbsolutePath());
	    }
		
		      return c;
	}
	
	private Cursor getListFiles(){
		
		 AssetManager assetManager = context.getAssets();
		 String[] columns = {"_id", "fileName", "path"};
			
			MatrixCursor c = new MatrixCursor(columns);
	        // To get names of all files inside the "Video" folder
	        try {
	            String[] files = assetManager.list(FILES);
	 
	            for(int i=0; i < files.length; i++){
	            	String path = "file:///android_asset/video/" + files[i];
	            	//String path = "file:///android_asset/" + files[i];
	            	Log.i("DEBUG:", files[i]);
	            	addNewRow(c, files[i], i, path);	
	            }
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
		return c;
		
	}
	
	public static String getPath(Uri uri) {
	    List<String> segments = uri.getPathSegments();

	    String path = "";
	    ContextWrapper c = new ContextWrapper(context);
	    path = c.getFilesDir().getPath();

	    return path;
	  }
	
	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
	    AssetManager am = getContext().getAssets();
	    String file_name = uri.getLastPathSegment();
	    
	    if(file_name == null) 
	        throw new FileNotFoundException();
	    AssetFileDescriptor afd = null;
	    try {
	        afd = am.openFd(FILES + "/" + file_name);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return afd;
	}
	
	private String getMimeTypeFromPath(String path) {
        String extension = path;
        int lastDot = extension.lastIndexOf('.');
        if (lastDot != -1) {
            extension = extension.substring(lastDot + 1);
        }
        // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
        extension = extension.toLowerCase(Locale.getDefault());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    
}
