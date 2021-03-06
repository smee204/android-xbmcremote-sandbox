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

package org.xbmc.android.remotesandbox.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import org.xbmc.android.jsonrpc.provider.AudioContract;
import org.xbmc.android.jsonrpc.provider.AudioDatabase.Tables;
import org.xbmc.android.jsonrpc.service.AudioSyncService;
import org.xbmc.android.remotesandbox.R;

public class ArtistsFragment extends ReloadableListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = ArtistsFragment.class.getSimpleName();

	// This is the Adapter being used to display the list's data.
	private CursorAdapter mAdapter;

	// If non-null, this is the current filter the user has provided.
	private String mCurrentFilter;
	
	private final AudioSyncService.RefreshObserver mRefreshObserver = new AudioSyncService.RefreshObserver() {
		
		@Override
		public void onRefreshed() {
			Log.d(TAG, "Refreshing Artists from database.");
			getLoaderManager().restartLoader(0, null, ArtistsFragment.this);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data.
		setEmptyText(getResources().getString(R.string.empty_artists));

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new ArtistsAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Update
		// the search filter, and restart the loader to do a new query
		// with this filter.
		mCurrentFilter = !TextUtils.isEmpty(newText) ? newText : null;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Insert desired behavior here.
		Log.i("FragmentComplexList", "Item clicked: " + id);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri baseUri;
		if (mCurrentFilter != null) {
			baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(mCurrentFilter));
		} else {
			baseUri = Contacts.CONTENT_URI;
		}

		return new CursorLoader(getActivity(), AudioContract.Artists.CONTENT_URI, ArtistsQuery.PROJECTION, null, null,
				AudioContract.Artists.DEFAULT_SORT);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		// see: http://stackoverflow.com/questions/6757156/android-cursorloader-crash-on-non-topmost-fragment
		if (!isVisible()) {
			return;
		}
		
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() instanceof BaseFragmentTabsActivity) {
			((BaseFragmentTabsActivity)getActivity()).registerRefreshObserver(mRefreshObserver);
		}
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (getActivity() instanceof BaseFragmentTabsActivity) {
			((BaseFragmentTabsActivity)getActivity()).unregisterRefreshObserver(mRefreshObserver);
		}
	}
	/**
	 * {@link CursorAdapter} that renders a {@link ArtistsQuery}.
	 */
	public static class ArtistsAdapter extends CursorAdapter {
		
		private final LayoutInflater mInflater;

		public ArtistsAdapter(Context context) {
			super(context, null, false);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.list_item_onelabel, parent, false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final TextView titleView = (TextView) view.findViewById(R.id.item_title);
			titleView.setText(cursor.getString(ArtistsQuery.NAME));
		}
	}
	
	/**
	 * {@link org.xbmc.android.jsonrpc.provider.AudioContract.Artists}
	 * query parameters.
	 */
	private interface ArtistsQuery {
//		int _TOKEN = 0x1;

		String[] PROJECTION = { 
				Tables.ARTISTS + "." + BaseColumns._ID, 
				AudioContract.Artists.ID,
				AudioContract.Artists.NAME
		};

//		int _ID = 0;
//		int ID = 1;
		int NAME = 2;
	}

}