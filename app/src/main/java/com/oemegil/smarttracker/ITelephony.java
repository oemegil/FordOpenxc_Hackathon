package com.oemegil.smarttracker;

/**
 * Created by oguzcan.emegil on 5.11.2017.
 */

public interface ITelephony {
    boolean endCall();
    void answerRingingCall();
    void silenceRinger();
}
