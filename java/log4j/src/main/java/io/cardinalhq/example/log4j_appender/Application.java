package io.cardinalhq.example.log4jappender;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.MapMessage;

public class Application {

  private static final org.apache.logging.log4j.Logger logger =
      LogManager.getLogger("log4j-logger");

  public static void main(String[] args) {
    // Initialize OpenTelemetry as early as possible
    OpenTelemetry openTelemetry = initializeOpenTelemetry();

    // Install OpenTelemetry in log4j appender
    io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender.install(
        openTelemetry);
    
    // Log using log4j API
    logger.info("This is a simple info log message");
    
    Map<String, Object> mapMessage = new HashMap<>();
    mapMessage.put("key", "value");
    mapMessage.put("message", "This is a structured log message");
    logger.info(new MapMessage<>(mapMessage));
    
    logger.error("This is an error log message with an exception", new Exception("error!"));
    
    // Continuous logging loop to keep the app running
    logger.info("Starting continuous logging loop...");
    int messageCount = 1;
    
    try {
      while (true) {
        // Log different types of messages
        logger.info("Continuous log message #{} - Application is running", messageCount);
        
        if (messageCount % 5 == 0) {
          logger.warn("Warning message #{} - This is a periodic warning", messageCount);
        }
        
        if (messageCount % 10 == 0) {
          logger.error("Error message #{} - Simulated error for testing", messageCount, 
              new RuntimeException("Simulated error #" + messageCount));
        }
        
        // Add some structured logging
        Map<String, Object> periodicMap = new HashMap<>();
        periodicMap.put("messageNumber", messageCount);
        periodicMap.put("status", "running");
        logger.info(new MapMessage<>(periodicMap));
        
        messageCount++;
        
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      logger.info("Logging loop interrupted, shutting down...");
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("Unexpected error in logging loop", e);
    }
  }

  private static OpenTelemetry initializeOpenTelemetry() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .setResource(
                        Resource.getDefault().toBuilder()
                            .put(SERVICE_NAME, "log4j-example")
                            .build())
                    .addLogRecordProcessor(
                        BatchLogRecordProcessor.builder(
                                OtlpGrpcLogRecordExporter.builder()
                                    .setEndpoint("http://localhost:4317")
                                    .build())
                            .build())
                    .build())
            .build();

    // Add hook to close SDK, which flushes logs
    Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));

    return sdk;
  }
}
