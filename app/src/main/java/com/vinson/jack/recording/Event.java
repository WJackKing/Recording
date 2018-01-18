package com.vinson.jack.recording;

/**
 * Created by jack on 2018/1/14.
 * save event.
 */

class Event {
    private boolean isSpend;
    private float money;
    private String note = "";

    Event(boolean isSpend, float money){
        this.isSpend = isSpend;
        this.money = money;
    }

    boolean getSpend(){
        return isSpend;
    }

    float getMoney(){
        return money;
    }

    boolean getInvite(){
        return false;
    }

    String getInviteName(){
        return "";
    }

    void setNote(String note){
        this.note = note;
    }

    String getNote(){
        return note;
    }
}
