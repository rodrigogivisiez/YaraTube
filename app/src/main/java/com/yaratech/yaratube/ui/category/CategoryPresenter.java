package com.yaratech.yaratube.ui.category;

import android.content.Context;
import android.util.Log;

import com.yaratech.yaratube.data.source.DataSource;
import com.yaratech.yaratube.data.source.Repository;
import com.yaratech.yaratube.data.source.remote.RemoteDataSource;

import java.util.List;

public class CategoryPresenter implements CategoryContract.Presenter {

    private static final String TAG = "CategoryPresenter";
    private CategoryContract.View mView;
    private Repository repository;
    private Context context;

    public CategoryPresenter() {
    }

    @Override
    public void attachView(CategoryContract.View view) {
        mView = view;
        context = ((CategoryFragment) mView).getContext();
        this.repository = Repository.getINSTANCE(new RemoteDataSource((context)));
    }

    @Override
    public void detachView(CategoryContract.View view) {
        context = null;
        mView = null;
    }

    @Override
    public boolean isAttached() {
        return mView != null;
    }


    @Override
    public void fetchCategoriesFromRemoteDataSource() {
        Log.i(TAG, "fetchCategoriesFromRemoteDataSource: ");

        // check if view is attached to the presenter
        if (isAttached()) {
            // show progress bar
            mView.showProgressBarLoading();
            repository.fetchAllCategories(new DataSource.ApiResultCallback() {

                @Override
                public void onDataLoaded(Object response) {
                    List list = (List) response;
                    Log.i(TAG, "onDataLoaded: <<<< list size is : >>>>" + list.size());
                    if (mView != null) {
                        mView.finishProgressBarLoading();
                        mView.showLoadedData(list);
                    }
                }

                @Override
                public void onDataNotAvailable() {
                    if (mView != null) {
                        mView.finishProgressBarLoading();
                        mView.showDataNotAvailableToast();
                    }
                }

                @Override
                public void onNetworkNotAvailable() {
                    if (mView != null) {
                        mView.finishProgressBarLoading();
                        mView.showNetworkNotAvailableToast();
                    }
                }
            });
        }
    }

    @Override
    public void cancelCategoryApiRequest() {
        repository.cancelCategoryApiRequest();
    }
}