package com.team123.mobilecollector.A02_SessionsManager;


import com.team123.mobilecollector.bluetooth.MdsRx;

import rx.Observable;

public class SessionsManagerPresenter implements SessionsManagerContract.Presenter {

    private final SessionsManagerContract.View mView;

    public SessionsManagerPresenter(SessionsManagerContract.View view) {
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
