my-quarkus-project
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── project
│   │   │
│   │   │               ├── domain
│   │   │               │   ├── model
│   │   │               │   │   ├── Invoice.java
│   │   │               │   │   └── InvoiceStatus.java
│   │   │               │   │
│   │   │               │   ├── repository
│   │   │               │   │   └── InvoiceRepository.java   (Interface)
│   │   │               │   │
│   │   │               │   ├── event
│   │   │               │   │   └── InvoiceOverdueEvent.java
│   │   │               │   │
│   │   │               │   └── exception
│   │   │               │       └── InvalidInvoiceStateException.java
│   │   │               │
│   │   │               ├── application
│   │   │               │   ├── usecase
│   │   │               │   │   ├── ProcessOverdueInvoicesUseCase.java
│   │   │               │   │   ├── CreateInvoiceUseCase.java
│   │   │               │   │   └── PayInvoiceUseCase.java
│   │   │               │   │
│   │   │               │   ├── port
│   │   │               │   │   ├── in
│   │   │               │   │   │   └── InvoiceCommandPort.java
│   │   │               │   │   └── out
│   │   │               │   │       └── InvoiceRepositoryPort.java
│   │   │               │   │
│   │   │               │   └── dto
│   │   │               │       └── InvoiceDto.java
│   │   │               │
│   │   │               ├── infrastructure
│   │   │               │   ├── scheduler
│   │   │               │   │   └── InvoiceScheduler.java
│   │   │               │   │
│   │   │               │   ├── persistence
│   │   │               │   │   ├── entity
│   │   │               │   │   │   └── InvoiceEntity.java
│   │   │               │   │   └── repository
│   │   │               │   │       └── InvoiceRepositoryJpa.java
│   │   │               │   │
│   │   │               │   ├── messaging
│   │   │               │   │   └── InvoiceEventPublisher.java
│   │   │               │   │
│   │   │               │   └── config
│   │   │               │       └── SchedulerConfig.java
│   │   │               │
│   │   │               └── interfaces
│   │   │                   └── rest
│   │   │                       ├── InvoiceResource.java
│   │   │                       └── mapper
│   │   │                           └── InvoiceMapper.java
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       └── db
│   │           └── migration
│   │               └── V1__init.sql
│   │
│   └── test
│       └── java
│           └── com
│               └── example
│                   └── project
│                       ├── domain
│                       │   └── InvoiceTest.java
│                       ├── application
│                       │   └── usecase
│                       │       └── ProcessOverdueInvoicesUseCaseTest.java
│                       └── infrastructure
│                           └── scheduler
│                               └── InvoiceSchedulerTest.java
