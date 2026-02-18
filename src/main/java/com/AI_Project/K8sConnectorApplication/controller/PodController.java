package com.AI_Project.K8sConnectorApplication.controller;

import com.AI_Project.K8sConnectorApplication.dto.PodInfoDto;
import com.AI_Project.K8sConnectorApplication.service.KubernetesPodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RestController
@RequestMapping("/api/k8s")
public class PodController {

    private final KubernetesPodService k8s;

    public PodController(KubernetesPodService k8s) {
        this.k8s = k8s;
    }

    @Operation(summary = "List pods", description = "Fetch pods from a namespace. Defaults to all namespaces if not provided.")
    @GetMapping("/pods")
    public ResponseEntity<List<PodInfoDto>> listPods(
            @Parameter(description = "Namespace (optional)")
            @RequestParam(required = false) String namespace) throws Exception {
        return ResponseEntity.ok(k8s.listPods(namespace));
    }

    @Operation(summary = "Get pod details")
    @GetMapping("/pods/{namespace}/{podName}")
    public ResponseEntity<PodInfoDto> getPod(
            @PathVariable String namespace,
            @PathVariable String podName) throws Exception {
        return ResponseEntity.ok(k8s.getPodDetails(namespace, podName));
    }

    @Operation(summary = "Get pod logs")
    @GetMapping("/pods/{namespace}/{podName}/logs")
    public ResponseEntity<String> getLogs(
            @PathVariable String namespace,
            @PathVariable String podName,
            @Parameter(description = "Number of lines from end of log")
            @RequestParam(required = false) Integer tailLines) throws Exception {
        return ResponseEntity.ok(k8s.getPodLogs(namespace, podName, tailLines));
    }

    @Operation(summary = "Restart a pod")
    @PostMapping("/pods/{namespace}/{podName}/restart")
    public ResponseEntity<?> restart(
            @PathVariable String namespace,
            @PathVariable String podName) throws Exception {
        String res = k8s.restartPod(namespace, podName);
        return ResponseEntity.ok().body(res);
    }

    @Operation(summary = "Check if port is open in a pod")
    @GetMapping("/pods/{namespace}/{podName}/port-check")
    public ResponseEntity<Boolean> portCheck(
            @PathVariable String namespace,
            @PathVariable String podName,
            @RequestParam int port,
            @RequestParam(defaultValue = "2000") int timeout) throws Exception {
        boolean ok = k8s.checkPort(namespace, podName, port, timeout);
        return ResponseEntity.ok(ok);
    }

    @Operation(
            summary = "Remediate a pod issue",
            description = "Checks the pod's specified port and performs remediation if needed"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Remediation report returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error during remediation")
    })
    @PostMapping("/pods/{namespace}/{podName}/remediate")
    public ResponseEntity<String> remediate(
            @Parameter(description = "Namespace of the pod", example = "default") @PathVariable String namespace,
            @Parameter(description = "Name of the pod", example = "nginx") @PathVariable String podName,
            @Parameter(description = "Port to check on the pod", example = "80") @RequestParam int port,
            @Parameter(description = "Timeout in milliseconds for port check", example = "2000") @RequestParam(defaultValue = "2000") int timeout) {

        String report = k8s.remediatePodIssue(namespace, podName, port, timeout);
        return ResponseEntity.ok(report);
    }

}
