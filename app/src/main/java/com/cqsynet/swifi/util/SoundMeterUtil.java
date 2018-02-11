package com.cqsynet.swifi.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.cqsynet.swifi.AppConstants;

public class SoundMeterUtil {
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    private Context mContext;
    private OnRecordListener mOnRecordListener;
    private int time = 0;

    public interface OnRecordListener {
        void onRecordOvertime(int remainTime);
    }

    public SoundMeterUtil(Context context, OnRecordListener listener) {
        mContext = context;
        mOnRecordListener = listener;
    }

    public void start(String name) {
        if (mRecorder == null) {
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile(mContext.getCacheDir().getPath() + "/" + name);
                mRecorder.prepare();
                mRecorder.start();
                mEMA = 0.0;
                time = 0;
                mHdl.sendEmptyMessageDelayed(0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        mHdl.removeCallbacksAndMessages(null);
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mRecorder = null;
            }
        }
    }

    public void pause() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    time++;
                    int remainTime = AppConstants.VOICE_RECORD_OVERTIME - time;
                    if(remainTime > 0) {
                        mHdl.sendEmptyMessageDelayed(0, 1000);
                    }
                    mOnRecordListener.onRecordOvertime(remainTime);
                    break;
            }
        }
    };
}
