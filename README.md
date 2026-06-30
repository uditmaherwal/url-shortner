# URL Shortener Service

A highly scalable, low-latency URL shortening API built with **Java**, **Spring Boot**, and **MongoDB**. Designed for high concurrency, it utilizes a pre-computed key generation strategy and asynchronous processing to deliver extremely fast URL assignment and redirection.

## Core Features

* **O(1) URL Generation:** Utilizes a thread-safe `ConcurrentLinkedQueue` to pre-compute Base-62 encoded short keys in batches, eliminating database bottlenecks during URL creation.
* **Atomic Distributed Counter:** Uses MongoDB's `findAndModify` to safely assign unique ID blocks across multiple application instances.
* **Asynchronous Analytics:** Tracks click metrics using Spring's `@Async`, ensuring that updating analytics never blocks or delays the user's redirect.
* **Containerized Infrastructure:** Ready to deploy via Docker Compose with integrated MongoDB, PostgreSQL, Redis, and Kafka services.
* **Graceful Degradation:** Automatic initialization of database counters and safe fallback handling for missing URLs.

## Tech Stack

* **Language:** Java 25
* **Framework:** Spring Boot 4.1.0
* **Database:** MongoDB (with Docker Compose configurations for PostgreSQL and Redis)
* **Concurrency:** `ConcurrentLinkedQueue`, `@Async`

---

## System Architecture

1. **Pre-computation:** On application startup, the service claims a block of 1,000 unique IDs from MongoDB, encodes them to Base-62, and stores them in memory.
2. **Shortening:** When a request arrives, the service pops an available key from the memory queue in `O(1)` time, mapping it to the original URL and saving it to MongoDB.
3. **Redirection:** When a user accesses a short URL, the application looks up the original URL, immediately triggers an asynchronous click-count increment, and returns an HTTP 302 redirect.

---

## Getting Started

### Prerequisites

* Java 25 or higher
* Docker and Docker Compose
* Maven

### Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/uditmaherwal/url-shortner.git](https://github.com/uditmaherwal/url-shortner.git)
   cd url-shortner
