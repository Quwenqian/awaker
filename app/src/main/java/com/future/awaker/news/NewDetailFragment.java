package com.future.awaker.news;


import android.databinding.Observable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.future.awaker.R;
import com.future.awaker.base.BaseListFragment;
import com.future.awaker.base.listener.OnItemClickListener;
import com.future.awaker.data.Header;
import com.future.awaker.data.NewDetail;
import com.future.awaker.data.NewEle;
import com.future.awaker.databinding.FragNewDetailBinding;
import com.future.awaker.util.HtmlParser;

import java.util.List;

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Copyright ©2017 by Teambition
 */

public class NewDetailFragment extends BaseListFragment<FragNewDetailBinding> implements OnItemClickListener<NewEle> {

    private static final String NEW_ID = "newId";
    private static final String NEW_TITLE = "newTitle";
    private static final String NEW_URL = "newUrl";

    private NewDetailViewModel viewModel = new NewDetailViewModel();
    private NewDetailBack newDetailBack = new NewDetailBack();
    private NewDetailAdapter adapter;

    public static NewDetailFragment newInstance(String newId, String newTitle, String newUrl) {
        Bundle args = new Bundle();
        args.putString(NEW_ID, newId);
        args.putString(NEW_TITLE, newTitle);
        args.putString(NEW_URL, newUrl);
        NewDetailFragment fragment = new NewDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.frag_new_detail;
    }

    @Override
    protected int getEmptyLayout() {
        return R.layout.layout_empty;
    }

    @Override
    protected void initData() {
        String newId = getArguments().getString(NEW_ID);
        String newTitle = getArguments().getString(NEW_TITLE);
        String newUrl = getArguments().getString(NEW_URL);

        setToolbar(binding.toolbar);

        viewModel.setNewId(newId);
        viewModel.setTitle(newTitle);

        setViewModel(viewModel);
        binding.setViewModel(viewModel);

        Header header = new Header();
        header.title = newTitle;
        header.url = newUrl;

        adapter = new NewDetailAdapter(header, this);
        recyclerView.setAdapter(adapter);

        viewModel.newDetail.addOnPropertyChangedCallback(newDetailBack);
    }

    @Override
    public void onResume() {
        adapter.onResume();
        super.onResume();

    }

    @Override
    public void onPause() {
        adapter.onPause();
        super.onPause();

    }

    @Override
    public void onDestroy() {
        adapter.onDestroy();
        viewModel.newDetail.removeOnPropertyChangedCallback(newDetailBack);

        super.onDestroy();
    }


    @Override
    public void onLoadMore() {
        // new detail not load more
    }

    @Override
    public void onItemClick(View view, int position, NewEle bean) {
        if (NewEle.TYPE_IMG == bean.type) {
            String url = bean.imgUrl;
            ImageDetailActivity.launch(getContext(), url);
        } else if (NewEle.TYPE_VIDEO == bean.type) {
            String url = bean.videoUrl;
            ImageDetailActivity.launch(getContext(), url);
        }
    }

    private class NewDetailBack extends Observable.OnPropertyChangedCallback {

        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            NewDetail newDetail = viewModel.newDetail.get();
            if (newDetail == null) {
                return;
            }
            String html = newDetail.content;
            if (TextUtils.isEmpty(html)) {
                return;
            }
            final String realHtml = html;
            io.reactivex.Observable.create((ObservableOnSubscribe<List<NewEle>>) e -> {

                List<NewEle> newEleList = HtmlParser.htmlToList(realHtml);
                e.onNext(newEleList);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(newEleList -> adapter.setData(newEleList));
        }
    }
}