package com.ofnull.fastpig.spi.recordprocessor;

import com.ofnull.fastpig.spi.job.IJobConfiguration;
import org.apache.flink.shaded.guava30.com.google.common.collect.Lists;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public interface IRecordProcessor<T> extends Closeable {

    default void setJobConfiguration(IJobConfiguration jobConfiguration) {

    }

    default void init(T config) throws Exception {

    }

    Class<T> argsType();

    default void doTransform(IJsonPathSupportedMap event, T config, ProcessContext context) throws Exception {

    }

    @Override
    default void close() throws IOException {

    }

    public static class ProcessContext {
        private IJobConfiguration jobConfiguration;
        private List<IJsonPathSupportedMap> appendEvents = new CopyOnWriteArrayList<>();
        private List<Integer> discardIndies = Lists.newArrayList();
        private int currentEventIndex = -1;

        public IJobConfiguration getJobConfiguration() {
            return jobConfiguration;
        }

        public void setJobConfiguration(IJobConfiguration jobConfiguration) {
            this.jobConfiguration = jobConfiguration;
        }

        public List<IJsonPathSupportedMap> getAppendEvents() {
            return appendEvents;
        }

        public void setAppendEvents(List<IJsonPathSupportedMap> appendEvents) {
            this.appendEvents = appendEvents;
        }

        public List<Integer> getDiscardIndies() {
            return discardIndies;
        }

        public void setDiscardIndies(List<Integer> discardIndies) {
            this.discardIndies = discardIndies;
        }

        public int getCurrentEventIndex() {
            return currentEventIndex;
        }

        public void setCurrentEventIndex(int currentEventIndex) {
            this.currentEventIndex = currentEventIndex;
        }

        public void incrCurrentIndex() {
            this.currentEventIndex++;
        }

        public void markCurrentEventDiscard() {
            discardIndies.add(currentEventIndex);
        }
    }


}
