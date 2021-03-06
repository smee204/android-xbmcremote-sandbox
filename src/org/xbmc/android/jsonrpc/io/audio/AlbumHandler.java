/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.jsonrpc.io.audio;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xbmc.android.jsonrpc.api.AudioLibraryAPI;
import org.xbmc.android.jsonrpc.io.JsonHandler;
import org.xbmc.android.jsonrpc.provider.AudioContract;
import org.xbmc.android.jsonrpc.provider.AudioContract.Albums;
import org.xbmc.android.jsonrpc.provider.AudioContract.Artists;
import org.xbmc.android.jsonrpc.provider.AudioContract.SyncColumns;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

/**
 * Handles one-way synchronization between XBMC's <tt>album</tt> table and the local
 * {@link Albums} table.
 *
 * @author freezy <freezy@xbmc.org>
 */
public class AlbumHandler extends JsonHandler {

	private final static String TAG = AlbumHandler.class.getSimpleName();

	public AlbumHandler() {
		super(AudioContract.CONTENT_AUTHORITY);
	}

	

	@Override
	public ContentValues[] parse(JSONObject result, ContentResolver resolver)
			throws JSONException {
		Log.d(TAG, "Building queries for album's drop and create.");

		final long now = System.currentTimeMillis();
		final JSONArray albums = result.getJSONArray("albums");

		final ContentValues[] batch = new ContentValues[albums.length()];
		
		
		for (int i = 0; i < albums.length(); i++) {
			final JSONObject album = albums.getJSONObject(i);
			batch[i] = new ContentValues();
			batch[i].put(SyncColumns.UPDATED, now);
			batch[i].put(Albums.ID, album.getString("albumid"));
			batch[i].put(Albums.TITLE, album.getString(AudioLibraryAPI.AlbumFields.TITLE));
			batch[i].put(Albums.PREFIX + Artists.ID, album.getString(AudioLibraryAPI.AlbumFields.ARTISTID));
			batch[i].put(Albums.YEAR, album.getString(AudioLibraryAPI.AlbumFields.YEAR));
		}
		return batch;
	}

}
