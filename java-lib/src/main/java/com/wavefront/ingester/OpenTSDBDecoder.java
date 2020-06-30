package com.wavefront.ingester;

import com.google.common.base.Preconditions;

import java.util.List;

import com.google.common.collect.ImmutableList;

import wavefront.report.ReportMetric;
import wavefront.report.ReportMetric;

/**
 * OpenTSDB decoder that takes in a point of the type:
 *
 * PUT [metric] [timestamp] [value] [annotations]
 *
 * @author Clement Pang (clement@wavefront.com).
 */
public class OpenTSDBDecoder implements ReportableEntityDecoder<String, ReportMetric> {

  private static final AbstractIngesterFormatter<ReportMetric> FORMAT =
      ReportMetricIngesterFormatter.newBuilder().
          caseInsensitiveLiterals(ImmutableList.of("put")).
          text(ReportMetric::setMetric).
          timestamp(ReportMetric::setTimestamp).
          value(ReportMetric::setValue).
          annotationMap(ReportMetric::setAnnotations).
          build();
  private final String hostName;
  private final List<String> customSourceTags;

  public OpenTSDBDecoder(List<String> customSourceTags) {
    this("unknown", customSourceTags);
  }

  public OpenTSDBDecoder(String hostName, List<String> customSourceTags) {
    Preconditions.checkNotNull(hostName);
    Preconditions.checkNotNull(customSourceTags);
    this.hostName = hostName;
    this.customSourceTags = customSourceTags;
  }

  @Override
  public void decode(String msg, List<ReportMetric> out, String customerId, IngesterContext ctx) {
    ReportMetric point = FORMAT.drive(msg, () -> hostName, customerId, customSourceTags, ctx);
    if (out != null) {
      out.add(point);
    }
  }
}