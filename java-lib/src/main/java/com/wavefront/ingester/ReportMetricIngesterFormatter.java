package com.wavefront.ingester;

import com.wavefront.common.Clock;

import wavefront.report.ReportMetric;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder pattern for creating new ingestion formats. Inspired by the date time formatters in
 * Joda.
 *
 * @author Clement Pang (clement@wavefront.com).
 */
public class ReportMetricIngesterFormatter extends AbstractIngesterFormatter<ReportMetric> {

  private ReportMetricIngesterFormatter(List<FormatterElement<ReportMetric>> elements) {
    super(elements);
  }

  public static class ReportMetricIngesterFormatBuilder
      extends IngesterFormatBuilder<ReportMetric> {
    @Override
    public ReportMetricIngesterFormatter build() {
      return new ReportMetricIngesterFormatter(elements);
    }
  }

  public static IngesterFormatBuilder<ReportMetric> newBuilder() {
    return new ReportMetricIngesterFormatBuilder();
  }

  @Override
  public ReportMetric drive(String input, Supplier<String> defaultHostNameSupplier,
                           String customerId, @Nullable List<String> customSourceTags,
                           @Nullable IngesterContext ingesterContext) {
    ReportMetric point = new ReportMetric();
    point.setTable(customerId);
    // if the point has a timestamp, this would be overriden
    point.setTimestamp(Clock.now());
    final StringParser parser = new StringParser(input);

    try {
      for (FormatterElement<ReportMetric> element : elements) {
        if (ingesterContext != null) {
          element.consume(parser, point, ingesterContext);
        } else {
          element.consume(parser, point);
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException("Could not parse: " + input, ex);
    }
    if (parser.hasNext()) {
      throw new RuntimeException("Unexpected extra input: " + parser.next());
    }

    String host = AbstractIngesterFormatter.getHost(point.getAnnotations(), customSourceTags);
    if (host == null) {
      if (defaultHostNameSupplier == null) {
        host = "unknown";
      } else {
        host = defaultHostNameSupplier.get();
      }
    }
    point.setHost(host);
    return point;
  }
}
