package com.AI_Project.K8sConnectorApplication.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

@Configuration
public class KubernetesClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesClientConfig.class);

    /**
     * Path to kubeconfig file (optional).
     * - Can be set via application.properties or environment variable
     *   Example: kubernetes.kubeconfig=/home/user/.kube/config
     * - If not set, the client will try in-cluster config (useful when running inside Kubernetes).
     */
    @Value("${kubernetes.kubeconfig:}")
    private String kubeconfig;

    @Value("${kubernetes.api.url:}")
    private String apiServerUrl;

    /**
     * Bean that creates and configures the Kubernetes ApiClient.
     * - Uses kubeconfig if provided, otherwise falls back to in-cluster config.
     * - Customizes OkHttpClient with reasonable timeouts.
     * - Registers this client as the default so Kubernetes Java client can use it globally.
     */
    @Bean
    public ApiClient apiClient() throws Exception {
        logger.info("========================================");
        logger.info("Initializing Kubernetes API Client");
        logger.info("Kubeconfig path: '{}'", kubeconfig);
        logger.info("API Server URL: '{}'", apiServerUrl);
        logger.info("========================================");

        ApiClient client;

        // Try to load kubeconfig if specified
        if (kubeconfig != null && !kubeconfig.isBlank()) {
            logger.info("Attempting to load kubeconfig from: {}", kubeconfig);
            try {
                client = Config.fromConfig(new FileReader(kubeconfig));
                logger.info("✓ Successfully loaded kubeconfig from: {}", kubeconfig);
                logger.info("Base path from kubeconfig: {}", client.getBasePath());

                // Log authentication info (without sensitive data)
                if (client.getAuthentication("BearerToken") != null) {
                    logger.info("✓ BearerToken authentication configured");
                }
            } catch (IOException e) {
                logger.error("✗ Failed to load kubeconfig from {}: {}", kubeconfig, e.getMessage());
                logger.error("Full error: ", e);
                throw new RuntimeException("Failed to load kubeconfig", e);
            }
        } else {
            logger.warn("No kubeconfig path provided, attempting default configuration");
            try {
                client = Config.defaultClient();
                logger.info("✓ Using default Kubernetes client configuration");
                logger.info("Base path from default config: {}", client.getBasePath());
            } catch (IOException e) {
                logger.error("✗ Failed to load default config: {}", e.getMessage());
                logger.warn("Creating unauthenticated ApiClient - this will likely fail");
                client = new ApiClient();
            }
        }

        // Override API server URL if provided
        if (apiServerUrl != null && !apiServerUrl.isEmpty()) {
            logger.info("Overriding Kubernetes API URL to: {}", apiServerUrl);
            client.setBasePath(apiServerUrl);
        }

        // Disable SSL verification for development (Docker Desktop uses self-signed certs)
        client.setVerifyingSsl(false);
        logger.info("SSL verification disabled for development");

        // Configure HTTP client with custom timeouts
        OkHttpClient httpClient = client.getHttpClient().newBuilder()
                .readTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        client.setHttpClient(httpClient);

        logger.info("========================================");
        logger.info("✓ Kubernetes API client configured");
        logger.info("Final Base Path: {}", client.getBasePath());
        logger.info("========================================");

        // Register as global default client
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

        return client;
    }

    /**
     * Bean for CoreV1Api.
     * - Provides access to core Kubernetes resources:
     *   Pods, Services, ConfigMaps, Endpoints, Namespaces, etc.
     */
    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    /**
     * Bean for AppsV1Api.
     * - Provides access to workloads managed by the "apps" API group:
     *   Deployments, ReplicaSets, StatefulSets, DaemonSets, etc.
     */
    @Bean
    public AppsV1Api appsV1Api(ApiClient apiClient) {
        return new AppsV1Api(apiClient);
    }
}
