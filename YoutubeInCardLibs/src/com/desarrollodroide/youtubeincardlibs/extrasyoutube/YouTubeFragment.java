package com.desarrollodroide.youtubeincardlibs.extrasyoutube;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.desarrollodroide.youtubeincardlibs.BaseFragment;
import com.desarrollodroide.youtubeincardlibs.PicassoCard;
import com.desarrollodroide.youtubeincardlibs.R;
import com.desarrollodroide.youtubeincardlibs.etagcache.EtagCache;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 * <p/>
 * YouTubeFragment which contains a list view of YouTube video cards
 */
public class YouTubeFragment extends BaseFragment {

	private static final String YOUTUBE_PLAYLIST = "PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0";
	private static final String PLAYLIST_KEY = "PLAYLIST_KEY";
	private EtagCache mEtagCache;
	private Playlist mPlaylist;
	private CardListView listView;
	private CardArrayAdapter mCardArrayAdapter;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private int mListCount = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//Picasso.with(getActivity()).setDebugging(true);

		View rootView = inflater.inflate(R.layout.demo_extras_fragment_picasso,
				container, false);

		// restore the playlist after an orientation change
		if (savedInstanceState != null) {
			mPlaylist = new Gson().fromJson(
					savedInstanceState.getString(PLAYLIST_KEY), Playlist.class);

		}

		// ensure the adapter and listview are initialized
		if (mPlaylist != null) {
			mCardArrayAdapter = null;
			cards.clear();
			mListCount = 0;
			mPlaylist = null;
		}

		// start loading the first page of our playlist
		new GetYouTubePlaylistAsyncTask() {
			@Override
			public EtagCache getEtagCache() {
				return mEtagCache;
			}

			@Override
			public void onPostExecute(JSONObject result) {
				handlePlaylistResult(result);
			}
		}.execute(YOUTUBE_PLAYLIST, null);

		return rootView;
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String json = new Gson().toJson(mPlaylist);
		outState.putString(PLAYLIST_KEY, json);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// initialize our etag cache for this playlist
		File cacheFile = new File(activity.getFilesDir(), YOUTUBE_PLAYLIST);
		mEtagCache = EtagCache.create(cacheFile, EtagCache.FIVE_MB);
	}


	private void handlePlaylistResult(JSONObject result) {
		try {
			if (mPlaylist == null) {
				mPlaylist = new Playlist(result);
			} else {
				mPlaylist.addPage(result);
			}
			initCard();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initCard() {
		int count = mPlaylist.getCount();
	
		// Init an array of Cards
		for (int i = mListCount; i < count; i++) {
			cards.add(createCard(i));

			// get the next playlist page if we're at the end of the current
			// page and we have another page to get
			final String nextPageToken = mPlaylist.getNextPageToken(i);
			if (!isEmpty(nextPageToken) && i == mPlaylist.getCount() - 1) {
				mListCount = i + 1;
				new GetYouTubePlaylistAsyncTask() {
					@Override
					public EtagCache getEtagCache() {
						return mEtagCache;
					}

					@Override
					public void onPostExecute(JSONObject result) {
						handlePlaylistResult(result);
					}
				}.execute(YOUTUBE_PLAYLIST, nextPageToken);
			}
				// Set the adapter
				mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);

				listView = (CardListView) getActivity().findViewById(
						R.id.carddemo_extra_list_picasso);
				if (listView != null) {
					listView.setAdapter(mCardArrayAdapter);
				
			}
		}

	}

	private boolean isEmpty(String s) {
		if (s == null || s.length() == 0) {
			return true;
		}
		return false;
	}

	private Card createCard(int position) {
		PlaylistItem item = mPlaylist.getItem(position);
		PicassoCard card = new PicassoCard(getActivity(), item.thumbnailUrl);
		card.setTitle(item.title);
		// card.setSecondaryTitle(item.description);
		card.setCount(position);
		return card;
	}

	@Override
	public int getTitleResourceId() {
		return R.string.carddemo_extras_title_picasso;
	}

}
