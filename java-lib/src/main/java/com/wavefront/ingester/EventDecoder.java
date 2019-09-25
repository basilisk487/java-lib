package com.wavefront.ingester;

import wavefront.report.Event;

import java.util.List;

/**
 * Span decoder that takes in data in the following format:
 *
 * { @Event|@OngoingEvent } startTimeMillis [endTimeMillis] eventName [annotations]
 *
 * @author vasily@wavefront.com
 */
public class EventDecoder implements ReportableEntityDecoder<String, Event> {

  public static final String EVENT = "@Event";
  public static final String ONGOING_EVENT = "@OngoingEvent";

  private static final AbstractIngesterFormatter<Event> FORMAT =
      EventIngesterFormatter.newBuilder()
          .whiteSpace()
          .appendCaseSensitiveLiterals(new String[]{EVENT, ONGOING_EVENT}).whiteSpace()
          .appendTimestamp().whiteSpace()
          .appendOptionalEndTimestamp().whiteSpace()
          .appendName().whiteSpace()
          .appendAnnotationsConsumer().whiteSpace()
          .build();

  public EventDecoder() {
  }

  @Override
  public void decode(String msg, List<Event> out, String customerId) {
    Event event = FORMAT.drive(msg, "default", "default");
    if (out != null) {
      out.add(event);
    }
  }
}
