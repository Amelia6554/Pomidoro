package com.example.logic;

import android.os.CountDownTimer;

public class Timer {

    private boolean counterIsActive;
    private CountDownTimer countDownTimer;
    private int time;
    private TimerListener listener;

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public boolean isCounterIsActive() {
        return counterIsActive;
    }

    public int getTime() {
        return time;
    }


    public void start_timer(int seconds) {
        if (!counterIsActive) {
            counterIsActive = true;

            countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    time = (int) (millisUntilFinished / 1000);
                    if (listener != null) {
                        listener.onTick(time);
                    }
                }

                @Override
                public void onFinish() {
                    counterIsActive = false;
                    if (listener != null) {
                        listener.onFinish();
                    }
                }
            }.start();
        } else {
            reset();
        }
    }

    private void reset() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        counterIsActive = false;
    }
}


