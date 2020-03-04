package com.xrstaxatrwebchat.wchat;

import com.vaadin.flow.component.UI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {

   ExecutorService executorService;

    private Map<UI, BroadcastListener> listeners;

    void instantiateListeners() {
        this.listeners = new HashMap<>();
    }

    void instantiateExecutors(){
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public synchronized void register(UI ui, BroadcastListener listener) {
        this.listeners.put(ui, listener);
    }

    public synchronized void unregister(UI ui) {
        this.listeners.remove(ui);
    }

    public synchronized void broadcast(final String message) {
        for (final Map.Entry<UI, BroadcastListener> entry : listeners.entrySet()) {

            this.executorService.execute(new Runnable() {

                @Override
                public void run() {
                    entry.getValue().receiveBroadcast(entry.getKey(), message);
                }
            });
        }
    }

    public synchronized void unitBroadcast(String message, UI ui){

        this.executorService.execute((new Runnable() {
            @Override
            public void run() {
                listeners.get(ui).receiveBroadcast(ui, message);
            }
        }));
    }

    public interface BroadcastListener {
        void receiveBroadcast(UI ui, String message);
    }


}
