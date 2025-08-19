plugins {
    id("java")
    id("java-library")
    id("application")
}

apply(plugin = "java-library")

group = "io.cardinalhq"
version = "0.1.0-SNAPSHOT"

description = "OpenTelemetry log4j Example"
val moduleName by extra { "io.cardinalhq.example.log4jappender" }

repositories {
    mavenCentral()
}

dependencies {
    // Log4j
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.25.1"))
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")

    // OpenTelemetry core
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:2.19.0-alpha"))
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry.semconv:opentelemetry-semconv")

    // OpenTelemetry log4j appender
    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-appender-2.17")
}

application {
    mainClass = "io.cardinalhq.example.log4jappender.Application"
}

// Configure jar task to create executable JAR
tasks.jar {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    archiveVersion.set("")
    
    manifest {
        attributes(mapOf(
            "Main-Class" to "io.cardinalhq.example.log4jappender.Application"
        ))
    }
    
    // Include all dependencies in the JAR
    from(configurations.runtimeClasspath.map { config ->
        config.map { if (it.isDirectory) it else zipTree(it) }
    })
    
    // Avoid duplicate files
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
