package com.luo.serialtool;

import android.util.Log;

/**
 * 作者：Administrator
 * 日期：2016.07.21
 * 说明：
 */
public class Bill_Acceptor_Server {
    private static final String TAG = Bill_Acceptor_Server.class.getSimpleName();
    private static final String DEVICE = "/dev/ttyS2";
    private static final int BAUDRATE = 9600;
    private boolean hc_enable = false;
    private boolean ict_enable = false;
    private HC_BillAcceptor mHc_billAcceptor;
    private ICT_BillAcceptor mICT_billAcceptor;

    public Bill_Acceptor_Server(){
    }

    public Bill_Acceptor getAcceptor(){
        if (hc_enable){
            return mHc_billAcceptor;
        }else if(ict_enable){
            return mICT_billAcceptor;
        }else{
            return mICT_billAcceptor;
        }
    }

    public void start(){
        new Thread(){
            @Override
            public void run() {
                if (mHc_billAcceptor == null) {
                    mHc_billAcceptor = new HC_BillAcceptor(DEVICE, BAUDRATE,8,1,'N');
                    mHc_billAcceptor.start(new Bill_Acceptor.onFinishListener() {
                        @Override
                        public void onSuccess() {
                            hc_enable = true;
                            Log.i(TAG, "HC bill acceptor enable");
                        }

                        @Override
                        public void onFailed() {
                            Log.i(TAG, "HC bill acceptor disable");
                        }
                    });
                }
//                SystemClock.sleep(2000);
//                if (!hc_enable && mICT_billAcceptor == null){
//                    mICT_billAcceptor = new ICT_BillAcceptor(DEVICE, BAUDRATE,8,1,'E');
//                    mICT_billAcceptor.start(new Bill_Acceptor.onFinishListener() {
//                        @Override
//                        public void onSuccess() {
//                            ict_enable = true;
//                            Log.i(TAG, "ICT bill acceptor enable");
//                        }
//
//                        @Override
//                        public void onFailed() {
//                            Log.i(TAG, "ICT bill acceptor disable");
//                        }
//                    });
//                }
            }
        }.start();
    }

    public void stop(){
        if (hc_enable && mHc_billAcceptor != null){
            mHc_billAcceptor.stopReceiveThread();
        }
        if (ict_enable && mICT_billAcceptor != null){
            mICT_billAcceptor.stopReceiveThread();
        }
    }
}
