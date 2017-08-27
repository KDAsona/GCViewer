package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.*;
import org.junit.Test;

/**
 * Created by Mart on 10/05/2017.
 */
public class TestDataReaderShenandoah {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderShenandoah(gcResource, getInputStream(gcResource.getResourceName()));
    }

    @Test
    public void parseBasicEvent() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahBasic.txt");
        assertThat("size", model.size(), is(5));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(2));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(3));
        assertThat("total pause time", model.getPause().getSum(), is(0.001318));
        assertThat("gc pause time", model.getGCPause().getSum(), is(0.001318));
        assertThat("full gc pause time", model.getFullGCPause().getSum(), is(0.0));
        assertThat("heap size after concurrent cycle", model.getPostConcurrentCycleHeapUsedSizes().getMax(), is(33 * 1024));
        assertThat("max memory freed during STW pauses", model.getFreedMemoryByGC().getMax(), is(34 * 1024));
    }

    @Test
    public void parseAllocationFailure() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahAllocationFailure.txt");
        assertThat("size", model.size(), is(1));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
        assertThat("total pause time", model.getPause().getSum(), is(14.289335));
        assertThat("gc pause time", model.getGCPause().getSum(), is(0.0));
        assertThat("full gc pause time", model.getFullGCPause().getSum(), is(14.289335));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_ALLOC_FAILURE.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(7943 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(6013 * 1024));
        assertThat("total heap size", event.getTotal(), is(8192 * 1024));
        assertThat("timestamp", event.getTimestamp(), is(43.948));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parseDefaultConfiguration() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahDefaultConfiguration.txt");

        assertThat("size", model.size(), is(140));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_SYSTEM_GC.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("timestamp", event.getTimestamp(), is(1.337));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parsePassiveHeuristics() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahPassiveHeuristics.txt");
        assertThat("size", model.size(), is(0));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
    }


    @Test
    public void parseAggressiveHeuristics() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahAggressiveHeuristics.txt");
        assertThat("size", model.size(), is(549));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_INIT_MARK.toString()));

        ConcurrentGCEvent event2 = (ConcurrentGCEvent) model.get(1);
        assertThat("type", event2.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_MARK.toString()));
        assertThat("preUsed heap size", event2.getPreUsed(), is(90 * 1024));
        assertThat("postUsed heap size", event2.getPostUsed(), is(90 * 1024));
        assertThat("total heap size", event2.getTotal(), is(128 * 1024));
        assertThat("timestamp", event2.getTimestamp(), is(8.350));
        assertThat("generation", event2.getGeneration(), is(AbstractGCEvent.Generation.TENURED));
    }

    @Test
    public void parseSingleSystemGCEvent() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahSingleSystemGC.txt");
        assertThat("size", model.size(), is(353));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_SYSTEM_GC.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("timestamp", event.getTimestamp(), is(1.481));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parseSeveralSystemGCEvents() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahSeveralSystemGC.txt");
        assertThat("size", model.size(), is(438));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_SYSTEM_GC.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
        assertThat("timestamp", event.getTimestamp(), is(1.303));
    }

    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile(fileName);
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();
        assertThat("model format", model.getFormat(), is(GCModel.Format.RED_HAT_SHENANDOAH_GC));
        assertThat("number of errors", handler.getCount(), is(0));
        return model;
    }
}
