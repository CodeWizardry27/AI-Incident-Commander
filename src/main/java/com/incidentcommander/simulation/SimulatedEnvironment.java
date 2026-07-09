package com.incidentcommander.simulation;

import com.incidentcommander.agent.core.AgentContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * SimulatedEnvironment — Provides realistic scenario data for demo purposes.
 * 
 * Each scenario contains simulated: logs, metrics, DB status, git commits,
 * slow queries, deployment history, and API health data.
 * 
 * In production, these would come from real monitoring systems 
 * (Elasticsearch, Prometheus, GitHub API, etc.)
 */
@Component
public class SimulatedEnvironment {

    public record Scenario(String id, String title, String description, String severity) {}

    /** Get all available demo scenarios. */
    public List<Scenario> getScenarios() {
        return List.of(
            new Scenario("db_pool_exhaustion", "DB Connection Pool Exhaustion",
                "The /api/payments endpoint is returning 500 Internal Server Error. " +
                "Users are unable to complete payments. Started approximately 2 hours ago.", "CRITICAL"),
            new Scenario("memory_leak", "Memory Leak in File Upload Service",
                "The file-upload service is consuming increasingly more memory. " +
                "Response times are degrading. OOM errors appearing.", "HIGH"),
            new Scenario("api_rate_limit", "Payment Gateway Rate Limiting",
                "Intermittent 429 Too Many Requests errors from the payment gateway. " +
                "About 30% of payment attempts are failing.", "HIGH")
        );
    }

    /** Load scenario data into an AgentContext. */
    public AgentContext loadScenario(String scenarioId, Long incidentId, String description) {
        AgentContext context = new AgentContext();
        context.setIncidentId(incidentId);
        context.setScenarioId(scenarioId);
        context.setIncidentDescription(description);

        Map<String, String> data = switch (scenarioId) {
            case "db_pool_exhaustion" -> getDbPoolScenario();
            case "memory_leak" -> getMemoryLeakScenario();
            case "api_rate_limit" -> getApiRateLimitScenario();
            default -> getDbPoolScenario(); // Default scenario
        };

        context.setScenarioData(data);
        return context;
    }

