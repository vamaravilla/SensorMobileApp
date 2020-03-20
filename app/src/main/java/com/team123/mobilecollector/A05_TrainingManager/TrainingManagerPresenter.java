package com.team123.mobilecollector.A05_TrainingManager;


import com.team123.mobilecollector.bluetooth.MdsRx;

import rx.Observable;

public class TrainingManagerPresenter implements TrainingManagerContract.Presenter {

    private final TrainingManagerContract.View mView;

    public TrainingManagerPresenter(TrainingManagerContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public Observable<String> subscribeLinearAcc(String uri) {
        return MdsRx.Instance.subscribe(uri);

    }
}
