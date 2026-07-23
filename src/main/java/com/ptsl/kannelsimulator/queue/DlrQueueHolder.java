package com.ptsl.kannelsimulator.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ptsl.kannelsimulator.model.DlrRequest;

public final class DlrQueueHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DlrQueueHolder.class);

    /**
     * Queue Capacity
     * Can later be moved to config.
     */
    private static final int QUEUE_CAPACITY = 500_000;

    /**
     * Singleton Instance
     */
    private static final DlrQueueHolder INSTANCE = new DlrQueueHolder();

    /**
     * Internal Queue
     */
    private final BlockingQueue<DlrRequest> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private DlrQueueHolder() {
        LOGGER.info("DLR Queue initialized. Capacity : " + QUEUE_CAPACITY);
    }

    public static DlrQueueHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Producer API.
     * <p>
     * Never blocks the servlet thread.
     */
    public boolean offer(DlrRequest request) {

        if (request == null) {
            return false;
        }

        return queue.offer(request);
    }

    /**
     * Consumer API.
     * <p>
     * Waits for data.
     */
    public DlrRequest poll(long timeout, TimeUnit unit) throws InterruptedException {

        return queue.poll(timeout, unit);
    }

    /**
     * Queue Size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Queue Remaining Capacity
     */
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    /**
     * Is Queue Empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Current Capacity Used
     */
    public int usedCapacity() {
        return QUEUE_CAPACITY - queue.remainingCapacity();
    }

    /**
     * Queue Capacity
     */
    public int capacity() {
        return QUEUE_CAPACITY;
    }

    /**
     * Clear Queue
     * <p>
     * Mainly useful for testing.
     */
    public void clear() {
        queue.clear();
    }
}