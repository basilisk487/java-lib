package com.wavefront.ingester;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import wavefront.report.ReportMetric;

import static junit.framework.Assert.assertEquals;

/**
 * @author vasily@wavefront.com
 */
public class SpyApiMetricDecoderTest {

  @Test
  public void testSimple() throws Exception {
    SpyApiMetricDecoder decoder = new SpyApiMetricDecoder();
    List<ReportMetric> out = Lists.newArrayList();
    decoder.decode("\"sldb.coexisting_tags.bloomfilter.count\" " +
        "source=\"surf-2c-app93-i-08de348a716bd3127\" 1581459686000 1.0 \"service\"=\"engine\" " +
        "\"entity\"=\"span\" \"_wavefront_source\"=\"proxy::wavefront-proxy-canary-1\" " +
        "\"customer\"=\"ts-2018-enablement-39\"", out, "customer");
    ReportMetric point = out.get(0);
    assertEquals("customer", point.getTable());
    assertEquals("sldb.coexisting_tags.bloomfilter.count", point.getMetric());
    assertEquals("surf-2c-app93-i-08de348a716bd3127", point.getHost());
    assertEquals(1.0, point.getValue());
    assertEquals(4, point.getAnnotations().size());
    assertEquals("engine", point.getAnnotations().get("service"));
    assertEquals("span", point.getAnnotations().get("entity"));
    assertEquals("proxy::wavefront-proxy-canary-1", point.getAnnotations().get("_wavefront_source"));
    assertEquals("ts-2018-enablement-39", point.getAnnotations().get("customer"));
  }
}
