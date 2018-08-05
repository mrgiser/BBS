package cn.he.zhao.bbs.model.my;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import cn.he.zhao.bbs.util.Mails;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * Execs
 *
 * @Author HeFeng
 * @Create 2018-07-24 15:58
 */
public final class Execs {
    public static final Logger LOGGER = LoggerFactory.getLogger(Execs.class);

    private Execs() {
    }

    public static String exec(String cmd) {
        InputStream inputStream = null;

        Thread t;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            t = new Thread(new Execs.InputStreamRunnable(p.getErrorStream()));
            t.start();
            inputStream = p.getInputStream();
            String result = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
            p.destroy();
            String var5 = result;
            return var5;
        } catch (IOException var9) {
            LOGGER.error("Executes command [" + cmd + "] failed", var9);
            t = null;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // TODO: 2018/7/24  return t;
        return null;
//        return t;
    }

    public static String exec(String[] cmds) {
        InputStream inputStream = null;

        Thread t;
        try {
            Process p = Runtime.getRuntime().exec(cmds);
            t = new Thread(new Execs.InputStreamRunnable(p.getErrorStream()));
            t.start();
            inputStream = p.getInputStream();
            String result = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
            p.destroy();
            String var5 = result;
            return var5;
        } catch (IOException var9) {
            LOGGER.error( "Executes commands [" + Arrays.toString(cmds) + "] failed", var9);
            t = null;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // TODO: 2018/7/24  return t;
        return null;
//        return t;
    }

    private static class InputStreamRunnable implements Runnable {
        private BufferedReader bufferedReader;

        public InputStreamRunnable(InputStream is) {
            try {
                this.bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"));
            } catch (UnsupportedEncodingException var3) {
                throw new IllegalStateException("Constructs input stream handle thread failed", var3);
            }
        }

        public void run() {
            while(true) {
                try {
                    if (null != this.bufferedReader.readLine()) {
                        continue;
                    }
                } catch (IOException var5) {
                    ;
                } finally {
                    IOUtils.closeQuietly(this.bufferedReader);
                }

                return;
            }
        }
    }
}
