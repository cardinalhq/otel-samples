package main

import (
	"log/slog"
	"os"

	slogmulti "github.com/samber/slog-multi"
	"go.opentelemetry.io/contrib/bridges/otelslog"
)

// This example demonstrates how to use the otelslog bridge to send logs to OpenTelemetry.
// It requires the OTEL_SERVICE_NAME environment variable to be set.
//
// You will need to set up the other bits for an OpenTelemetry collector to receive the logs,
// including setting up the OpenTelemetry SDK to export logs.
//
// Order here does not matter; if slog is set up this way prior to the log exporter being set up,
// exporting to otel will become a no-op.  As soon as the exporter is configured, future slog
// calls will be sent to the OpenTelemetry collector in addition to the stdout JSON handler.
func main() {
	serviceName := os.Getenv("OTEL_SERVICE_NAME")
	if serviceName == "" {
		panic("OTEL_SERVICE_NAME environment variable is not set")
	}

	slog.SetDefault(slog.New(slogmulti.Fanout(
		slog.NewJSONHandler(os.Stdout, nil),
		otelslog.NewHandler(serviceName),
	)))

	slog.Info("Hello, world!")
}
