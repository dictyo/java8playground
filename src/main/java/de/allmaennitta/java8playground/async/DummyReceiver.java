package de.allmaennitta.java8playground.async;

import java.util.ArrayList;
import java.util.List;

public class DummyReceiver {
    private static DummyReceiver singleton = new DummyReceiver();
    private List<String> notifications = new ArrayList<>(3);
    public static DummyReceiver get(){
        return singleton;
    }

    public void reset(){
        this.notifications.clear();
    }
    public void notify(String msg){
        this.notifications.add(msg);
        System.out.println("Notifications: "+notifications);
    }

    public List<String> getNotifications() {
        return this.notifications;
    }

//    public String sendMsg(String msg) throws InterruptedException {
//        return this.sendMsg(msg, 0);
//    }
//
//    public String sendMsg(String msg, int sleepTimeInSec) throws InterruptedException {
//        if (sleepTimeInSec > 0){
//            TimeUnit.SECONDS.sleep(sleepTimeInSec);
//        }
//        return msg;
//    }
}
