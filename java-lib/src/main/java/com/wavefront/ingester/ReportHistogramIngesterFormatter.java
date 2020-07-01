package com.wavefront.ingester;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.wavefront.common.Clock;

import wavefront.report.ReportHistogram;

/**
 * Builder pattern for creating new ingestion formats. Inspired by the date time formatters in
 * Joda.
 *
 * @author Clement Pang (clement@wavefront.com).
 */
public class ReportHistogramIngesterFormatter extends AbstractIngesterFormatter<ReportHistogram> {

  private ReportHistogramIngesterFormatter(List<FormatterElement<ReportHistogram>> elements) {
    super(elements);
  }

  public static class ReportHistogramIngesterFormatBuilder
      extends IngesterFormatBuilder<ReportHistogram> {
    @Override
    public ReportHistogramIngesterFormatter build() {
      return new ReportHistogramIngesterFormatter(elements);
    }
  }

  public static IngesterFormatBuilder<ReportHistogram> newBuilder() {
    return new ReportHistogramIngesterFormatBuilder();
  }

  @Override
  public ReportHistogram drive(String input, Supplier<String> defaultHostNameSupplier,
                           String customerId, @Nullable List<String> customSourceTags,
                           @Nullable IngesterContext ingesterContext) {
    ReportHistogram histogram = new ReportHistogram();
    histogram.setCustomer(customerId);
    // if the point has a timestamp, this would be overriden
    histogram.setTimestamp(Clock.now());
    final StringParser parser = new StringParser(input);

    try {
      for (FormatterElement<ReportHistogram> element : elements) {
        if (ingesterContext != null) {
          element.consume(parser, histogram, ingesterContext);
        } else {
          element.consume(parser, histogram);
        }
      }
    } catch (TooManyCentroidException ex) {
      throw new TooManyCentroidException("Could not parse: " + input, ex);
    } catch (Exception ex) {
      throw new RuntimeException("Could not parse: " + input, ex);
    }
    if (parser.hasNext()) {
      throw new RuntimeException("Unexpected extra input: " + parser.next());
    }

    String host = AbstractIngesterFormatter.getHost(histogram.getAnnotations(), customSourceTags);
    if (host == null) {
      host = defaultHostNameSupplier.get();
    }
    histogram.setHost(host);
    return histogram;
  }
}
