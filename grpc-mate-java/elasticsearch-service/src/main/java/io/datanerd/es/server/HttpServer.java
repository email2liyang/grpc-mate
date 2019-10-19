package io.datanerd.es.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

import fi.iki.elonen.NanoHTTPD;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Singleton
public class HttpServer extends NanoHTTPD {

  private static Logger log = LoggerFactory.getLogger(HttpServer.class); //NOPMD

  @Inject
  public HttpServer() {
    super(9090);
  }

  @Override
  public void start() throws IOException {
    super.start();
    log.info("metrics NanoHTTPD started.");
  }

  @Override
  public Response serve(IHTTPSession session) {
    Response response;
    switch (session.getUri()) {
      case "/health":
        try {
          String status = "OK";
          response = newFixedLengthResponse(status);
        } catch (Exception e) {
          String status = "NOT_HEALTHY";
          response = newFixedLengthResponse(status);
        }
        break;
      case "/metrics":
        try {
          String metricsPage = getMetricsAsString();
          response = newFixedLengthResponse(Response.Status.OK, TextFormat.CONTENT_TYPE_004, metricsPage);
        } catch (IOException e) {
          response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "plain/text", e.getLocalizedMessage());
        }
        break;
      default:
        response = newFixedLengthResponse(Response.Status.NOT_FOUND, "plain/text", "Not found");
    }
    return response;
  }

  private String getMetricsAsString() throws IOException {
    StringWriter sw = new StringWriter();
    TextFormat.write004(sw, CollectorRegistry.defaultRegistry.metricFamilySamples());
    return sw.toString();
  }
}
