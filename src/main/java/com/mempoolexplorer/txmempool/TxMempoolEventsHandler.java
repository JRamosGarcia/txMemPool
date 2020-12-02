package com.mempoolexplorer.txmempool;

import com.mempoolexplorer.txmempool.components.containers.MempoolEventQueueContainer;
import com.mempoolexplorer.txmempool.events.CustomChannels;
import com.mempoolexplorer.txmempool.events.MempoolEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CustomChannels.class)
public class TxMempoolEventsHandler {

    @Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
    private String topic;

    @Autowired
    MempoolEventQueueContainer mempoolEventQueueContainer;

    @StreamListener("txMemPoolEvents")
    public void blockSink(MempoolEvent mempoolEvent) {
        mempoolEventQueueContainer.getBlockingQueue().add(mempoolEvent);
    }

}
