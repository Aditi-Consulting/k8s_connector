package com.AI_Project.K8sConnectorApplication.controller;

import com.AI_Project.K8sConnectorApplication.dto.DeploymentInfoDto;
import com.AI_Project.K8sConnectorApplication.service.KubernetesDeploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/k8s/deployments")
@Tag(name = "Kubernetes Deployments", description = "APIs for interacting with Kubernetes Deployments")
public class DeploymentController {

    private final KubernetesDeploymentService deploymentService;

    public DeploymentController(KubernetesDeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping("/{namespace}")
    @Operation(summary = "List Deployments in a namespace",
            description = "Fetch all deployments running in the specified namespace with details")
    public ResponseEntity<List<Map<String, Object>>> listDeployments(@PathVariable String namespace) throws Exception {
        return ResponseEntity.ok(deploymentService.listDeployments(namespace));
    }

    @Operation(summary = "Get Deployment details")
    @GetMapping("/{namespace}/{deploymentName}")
    public ResponseEntity<DeploymentInfoDto> getDeployment(
            @PathVariable String namespace,
            @PathVariable String deploymentName) throws Exception {
        return ResponseEntity.ok(deploymentService.getDeploymentDetails(namespace, deploymentName));
    }

    @Operation(summary = "Restart a Deployment")
    @PostMapping("/{namespace}/{deploymentName}/restart")
    public ResponseEntity<String> restartDeployment(
            @PathVariable String namespace,
            @PathVariable String deploymentName) throws Exception {
        return ResponseEntity.ok(deploymentService.restartDeployment(namespace, deploymentName));
    }

    @Operation(summary = "Scale a Deployment")
    @PostMapping("/scale")
    public ResponseEntity<String> scaleDeployment(@RequestBody DeploymentInfoDto request
           ) throws Exception {
        return ResponseEntity.ok(deploymentService.scaleDeployment(request.getNamespace(), request.getName(), request.getReplicas()));
    }
}

