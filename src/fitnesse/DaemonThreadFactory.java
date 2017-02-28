package fitnesse;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
   * The default thread factory - creates daemon threads
   */
  public class DaemonThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public DaemonThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() :
              Thread.currentThread().getThreadGroup();
      namePrefix = "server-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r,
              namePrefix + threadNumber.getAndIncrement(),
              0);
      t.setDaemon(true);
      if (t.getPriority() != Thread.NORM_PRIORITY)
        t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }