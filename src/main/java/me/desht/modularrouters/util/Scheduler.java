package me.desht.modularrouters.util;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import me.desht.modularrouters.ModularRouters;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>A highly efficient, thread-safe, lock-free implementation of
 * {@link com.google.common.util.concurrent.ListeningExecutorService} that uses the
 * main Minecraft thread to execute tasks.</p>
 * <p>Limited scheduling is available via the {@link #schedule(Runnable, long)} method.</p>
 * <p>This ExecutorService cannot be shut down or terminated.</p>
 * <p>If tasks are added from inside a task executed by this Scheduler, they will be executed in the same tick as the
 * task adding the new tasks. If a task is scheduled from inside another task, the current tick will count as the first
 * waiting tick.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Scheduler extends AbstractListeningExecutorService {

    private static final Scheduler server;
    private static final Scheduler client;

    /**
     * <p>Return a Scheduler that executes tasks on the main server thread.</p>
     *
     * @return a Scheduler
     */
    public static Scheduler server() {
        return server;
    }

    /**
     * <p>The Scheduler that executes tasks on the main client thread. On a dedicated server this method will return null.</p>
     *
     * @return a Scheduler or null
     */
    public static Scheduler client() {
        return client;
    }

    /**
     * <p>Return {@link #client()} if {@code side} is {@code Side.CLIENT}, {@link #server()} otherwise.</p>
     *
     * @param side the side
     * @return a Scheduler for the side
     */
    public static Scheduler forSide(Side side) {
        return side == Side.CLIENT ? client : server;
    }

    /**
     * <p>Execute the given task after {@code tickDelay} ticks have passed.</p>
     *
     * @param r         the task
     * @param tickDelay the delay, in ticks
     */
    public void schedule(Runnable r, long tickDelay) {
        checkArgument(tickDelay >= 0);
        execute(new WaitingTask(r, tickDelay));
    }

    @Override
    public void execute(Runnable task) {
        execute(new WrappedRunnable(task));
    }

    public void execute(Task task) {
        inputQueue.offer(task);
    }

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            client = new Scheduler();
        } else {
            client = null;
        }
        server = new Scheduler();
    }

    // this is the queue that holds new tasks until they are picked up by the main thread
    private final ConcurrentLinkedQueue<Task> inputQueue = new ConcurrentLinkedQueue<>();

    // only used by the main thread
    private Task[] activeTasks = new Task[5];
    private int size = 0; // actual number of tasks in the above array, used for adding to the end

    public void tick() {
        Task[] activeTasks = this.activeTasks;
        int size = this.size;
        {
            // handle existing tasks

            // move through task list and simultaneously execute tasks and compact the list
            // by moving non-removed tasks to the new end of the list if needed
            int idx = 0, free = -1;
            while (idx < size) {
                Task t = activeTasks[idx];
                if (!checkedExecute(t)) {
                    // task needs to be removed, null out it's slot
                    activeTasks[idx] = null;
                    if (free == -1) {
                        // if this is the first task to be removed, set it as the compaction target
                        free = idx;
                    }
                } else if (free != -1) {
                    // we had to remove one or more tasks earlier in the list,
                    // move this one there to keep the list continuous
                    activeTasks[free++] = t;
                    activeTasks[idx] = null;
                }
                idx++;
            }
            // we had to remove at least one task, adjust the size
            if (free != -1) {
                this.size = free;
            }
        }
        {
            // handle new tasks
            Task task;
            while ((task = inputQueue.poll()) != null) {
                // only add task to the active list if it wants to keep executing
                // avoids unnecessary work for one-off tasks
                if (checkedExecute(task)) {
                    if (size == activeTasks.length) {
                        // we are full
                        Task[] newArr = new Task[size << 1];
                        System.arraycopy(activeTasks, 0, newArr, 0, size);
                        activeTasks = this.activeTasks = newArr;
                    }
                    activeTasks[size] = task;
                    this.size++;
                }
            }
        }
    }

    private static boolean checkedExecute(Task task) {
        try {
            return task.execute();
        } catch (Throwable x) {
            ModularRouters.logger.error(String.format("Exception thrown during execution of %s", task));
            return false;
        }
    }

    public interface Task {

        /**
         * <p>Execute this task, return true to keep executing.</p>
         *
         * @return true to keep executing
         */
        boolean execute();

    }

    private static final class WaitingTask implements Task {

        private final Runnable r;
        private long ticks;

        WaitingTask(Runnable r, long ticks) {
            this.r = r;
            this.ticks = ticks;
        }

        @Override
        public boolean execute() {
            if (--ticks == 0) {
                r.run();
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String toString() {
            return String.format("Scheduled task (task=%s, remainingTicks=%s)", r, ticks);
        }
    }

    private static class WrappedRunnable implements Task {
        private final Runnable task;

        public WrappedRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public boolean execute() {
            task.run();
            return false;
        }

        @Override
        public String toString() {
            return task.toString();
        }
    }

    /**
     * @return always false
     * @deprecated always false, this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public boolean isShutdown() {
        return false;
    }

    /**
     * @return always false
     * @deprecated always false, this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public boolean isTerminated() {
        return false;
    }

    /**
     * @deprecated this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public void shutdown() {
    }

    /**
     * @return a list of all waiting tasks
     * @deprecated this ExecutorService cannot be shut down
     */
    @Nonnull
    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    /**
     * @param timeout the timeout
     * @param unit    TimeUnit
     * @return always false
     * @throws InterruptedException
     * @deprecated this ExecutorService cannot be shut down, always returns false after sleeping for the specified
     * amount of time
     */
    @Override
    @Deprecated
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long millis = unit.toMillis(timeout);
        long milliNanos = TimeUnit.MILLISECONDS.toNanos(millis);
        int additionalNanos = Ints.saturatedCast(unit.toNanos(timeout) - milliNanos);
        Thread.sleep(millis, additionalNanos);
        return false;
    }

    private Scheduler() {
    }

}
