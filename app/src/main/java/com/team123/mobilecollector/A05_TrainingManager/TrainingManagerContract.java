package com.team123.mobilecollector.A05_TrainingManager;


import com.team123.mobilecollector.BasePresenter;
import com.team123.mobilecollector.BaseView;

import rx.Observable;

public interface TrainingManagerContract {

    interface Presenter extends BasePresenter {
        Observable<String> subscribeLinearAcc(String uri);

    }

    interface View extends BaseView<TrainingManagerContract.Presenter> {

    }
}
