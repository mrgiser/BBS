package cn.he.zhao.bbs.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 描述:
 * id生产
 *
 * @Author HeFeng
 * @Create 2018-09-02 11:46
 */
public final class Ids {
    private static final Lock ID_GEN_LOCK = new ReentrantLock();
    private static final long ID_GEN_SLEEP_MILLIS = 50L;

    private Ids() {
    }

    public static synchronized String genTimeMillisId() {
        String ret = null;
        ID_GEN_LOCK.lock();

        try {
            ret = String.valueOf(System.currentTimeMillis());

            try {
                Thread.sleep(50L);
            } catch (InterruptedException var5) {
                throw new RuntimeException("Generates time millis id fail");
            }
        } finally {
            ID_GEN_LOCK.unlock();
        }

        return ret;
    }
}