package com.AI_Project.K8sConnectorApplication.service;

import com.AI_Project.K8sConnectorApplication.dto.ServiceInfoDto;
import com.AI_Project.K8sConnectorApplication.exception.K8sConnectorException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KubernetesServiceService {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesServiceService.class);
    private final CoreV1Api coreV1Api;

    public KubernetesServiceService(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    /**
     * List services in the given namespace. If none found → 404 instead of empty 200.
     */
    public List<ServiceInfoDto> listServices(String namespace) {
        // Inline normalize namespace
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        // Inline ensure namespace exists
        try {
            coreV1Api.readNamespace(ns, null);
        } catch (ApiException e) {
            if (e.getCode() == 404) throw K8sConnectorException.notFound("Namespace not found: " + ns);
            throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage()), e);
        }

        try {
            logger.info("Listing services in namespace='{}'", ns);
            V1ServiceList serviceList = coreV1Api.listNamespacedService(
                    ns,
                    null, // pretty
                    null, // allowWatchBookmarks
                    null, // _continue
                    null, // fieldSelector
                    null, // labelSelector
                    null, // limit
                    null, // resourceVersion
                    null, // resourceVersionMatch
                    null, // timeoutSeconds
                    null, // watch
                    null  // options
            );

            if (serviceList == null || serviceList.getItems() == null || serviceList.getItems().isEmpty()) {
                logger.warn("No services found in namespace='{}'", ns);
                throw K8sConnectorException.notFound("No services found in namespace: " + ns);
            }

            List<ServiceInfoDto> result = serviceList.getItems().stream()
                    .map(ServiceInfoDto::fromV1Service)
                    .collect(Collectors.toList());

            logger.info("Returning {} services for namespace='{}'", result.size(), ns);
            return result;
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Resource not found in namespace: " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while listing services in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while listing services in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error listing services in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error listing services in namespace " + ns, e);
        }
    }

    /**
     * Get details for a single service.
     */
    public ServiceInfoDto getServiceDetails(String namespace, String serviceName) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try {
            coreV1Api.readNamespace(ns, null);
        } catch (ApiException e) {
            if (e.getCode() == 404) throw K8sConnectorException.notFound("Namespace not found: " + ns);
            throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage()), e);
        }

        try {
            logger.info("Fetching service='{}' in namespace='{}'", serviceName, ns);
            V1Service svc = coreV1Api.readNamespacedService(serviceName, ns, null);
            if (svc == null) throw K8sConnectorException.notFound("Service not found: " + serviceName + " in namespace " + ns);
            return ServiceInfoDto.fromV1Service(svc);
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Service not found: " + serviceName + " in namespace " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while reading service in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while reading service in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error reading service in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error fetching service " + serviceName + " in namespace " + ns, e);
        }
    }

    /**
     * Update ("fix") an existing service port from oldPort to newPort.
     */
    public String fixServicePort(String namespace, String serviceName, int oldPort, int newPort) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try {
            coreV1Api.readNamespace(ns, null);
        } catch (ApiException e) {
            if (e.getCode() == 404) throw K8sConnectorException.notFound("Namespace not found: " + ns);
            throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage()), e);
        }
        // Inline port validation
        if (oldPort <= 0 || oldPort > 65535 || newPort <= 0 || newPort > 65535) throw K8sConnectorException.badRequest("Ports must be between 1 and 65535");
        if (oldPort == newPort) throw K8sConnectorException.badRequest("Old and new port must differ");

        try {
            logger.info("Fixing port for service='{}' namespace='{}' oldPort={} newPort={}", serviceName, ns, oldPort, newPort);
            V1Service svc = coreV1Api.readNamespacedService(serviceName, ns, null);
            if (svc == null || svc.getSpec() == null || svc.getSpec().getPorts() == null) throw K8sConnectorException.notFound("Service or ports not found: " + serviceName + " in namespace " + ns);

            boolean oldPortFound = false;
            boolean newPortAlreadyExists = false;
            for (V1ServicePort p : svc.getSpec().getPorts()) {
                if (p.getPort() == oldPort) oldPortFound = true;
                if (p.getPort() == newPort) newPortAlreadyExists = true;
            }
            if (!oldPortFound) throw K8sConnectorException.notFound("Old port " + oldPort + " not found in service " + serviceName);
            if (newPortAlreadyExists) throw K8sConnectorException.conflict("New port " + newPort + " already exists in service " + serviceName);

            for (V1ServicePort p : svc.getSpec().getPorts()) if (p.getPort() == oldPort) p.setPort(newPort);
            coreV1Api.replaceNamespacedService(serviceName, ns, svc, null, null, null, null);
            String msg = "Service " + serviceName + " port updated from " + oldPort + " to " + newPort + " in namespace " + ns;
            logger.info(msg);
            return msg;
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody() != null && !e.getResponseBody().isBlank()) ? e.getResponseBody() : e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Service not found: " + serviceName + " in namespace " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while updating service port in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while updating service port in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error updating service port in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error updating service port for " + serviceName + " in namespace " + ns, e);
        }
    }
}

