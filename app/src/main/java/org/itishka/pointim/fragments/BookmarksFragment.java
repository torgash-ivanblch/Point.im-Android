package org.itishka.pointim.fragments;

import org.itishka.pointim.api.ConnectionManager;
import org.itishka.pointim.api.data.PostList;

import retrofit.Callback;

/**
 * Created by Tishka17 on 21.10.2014.
 */
public class BookmarksFragment extends PostListFragment {
    @Override
    protected void update(Callback<PostList> callback) {
        ConnectionManager.getInstance().pointService.getBookmarks(callback);
    }

    @Override
    protected void loadMore(long before, Callback<PostList> callback) {
        ConnectionManager.getInstance().pointService.getBookmarks(before, callback);
    }
}
