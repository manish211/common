/*
 * Copyright [yyyy] [name of copyright owner]
 * 
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ====================================================================
 */

package com.lafaspot.common.concurrent.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.lafaspot.common.concurrent.BlockManagerMaxCount;
import com.lafaspot.common.concurrent.Worker;
import com.lafaspot.common.concurrent.WorkerBlockManager;
import com.lafaspot.common.concurrent.WorkerException;
import com.lafaspot.common.concurrent.WorkerFuture;

/**
 * Unit tests for {@code WorkerWrapper}.
 *
 * @author davisthomas
 *
 */
public class WorkerWrapperTest {

    /**
     * Test cancellation of a worker.
     *
     * @throws WorkerException when worker throws error
     */
    @Test
    public void testCancellation() throws WorkerException {
        final WorkerWrapper wrapper = new WorkerWrapper<>(worker);
        wrapper.cancel(false);
        wrapper.execute();
        final WorkerStats stats = wrapper.getStat();
        Assert.assertTrue(stats.getStatsAsString().contains("WorkerCancelCount=1"));
        WorkerFuture future = wrapper.getFuture();
        try {
            future.get(1, TimeUnit.MILLISECONDS);
            Assert.fail("Cancelation exception was expected.");
        } catch (final CancellationException e) {
            Assert.assertEquals(e.getClass().getName(), CancellationException.class.getName());
        } catch (final TimeoutException | ExecutionException | InterruptedException e) {
            Assert.fail("Cancelation exception was expected.");
        }
    }

    /**
     * Test worker done without exception.
     *
     * @throws WorkerException when worker throws error
     * @throws ExecutionException when execution error
     * @throws InterruptedException when interrupted error
     */
    @Test
    public void testWorkerDoneWithoutException() throws WorkerException, InterruptedException, ExecutionException {
        final WorkerWrapper wrapper = new WorkerWrapper<>(worker);
        wrapper.execute();
        WorkerFuture future = wrapper.getFuture();
        Integer val = (Integer) future.get();
        Assert.assertEquals(val.intValue(), 1);
    }

    /**
     * Test worker done with exception.
     *
     * @throws WorkerException when worker throws error
     */
    @Test
    public void testWorkerDone() throws WorkerException {
        final WorkerWrapper wrapper = new WorkerWrapper<>(workerException);
        wrapper.execute();
        final WorkerFuture future = wrapper.getFuture();
        try {
            future.get();
            Assert.fail("Exception should have been thrown");
        } catch (final InterruptedException | ExecutionException e) {
            Assert.assertEquals(e.getMessage(), "java.lang.Exception: Exception to test ExcecutorService.");
        }
    }

    /**
     * test worker.
     */
    private final Worker<Integer> worker = new Worker<Integer>() {

        /** set of states. */
        private Set<BlockManagerMaxCount.State> blockManagerStates = new HashSet<>();
        /** block manager instance. */
        private WorkerBlockManager blockManager = new BlockManagerMaxCount(blockManagerStates);

        @Override
        public boolean execute() throws WorkerException {
            return true;
        }

        @Override
        public Exception getCause() {
            return null;
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public Integer getData() {
            return new Integer(1);
        }

        @Override
        public WorkerBlockManager getBlockManager() {
            return blockManager;
        }

        @Override
        public void cleanup() {
            blockManagerStates = null;
        }

    };

    /**
     * Test worker that returns an exception.
     */
    private final Worker<Integer> workerException = new Worker<Integer>() {

        /** set of states. */
        private Set<BlockManagerMaxCount.State> blockManagerStates = new HashSet<>();
        /** block manager instance. */
        private WorkerBlockManager blockManager = new BlockManagerMaxCount(blockManagerStates);

        @Override
        public boolean execute() throws WorkerException {
            return true;
        }

        @Override
        public Exception getCause() {
            return new Exception("Exception to test ExcecutorService.");
        }

        @Override
        public boolean hasErrors() {
            return true;
        }

        @Override
        public Integer getData() {
            return new Integer(1);
        }

        @Override
        public WorkerBlockManager getBlockManager() {
            return blockManager;
        }

        @Override
        public void cleanup() {
            blockManagerStates = null;
        }

    };

}
