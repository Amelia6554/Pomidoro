package com.example.logic;

public interface TimerListener {
    void onTick(int secondsLeft);
    void onFinish();
}