    private Map<String, String> getDbPoolScenario() {
        Map<String, String> data = new HashMap<>();

        data.put("logs", """
            [ERROR] 2024-01-15 14:23:01 - HikariPool-1 - Connection is not available, request timed out after 30000ms.
            [ERROR] 2024-01-15 14:23:02 - o.h.engine.jdbc.spi.SqlExceptionHelper - Cannot acquire connection from data source
            [ERROR] 2024-01-15 14:23:02 - PaymentController - Failed to process payment: Could not open JPA EntityManager
            [ERROR] 2024-01-15 14:23:05 - PaymentController - HTTP 500 Internal Server Error on POST /api/payments
            [ERROR] 2024-01-15 14:23:08 - HikariPool-1 - Connection is not available, request timed out after 30000ms.
            [ERROR] 2024-01-15 14:23:10 - BookingService - Failed to fetch bookings: Connection pool exhausted
            [WARN]  2024-01-15 14:22:45 - HikariPool-1 - Pool stats: total=20, active=20, idle=0, waiting=15
            [WARN]  2024-01-15 14:22:30 - HikariPool-1 - Pool stats: total=20, active=19, idle=1, waiting=8
            [INFO]  2024-01-15 14:20:00 - HikariPool-1 - Pool stats: total=20, active=5, idle=15, waiting=0
            [INFO]  2024-01-15 12:15:00 - DeploymentService - Deployment completed: payment-service v2.3.1
            """);

        data.put("metrics", """
            === SERVICE: payment-service ===
            CPU Usage:        45% (normal: 20-30%)
            Memory Usage:     68% (normal: 50-60%)
            Request Latency:  4500ms avg (normal: 120ms) ⚠️ CRITICAL
            Error Rate:       78% of requests failing ⚠️ CRITICAL
            Active Threads:   85 (normal: 15-25)
            Requests/sec:     12 (normal: 150)
            """);

        data.put("db_status", """
            === MySQL Connection Pool (HikariCP) ===
            Total Connections:    20 / 20 (MAX)
            Active Connections:   20 / 20 ⚠️ EXHAUSTED
            Idle Connections:     0
            Waiting Threads:      35 ⚠️ HIGH
            Avg Connection Time:  4500ms (normal: 5ms)
            Max Connection Time:  12000ms
            Connection Timeouts:  47 in last 30 minutes
            
            === Top Active Queries ===
            1. SELECT * FROM bookings WHERE status IN ('PENDING','CONFIRMED') — running for 8500ms
            2. SELECT * FROM bookings WHERE status IN ('PENDING','CONFIRMED') — running for 7200ms  
            3. SELECT * FROM bookings WHERE status IN ('PENDING','CONFIRMED') — running for 6800ms
            (17 more similar queries running...)
            """);

        data.put("api_health", """
            === API Health Check ===
            GET  /api/health          → 200 OK (12ms)
            GET  /api/users           → 200 OK (45ms)
            POST /api/payments        → 500 Internal Server Error (30001ms timeout) ⚠️
            GET  /api/bookings        → 500 Internal Server Error (30001ms timeout) ⚠️
            GET  /api/notifications   → 200 OK (23ms)
            """);

        data.put("git_commits", """
            === Recent Git Commits (last 24 hours) ===
            
            commit abc123f — 2 hours ago
            Author: dev.junior@company.com
            Message: "feat: add booking status filter to payment flow"
            Diff:
              - List<Booking> bookings = bookingRepo.findByUserId(userId);
              + List<Booking> bookings = bookingRepo.findAll(); // Get all bookings to filter
              + bookings = bookings.stream()
              +     .filter(b -> b.getStatus().equals("PENDING") || b.getStatus().equals("CONFIRMED"))
              +     .collect(Collectors.toList());
            Note: Changed from indexed userId lookup to full table scan (findAll)
            
            commit def456a — 5 hours ago  
            Author: dev.senior@company.com
            Message: "fix: update notification template"
            Diff: Minor text change in email template (no DB queries affected)
            
            commit ghi789b — 12 hours ago
            Author: devops@company.com
            Message: "chore: update Spring Boot to 3.2.1"
            Diff: pom.xml version bump only
            """);

        data.put("slow_queries", """
            === MySQL Slow Query Log (queries > 1000ms) ===
            
            Query: SELECT * FROM bookings WHERE status IN ('PENDING','CONFIRMED')
            Execution Time: 4500ms
            Rows Examined: 2,847,391 (FULL TABLE SCAN ⚠️)
            Rows Returned: 1,247
            Called: 85 times in last 30 minutes
            Table Size: 2.8M rows
            Indexes: PRIMARY (id), idx_user_id (user_id) — NO INDEX ON status COLUMN ⚠️
            
            Query: SELECT COUNT(*) FROM bookings  
            Execution Time: 1200ms
            Rows Examined: 2,847,391
            """);

        data.put("deployments", """
            === Deployment History (last 48 hours) ===
            
            #1. payment-service v2.3.1 — deployed 2 hours ago ← MOST RECENT
                Deployer: dev.junior@company.com
                Status: SUCCESS
                Changes: Added booking status filter feature
                
            #2. notification-service v1.8.0 — deployed 5 hours ago
                Deployer: dev.senior@company.com  
                Status: SUCCESS
                Changes: Updated email templates
                
            #3. gateway v3.1.2 — deployed 24 hours ago
                Deployer: devops@company.com
                Status: SUCCESS
                Changes: Routine dependency updates
            """);

        return data;
    }

