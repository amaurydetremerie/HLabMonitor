# Architecture Overview

HLabMonitor follows **Hexagonal Architecture** (Ports & Adapters), keeping the domain logic independent from infrastructure concerns.

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 4.0.1 |
| Language | Java | 21 |
| Build | Maven | 3.9+ |
| Schema migrations | Liquibase | — |
| ORM | JPA / Hibernate | — |
| Scheduling | Spring Integration | — |
| HTTP client | Spring RestClient | — |
| Metrics | Micrometer / Prometheus | — |
| Test | JUnit 5, Testcontainers, ArchUnit | — |

## Package Structure

```
be.wiserisk.hlabmonitor.monitor
├── domain
│   ├── model          ← Target, TargetResult, Notification, TargetId, ...
│   ├── enums          ← MonitoringType, MonitoringResult, NotificationStatus, StatisticType
│   ├── exception      ← TargetNotFoundException, TargetDuplicatedException, ...
│   └── service        ← MonitoringService, NotificationService, ManageService, ...
│
├── application.port
│   ├── in             ← Use case interfaces (ExecuteCheckUseCase, ManageMonitoringConfigUseCase, ...)
│   └── out            ← Output port interfaces (CheckTargetPort, PersistencePort, NotificationPort, ...)
│
└── infrastructure
    ├── adapter.in
    │   ├── rest        ← REST controllers (CheckTargetsController, CheckResultsController, ...)
    │   └── notification ← NotificationSenders (DiscordSender, EmailSender, TelegramSender, LogSender, PrometheusSender, SseSender)
    ├── adapter.out
    │   ├── HttpCheckAdapter   ← PING, HTTP, CERTIFICATE checks
    │   └── persistence        ← JPA entities, repositories, JpaPersistenceAdapter
    └── config
        └── yaml       ← @ConfigurationProperties binding (Monitoring, DatabaseProperties, ...)
```

## Flow of a Monitoring Check

```
application.yaml
      │  (on startup or /management/reload)
      ▼
ManageService.syncFullConfiguration()
      │
      ├── persistencePort.createTarget() / updateTarget()
      └── schedulerPort.scheduleTarget()
                │  (every <interval>)
                ▼
        MonitoringService.executeCheck(targetId)
                │
                ├── persistencePort.getTarget()
                ├── checkPort.ping() / httpCheck() / certCheck()  ← HttpCheckAdapter
                ├── persistencePort.saveResult()
                └── NotificationService.handleNotification()
```

## Flow of a Notification

```
NotificationService.handleNotification(TargetResult)
        │
        ├── Check if a SEND notification already exists for this target
        │       If yes  → resolveNotificationIfResultChanged()
        │       If no   → sendNotificationIfResultChanged()
        │
        ├── If result family changed → create Notification (TO_SEND or TO_TERMINATE)
        │
        └── NotificationService.saveNotification()
                │
                ├── persistencePort.saveNotification()  (SEND or TERMINATED)
                └── notificationPort.sendNotification()
                        │
                        ├── DiscordSender
                        ├── EmailSender
                        ├── TelegramSender
                        ├── LogSender
                        ├── PrometheusSender  ← updates metrics
                        └── SseSender         ← broadcasts SSE count update
```

## Domain Model

```
Target
  id: TargetId         ← "<key>:ping" | "<key>:http" | "<key>:certificate"
  type: MonitoringType ← PING | HTTP | HTTP_INTERNAL | CERTIFICATE | SPEEDTEST(*) | UNKNOWN
  target: String       ← IP, hostname, or URL
  interval: Duration
  acceptableStatusCode: Integer  ← optional, used for HTTP checks

TargetResult
  id: TargetId
  result: MonitoringResult  ← SUCCESS | FAILURE | WARNING | ERROR | UNKNOWN
  message: String
  checkedAt: Instant

Notification
  notificationId: Long
  targetId: TargetId
  notificationStatus: NotificationStatus  ← TO_SEND | SEND | TO_TERMINATE | TERMINATED | FAILED
  fireAt: Instant
  resolvedAt: Instant
  oldMonitoringResult: MonitoringResult
  newMonitoringResult: MonitoringResult
```

(*) `SPEEDTEST` is declared but not implemented — throws `UnsupportedOperationException`.

## Result Families

Notifications are triggered on **family** transitions, not individual result changes:

| Family | Results |
|--------|---------|
| SUCCESS | `SUCCESS` |
| FAILURE | `FAILURE`, `WARNING`, `ERROR` |
| UNKNOWN | `UNKNOWN` |

## ArchUnit Tests

The project enforces architectural constraints using ArchUnit (see test classes). Key rules:
- Domain layer has no dependency on infrastructure
- Use cases are only implemented by domain services
- Adapters implement their port interfaces
