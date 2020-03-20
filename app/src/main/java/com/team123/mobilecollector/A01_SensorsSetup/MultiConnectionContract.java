package com.team123.mobilecollector.A01_SensorsSetup;

import com.team123.mobilecollector.BasePresenter;
import com.team123.mobilecollector.BaseView;
import com.polidea.rxandroidble.RxBleDevice;

public interface MultiConnectionContract {

    interface Presenter extends BasePresenter {
        void scanFirstDevice();
        void scanSecondDevice();
        void connect(RxBleDevice rxBleDevice);
        void disconnect(RxBleDevice rxBleDevice);

    }

    interface View extends BaseView<MultiConnectionContract.Presenter> {
        void onFirsDeviceSelectedResult(RxBleDevice rxBleDevice);
        void onSecondDeviceSelectedResult(RxBleDevice rxBleDevice);
        void displayErrorMessage(String message);
    }
}
