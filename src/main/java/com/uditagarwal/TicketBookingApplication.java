package com.uditagarwal;

import com.uditagarwal.demo.ScenarioDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class TicketBookingApplication {

    /**
     * No web server: this is a CLI demo of the LLD.
     *
     *   mvn spring-boot:run
     *   java -jar target/lld-ticket-booking-1.0.0.jar
     *
     * The context is closed explicitly so the scheduler thread does not keep
     * the JVM alive, and the process exits non-zero if any scenario check fails.
     */
    public static void main(String[] args) {
        final int exitCode;
        try (ConfigurableApplicationContext context =
                     SpringApplication.run(TicketBookingApplication.class, args)) {
            final ScenarioDemo.DemoReport report = context.getBean(ScenarioDemo.class).runAll();
            exitCode = report.isSuccess() ? 0 : 1;
        }
        System.exit(exitCode);
    }
}
