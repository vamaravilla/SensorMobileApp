package com.team123.mobilecollector.A02_SessionsManager;


import com.team123.mobilecollector.BasePresenter;
import com.team123.mobilecollector.BaseView;

import rx.Observable;

public interface SessionsManagerContract {

    interface Presenter extends BasePresenter {
        Observable<String> subscribeLinearAcc(String uri);

    }

    interface View extends BaseView<SessionsManagerContract.Presenter> {

    }
}
