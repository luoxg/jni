package com.luo.serialtool;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

/**
 * 作者：Administrator
 * 日期：2016.07.19
 * 说明：
 */
public class HC_BillAcceptor extends Bill_Acceptor{

    public HC_BillAcceptor(String device,int baudrate,int databits,int stopbits,char parity) {
        super(device, baudrate,databits,stopbits,parity);
    }

    @Override
    public void start(final onFinishListener listener) {
        if(!isNormal()){
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }
        if (isStart) {
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        } else if (!starting) {
            new Thread() {
                @Override
                public void run() {
                    starting = true;
                    int available = -1;
                    int delayTime = 500;
                    byte[] buf = new byte[4];
                    try {
                        reset();
                        while (mInputStream.available() <= 2 && delayTime >= 0){
                            SystemClock.sleep(20);
                            delayTime -= 20;
                        }

                        available = mInputStream.available();
                        Log.i(TAG, "available = " + available);
                        if (available > 0) {
                            mInputStream.read(buf);
                        }
                        for (int i = 0; i < 3; i++) {
                            if (buf[i] == (byte) 0x80 && buf[i + 1] == (byte) 0x8F) {
                                allowAccept = true;
                                isStart = true;
                                startReceiveThread();
//                                accept();
                                if (listener != null) {
                                    listener.onSuccess();
                                }
                                Log.i(TAG, "bill acceptor enable");
                                return;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        starting = false;
                        if (!isStart){
                            closeSerialPort();
                        }
                    }
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }.start();
        }
    }

    @Override
    protected void beforeStop(){
        disable();
    }

    @Override
    protected void doBusiness(byte[] buf,int len) {
        switch (index) {
            case 0:
                bytes[0] = buf[0];
                if (bytes[0] == (byte) 0x81) {
                    index++;
                } else {
                    index = 0;
                }
                break;

            case 1:
                bytes[1] = buf[0];
                if (bytes[1] == (byte) 0x8F) {
                    index++;
                } else {
                    index = 0;
                }
                break;

            case 2:
                bytes[2] = buf[0];
                filterMoney();
                index = 0;
                break;
            default:
				/* display */
                index = 0;
                break;
        }
    }

    private void filterMoney() {
        if (allowAccept) {
            switch (bytes[2]){
                case 0x40:
                    money = 1;
                    break;
                case 0x41:
                    money = 5;
                    break;
                case 0x42:
                    money = 10;
                    break;
                case 0x43:
                    money = 20;
                    break;
                case 0x44:
//                    money = 50;
                    break;
                case 0x45:
//                    money = 100;
                    break;
            }
        }
        if (money > 0) {
            accept();
            data_received(money);
            money = 0;
        }else{
            reject();
        }
    }
}
