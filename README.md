# pulse

**P**rocessing **U**nified **L**ive **S**treaming **E**vents

Repository containing the application code for PULSE, an event-driven microservices platform for real-time transaction 
fraud risk assessment. Transactions are submitted via a REST API, scored by a rules-based processor, and persisted to a 
query store — all asynchronously through a message queue.

## **Table of contents**

- [pulse](#pulse)
  - [Prerequisites](#prerequisites)
  - [Repository Structure](#repository-structure)
  - [Tooling](#tooling)
    - [pre-commit](#pre-commit)
  - [Running locally](#running-locally)
    - [Docker Compose](#docker-compose)
    - [Kubernetes](#kubernetes)
  - [Building and testing](#building-and-testing)
  - [Deployment strategy](#deployment-strategy)

## Prerequisites

- [**Docker**](https://docs.docker.com/get-docker/) and [**Docker Compose**](https://docs.docker.com/compose/install/) (included with Docker Desktop)
- [**JDK 21**](https://adoptium.net/temurin/releases/?version=21)
- [**Maven 3.9+**](https://maven.apache.org/install.html)
- [**kubectl**](https://kubernetes.io/docs/tasks/tools/) (Kubernetes only)
- [**Kustomize**](https://kubectl.docs.kubernetes.io/installation/kustomize/) (Kubernetes only)
- [**kind**](https://kind.sigs.k8s.io/docs/user/quick-start/) or [**minikube**](https://minikube.sigs.k8s.io/docs/start/) (Kubernetes only)
- [**pre-commit**](https://pre-commit.com/#install)

## Repository Structure

```
pulse/
|-- .github/
|   `-- workflows/
|       |-- ci.yml                   // Lint and test on pull requests
|       `-- cd.yml                   // Build and push images to Artifact Registry on merge to main
|-- keycloak/
|   `-- pulse-realm.json             // Keycloak realm config, auto-imported on startup
|-- kubernetes/
|   |-- base/                        // Shared Kubernetes manifests
|   |   |-- submitter/
|   |   |-- processor/
|   |   |-- projector/
|   |   `-- otel-collector/
|   |-- overlays/
|   |   |-- local/                   // Local overlay: in-cluster Postgres, Redis, Pub/Sub emulator
|   |   `-- pro/                     // Production overlay: GCP Cloud SQL, Memorystore, Pub/Sub
|-- pulse-app/                       // Maven multi-module project
|   |-- common/
|   |   `-- model/                   // Shared event models and topic definitions
|   `-- services/
|       |-- submitter/               // REST API for transaction submission (port 8080)
|       |-- processor/               // Fraud risk scoring engine (event-driven, no public API)
|       |-- projector/               // Query API and PostgreSQL persistence (port 8083)
|-- .pre-commit-config.yaml
|-- docker-compose.yml               // Full local stack
`-- README.md                        // This file
```

## Tooling

### pre-commit

This repository uses pre-commit to run [KTLint](https://github.com/ktlint/ktlint) formatting checks before committing changes. To install the hooks, run the following command in the root of the repository:

```bash
pre-commit install
```

To run the checks manually on all files:

```bash
pre-commit run --all-files
```

## Running locally

### Docker Compose

Docker Compose is the recommended way to run the full stack locally. It starts all three services along with Kafka, PostgreSQL, Redis, Keycloak, and Jaeger in a single command.

```bash
docker-compose up --build
```

The first build compiles all services from source. Subsequent runs use the Docker layer cache. To run in the background, add `-d`. Keycloak takes around 90 seconds to become healthy on first start.

Once everything is running:

| Service | URL |
| ------- | --- |
| Submitter API | http://localhost:8080 |
| Submitter Swagger UI | http://localhost:8080/swagger-ui.html |
| Projector API | http://localhost:8083 |
| Projector Swagger UI | http://localhost:8083/swagger-ui.html |
| Kafka UI | http://localhost:8081 |
| Keycloak admin console | http://localhost:8180 (admin / admin) |
| Jaeger UI | http://localhost:16686 |

To stop and remove containers:

```bash
docker-compose down
```

To also remove volumes (wipes database data):

```bash
docker-compose down -v
```

### Kubernetes

The `local` overlay runs on a local Kubernetes cluster and replaces all GCP-managed services with in-cluster equivalents (Pub/Sub emulator, PostgreSQL, Redis). Note that this overlay uses **Pub/Sub** for messaging rather than Kafka.

**1. Build the service images**

The local overlay sets `imagePullPolicy: Never`, so images must be present in your cluster's image store before deploying.

For **Docker Desktop** (locally-built images are available automatically):

```bash
docker build -f pulse-app/services/submitter/Dockerfile -t submitter:latest pulse-app
docker build -f pulse-app/services/processor/Dockerfile -t processor:latest pulse-app
docker build -f pulse-app/services/projector/Dockerfile -t projector:latest pulse-app
```

For **kind** (images must be loaded explicitly after building):

```bash
kind load docker-image submitter:latest
kind load docker-image processor:latest
kind load docker-image projector:latest
```

For **minikube** (build inside minikube's Docker daemon):

```bash
eval $(minikube docker-env)
docker build -f pulse-app/services/submitter/Dockerfile -t submitter:latest pulse-app
docker build -f pulse-app/services/processor/Dockerfile -t processor:latest pulse-app
docker build -f pulse-app/services/projector/Dockerfile -t projector:latest pulse-app
```

**2. Configure OAuth2**

The local overlay requires an OAuth2 issuer. Fill in your provider (e.g. a standalone Keycloak instance) in `kubernetes/overlays/local/config.env` before deploying:

```env
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://<keycloak-host>/realms/pulse
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://<keycloak-host>/realms/pulse/protocol/openid-connect/certs
```

**3. Apply the manifests**

```bash
kubectl apply -k kubernetes/overlays/local
```

**4. Verify the deployment**

```bash
kubectl get pods -n pulse
```

Services use startup probes with a 5-minute window. Once pods are `Running`, use `kubectl port-forward` to reach the APIs:

```bash
kubectl port-forward -n pulse svc/submitter 8080:8080
kubectl port-forward -n pulse svc/projector 8083:8083
```

To tear down:

```bash
kubectl delete -k kubernetes/overlays/local
```

## Building and testing

To build all modules:

```bash
mvn -f pulse-app/pom.xml clean package
```

To run all tests (requires Docker for integration tests via Testcontainers):

```bash
mvn -f pulse-app/pom.xml verify
```

To run linting only (KTLint and Detekt, skipping tests):

```bash
mvn -f pulse-app/pom.xml verify -Dmaven.test.skip=true
```

To build a single service:

```bash
mvn -f pulse-app/services/submitter/pom.xml -am clean package
```

## Deployment strategy

On pull requests to `main`, the CI pipeline runs linting and all tests. On merge to `main`, the CD pipeline builds Docker images for all three services and pushes them to GCP Artifact Registry, tagged with both the commit SHA and `latest`.

The production Kubernetes overlay in `kubernetes/overlays/pro/` is deployed to GKE and uses GCP-managed services (Cloud SQL, Pub/Sub, Memorystore, Cloud Trace, Identity Platform) in place of the local equivalents. Infrastructure for the production environment is managed separately in [`pulse-infra/`](https://github.com/mbrickerd/pulse-infra).
