package cn.he.zhao.bbs.spring;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
/**
 * 描述:
 * Stopwatchs
 *
 * @Author HeFeng
 * @Create 2018-07-28 18:46
 */

public final class Stopwatchs {
    private static final ThreadLocal<Stopwatchs.Stopwatch> STOPWATCH = new ThreadLocal();

    public static void start(String taskTitle) {
        Stopwatchs.Stopwatch root = (Stopwatchs.Stopwatch)STOPWATCH.get();
        if (null == root) {
            root = new Stopwatchs.Stopwatch(taskTitle);
            STOPWATCH.set(root);
        } else {
            Stopwatchs.Stopwatch recent = getRecentRunning((Stopwatchs.Stopwatch)STOPWATCH.get());
            if (null != recent) {
                recent.addLeaf(new Stopwatchs.Stopwatch(taskTitle));
            }
        }
    }

    public static void end() {
        Stopwatchs.Stopwatch root = (Stopwatchs.Stopwatch)STOPWATCH.get();
        if (null != root) {
            Stopwatchs.Stopwatch recent = getRecentRunning(root);
            if (null != recent) {
                recent.setEndTime(System.currentTimeMillis());
            }
        }
    }

    public static void release() {
        STOPWATCH.set((Stopwatch) null);
    }

    public static String getTimingStat() {
        Stopwatchs.Stopwatch root = (Stopwatchs.Stopwatch)STOPWATCH.get();
        if (null == root) {
            return "No stopwatch";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            root.appendTimingStat(1, stringBuilder);
            return stringBuilder.toString();
        }
    }

    public static long getElapsed(String taskTitle) {
        long currentTimeMillis = System.currentTimeMillis();
        if (StringUtils.isBlank(taskTitle)) {
            return -1L;
        } else {
            Stopwatchs.Stopwatch root = (Stopwatchs.Stopwatch)STOPWATCH.get();
            if (null == root) {
                return -1L;
            } else {
                Stopwatchs.Stopwatch stopwatch = get(root, taskTitle);
                if (null == stopwatch) {
                    return -1L;
                } else {
                    return stopwatch.isEnded() ? stopwatch.getElapsedTime() : currentTimeMillis - stopwatch.getStartTime();
                }
            }
        }
    }

    private static Stopwatchs.Stopwatch get(Stopwatchs.Stopwatch parent, String taskTitle) {
        if (taskTitle.equals(parent.getTaskTitle())) {
            return parent;
        } else {
            Iterator var2 = parent.getLeaves().iterator();

            Stopwatchs.Stopwatch ret;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                Stopwatchs.Stopwatch leaf = (Stopwatchs.Stopwatch)var2.next();
                ret = get(leaf, taskTitle);
            } while(null == ret);

            return ret;
        }
    }

    private static Stopwatchs.Stopwatch getRecentRunning(Stopwatchs.Stopwatch parent) {
        if (null == parent) {
            return null;
        } else {
            List<Stopwatchs.Stopwatch> leaves = parent.getLeaves();
            if (leaves.isEmpty()) {
                return parent.isRunning() ? parent : null;
            } else {
                for(int i = leaves.size() - 1; i > -1; --i) {
                    Stopwatchs.Stopwatch leaf = (Stopwatchs.Stopwatch)leaves.get(i);
                    if (leaf.isRunning()) {
                        return getRecentRunning(leaf);
                    }
                }

                return parent;
            }
        }
    }

    private Stopwatchs() {
    }

    private static class Stopwatch {
        private String taskTitle;
        private List<Stopwatchs.Stopwatch> leaves = new ArrayList();
        private long startTime;
        private long endTime;
        private static final int HUNDRED = 100;
        private static final MathContext MATH_CONTEXT;

        Stopwatch(String taskTitle) {
            this.taskTitle = taskTitle;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isEnded() {
            return this.endTime > 0L;
        }

        public boolean isRunning() {
            return 0L == this.endTime;
        }

        public String getTaskTitle() {
            return this.taskTitle;
        }

        public long getEndTime() {
            return this.endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public List<Stopwatchs.Stopwatch> getLeaves() {
            return Collections.unmodifiableList(this.leaves);
        }

        public void addLeaf(Stopwatchs.Stopwatch leaf) {
            this.leaves.add(leaf);
        }

        public long getElapsedTime() {
            return this.endTime - this.startTime;
        }

        public float getPercentOfRoot() {
            Stopwatchs.Stopwatch root = (Stopwatchs.Stopwatch)Stopwatchs.STOPWATCH.get();
            if (null == root) {
                return 0.0F;
            } else {
                float rootElapsedTime = (float)root.getElapsedTime();
                return 0.0F == rootElapsedTime ? 0.0F : (float)this.getElapsedTime() / rootElapsedTime * 100.0F;
            }
        }

        private void appendTimingStat(int level, StringBuilder stringBuilder) {
            stringBuilder.append(this.toString());

            for(int i = 0; i < this.leaves.size(); ++i) {
                Stopwatchs.Stopwatch leaf = (Stopwatchs.Stopwatch)this.leaves.get(i);
                stringBuilder.append(this.getIndentBlanks(level * 2));
                leaf.appendTimingStat(level + 1, stringBuilder);
            }

        }

        private String getIndentBlanks(int num) {
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < num; ++i) {
                builder.append(' ');
            }

            return builder.toString();
        }

        public String toString() {
            float percentOfRoot = this.getPercentOfRoot();
            if (0.0F > percentOfRoot) {
                percentOfRoot = 0.0F;
            }

            BigDecimal percenOfRoot = new BigDecimal((double)percentOfRoot, MATH_CONTEXT);
            StringBuilder stringBuilder = (new StringBuilder("[")).append(percenOfRoot).append("]%, [").append(this.getElapsedTime()).append("]ms [").append(this.getTaskTitle()).append("]").append(Strings.LINE_SEPARATOR);
            return stringBuilder.toString();
        }

        static {
            MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);
        }
    }
}
