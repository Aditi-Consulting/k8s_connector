package com.AI_Project.K8sConnectorApplication.controller;

import com.AI_Project.K8sConnectorApplication.dto.ServiceInfoDto;
import com.AI_Project.K8sConnectorApplication.service.KubernetesServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/k8s/services")
@Tag(name = "Kubernetes Services", description = "APIs for interacting with Kubernetes Services")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    private final KubernetesServiceService serviceService;

    public ServiceController(KubernetesServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/{namespace}")
    @Operation(summary = "List Services in a namespace",
            description = "Fetch all services running in the specified namespace with details")
    public ResponseEntity<List<ServiceInfoDto>> listServices(@PathVariable String namespace) {
        logger.info("Received request to list services in namespace: {}", namespace);
        List<ServiceInfoDto> services = serviceService.listServices(namespace);
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "Get Service details")
    @GetMapping("/{namespace}/{serviceName}")
    public ResponseEntity<ServiceInfoDto> getService(
            @PathVariable String namespace,
            @PathVariable String serviceName) {
        logger.info("Received request to get service '{}' in namespace '{}'", serviceName, namespace);
        ServiceInfoDto service = serviceService.getServiceDetails(namespace, serviceName);
        return ResponseEntity.ok(service);
    }

    @Operation(summary = "Fix Service Port")
    @PostMapping("/{namespace}/{serviceName}/fix-port")
    public ResponseEntity<String> fixServicePort(
            @PathVariable String namespace,
            @PathVariable String serviceName,
            @RequestParam int oldPort,
            @RequestParam int newPort) {
        logger.info("Received request to fix port for service '{}' in namespace '{}': {} -> {}", serviceName, namespace, oldPort, newPort);
        String result = serviceService.fixServicePort(namespace, serviceName, oldPort, newPort);
        return ResponseEntity.ok(result);
    }
}