    private Map<String, String> getMemoryLeakScenario() {
        Map<String, String> data = new HashMap<>();

        data.put("logs", """
            [ERROR] 2024-01-15 16:45:01 - java.lang.OutOfMemoryError: Java heap space
            [ERROR] 2024-01-15 16:44:50 - FileUploadService - Failed to process upload: GC overhead limit exceeded
            [WARN]  2024-01-15 16:44:30 - GC Monitor - Full GC triggered: 95% heap used
            [WARN]  2024-01-15 16:43:00 - GC Monitor - Major GC: 89% heap used, freed only 2%
            [WARN]  2024-01-15 16:40:00 - GC Monitor - Minor GC: 82% heap used
            [INFO]  2024-01-15 16:00:00 - GC Monitor - Heap usage: 45% (normal)
            [INFO]  2024-01-15 14:00:00 - DeploymentService - Deployed file-upload-service v1.5.0
            """);

        data.put("metrics", """
            === SERVICE: file-upload-service ===
            CPU Usage:        92% (normal: 15%) ⚠️ CRITICAL — mostly GC
            Memory Usage:     98% (normal: 40-50%) ⚠️ CRITICAL
            Heap Used:        3.8GB / 4GB ⚠️
            GC Pause Time:    8500ms avg ⚠️
            Request Latency:  12000ms avg (normal: 200ms)
            Error Rate:       45% failing
            """);

        data.put("db_status", """
            === MySQL Connection Pool ===
            Total: 20, Active: 3, Idle: 17, Waiting: 0
            Status: HEALTHY — database is not the issue
            """);

        data.put("api_health", """
            POST /api/upload    → 500 (OutOfMemoryError) ⚠️
            GET  /api/files     → 200 OK (8500ms — slow due to GC)
            GET  /api/health    → 200 OK (15ms)
            """);

        data.put("git_commits", """
            commit mem456 — 3 hours ago
            Author: dev.backend@company.com
            Message: "feat: add large file upload support"
            Diff:
              public void processUpload(MultipartFile file) {
              -   try (InputStream is = file.getInputStream()) {
              -       // Process with streaming
              -       streamProcessor.process(is);
              -   }
              +   byte[] fileBytes = file.getBytes(); // Load entire file into memory
              +   processor.process(fileBytes);
              +   // Note: fileBytes array is never explicitly freed
              }
            """);

        data.put("slow_queries", "No slow queries found — database is healthy.");

        data.put("deployments", """
            #1. file-upload-service v1.5.0 — deployed 3 hours ago
                Changes: Added large file upload support
            #2. api-gateway v3.0.1 — deployed 2 days ago
                Changes: Routine update
            """);

        return data;
    }

    private Map<String, String> getApiRateLimitScenario() {
        Map<String, String> data = new HashMap<>();

        data.put("logs", """
            [ERROR] 2024-01-15 18:30:01 - RazorpayClient - HTTP 429 Too Many Requests
            [ERROR] 2024-01-15 18:30:02 - PaymentService - Payment failed: Rate limit exceeded
            [WARN]  2024-01-15 18:29:55 - RazorpayClient - Rate limit warning: 95/100 requests used
            [ERROR] 2024-01-15 18:29:50 - RazorpayClient - HTTP 429 Too Many Requests
            [INFO]  2024-01-15 18:29:00 - RetryScheduler - Batch retry job started: processing 500 failed payments
            [INFO]  2024-01-15 18:00:00 - PaymentService - Normal operations, 50 payments/min
            """);

        data.put("metrics", """
            === SERVICE: payment-service ===
            CPU Usage:        25% (normal)
            Memory Usage:     55% (normal)
            Request Latency:  350ms avg (normal: 200ms) — slightly elevated
            Error Rate:       32% of payment requests failing ⚠️
            Razorpay API calls: 180/min (limit: 100/min) ⚠️ EXCEEDING RATE LIMIT
            """);

        data.put("db_status", "Database is healthy. Connection pool: 5/20 active.");

        data.put("api_health", """
            POST /api/payments       → 429 Too Many Requests (intermittent) ⚠️
            GET  /api/payment-status → 200 OK
            GET  /api/health         → 200 OK
            """);

        data.put("git_commits", """
            commit rate789 — 1 hour ago
            Author: dev.backend@company.com
            Message: "feat: add batch retry for failed payments"
            Diff:
              + @Scheduled(fixedRate = 60000) // Every 1 minute
              + public void retryFailedPayments() {
              +     List<Payment> failed = paymentRepo.findByStatus("FAILED");
              +     failed.forEach(p -> razorpayClient.charge(p)); // No rate limiting!
              + }
            Note: Batch retry fires every minute, processing ALL failed payments at once
            """);

        data.put("slow_queries", "No slow queries — database is not the bottleneck.");

        data.put("deployments", """
            #1. payment-service v2.4.0 — deployed 1 hour ago
                Changes: Added batch retry for failed payments
            """);

        return data;
    }
}
