# Kubernetes Connector Application

A Spring Boot application that provides REST APIs to interact with Kubernetes clusters.

## Prerequisites

- Java 21
- Docker Desktop with Kubernetes enabled
- Maven 3.9+
- Access to a Kubernetes cluster

## Running Locally (Without Docker)

When running on your local machine, the application uses your default kubeconfig:

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Running in Docker

When running in Docker, you need to provide authentication credentials via your kubeconfig file.

### Step 1: Build the Docker Image

```bash
docker build -t k8s-connector .
```

### Step 2: Run the Container

**For Windows (Docker Desktop):**

```cmd
docker run -p 8080:8080 ^
  -v %USERPROFILE%\.kube\config:/app/kubeconfig ^
  -e KUBERNETES_KUBECONFIG=/app/kubeconfig ^
  -e KUBERNETES_API_URL=https://kubernetes.docker.internal:6443 ^
  --add-host=kubernetes.docker.internal:host-gateway ^
  k8s-connector
```

**For Linux/Mac:**

```bash
docker run -p 8080:8080 \
  -v ~/.kube/config:/app/kubeconfig \
  -e KUBERNETES_KUBECONFIG=/app/kubeconfig \
  -e KUBERNETES_API_URL=https://kubernetes.docker.internal:6443 \
  --add-host=kubernetes.docker.internal:host-gateway \
  k8s-connector
```

### Explanation of Docker Run Parameters

- `-p 8080:8080` - Maps port 8080 from container to host
- `-v ~/.kube/config:/app/kubeconfig` - Mounts your kubeconfig file into the container
- `-e KUBERNETES_KUBECONFIG=/app/kubeconfig` - Sets the kubeconfig path environment variable
- `-e KUBERNETES_API_URL=https://kubernetes.docker.internal:6443` - Sets the Kubernetes API server URL
- `--add-host=kubernetes.docker.internal:host-gateway` - Allows container to resolve Docker Desktop's Kubernetes API server

## API Endpoints

### Services

- `GET /api/k8s/services/{namespace}` - List all services in a namespace
- `GET /api/k8s/services/{namespace}/{serviceName}` - Get service details
- `POST /api/k8s/services/{namespace}/{serviceName}/fix-port?oldPort={old}&newPort={new}` - Update service port

### Pods

- `GET /api/k8s/pods/{namespace}` - List all pods in a namespace
- `GET /api/k8s/pods/{namespace}/{podName}` - Get pod details
- `POST /api/k8s/pods/{namespace}/{podName}/restart` - Restart a pod

### Deployments

- `GET /api/k8s/deployments/{namespace}` - List all deployments in a namespace
- `GET /api/k8s/deployments/{namespace}/{deploymentName}` - Get deployment details

## Swagger Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Troubleshooting

### Error: "system:anonymous" cannot list resources

This means the application is not authenticated. Make sure:

1. You mounted your kubeconfig file correctly
2. You set the `KUBERNETES_KUBECONFIG` environment variable
3. Your kubeconfig file has valid credentials

### Error: Connection refused

This means the application cannot reach the Kubernetes API server. Make sure:

1. Docker Desktop's Kubernetes is running
2. You used `--add-host=kubernetes.docker.internal:host-gateway`
3. You set `KUBERNETES_API_URL=https://kubernetes.docker.internal:6443`

### Error: SSL certificate errors

The application disables SSL verification for development. If you still see SSL errors, check your kubeconfig's certificate-authority-data.

## Configuration

You can customize the application using environment variables:

- `KUBERNETES_KUBECONFIG` - Path to kubeconfig file (default: empty, uses default config)
- `KUBERNETES_API_URL` - Kubernetes API server URL (default: empty, uses kubeconfig value)

## Development

### Building the Project

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/AI_Project/K8sConnectorApplication/
│   │       ├── config/          # Kubernetes client configuration
│   │       ├── controller/      # REST API controllers
│   │       ├── dto/             # Data transfer objects
│   │       ├── exception/       # Exception handling
│   │       ├── service/         # Business logic
│   │       └── util/            # Utility classes
│   └── resources/
│       ├── application.properties
│       └── application.yml
```

## License

[Your License Here]

