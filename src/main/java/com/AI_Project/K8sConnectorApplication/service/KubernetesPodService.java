package com.AI_Project.K8sConnectorApplication.service;

import com.AI_Project.K8sConnectorApplication.dto.PodInfoDto;
import com.AI_Project.K8sConnectorApplication.exception.K8sConnectorException;
import com.AI_Project.K8sConnectorApplication.util.NetworkUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KubernetesPodService {

    private final CoreV1Api coreV1Api;

    public KubernetesPodService(ApiClient apiClient) {
        this.coreV1Api = new CoreV1Api(apiClient);
    }

    // List pods; 404 if none
    public List<PodInfoDto> listPods(String namespace) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        // ensure namespace exists
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        try {
            V1PodList podList = coreV1Api.listNamespacedPod(ns, null,null,null,null,null,null,null,null,null,null,null);
            if (podList == null || podList.getItems() == null || podList.getItems().isEmpty()) throw K8sConnectorException.notFound("No pods found in namespace: " + ns);
            List<PodInfoDto> out = new ArrayList<>();
            for (V1Pod p : podList.getItems()) out.add(PodInfoDto.fromV1Pod(p));
            return out;
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Resource not found in namespace: " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while listing pods in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while listing pods in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error listing pods in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error listing pods in namespace " + ns, e);
        }
    }

    // Get single pod details
    public PodInfoDto getPodDetails(String namespace, String podName) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        try {
            V1Pod pod = coreV1Api.readNamespacedPod(podName, ns, null);
            if (pod == null) throw K8sConnectorException.notFound("Pod not found: " + podName + " in namespace " + ns);
            return PodInfoDto.fromV1Pod(pod);
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Pod not found: " + podName + " in namespace " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while reading pod in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while reading pod in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error reading pod in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error fetching pod " + podName + " in namespace " + ns, e);
        }
    }

    // Pod logs
    public String getPodLogs(String namespace, String podName, Integer tailLines) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        if (tailLines != null && tailLines < 0) throw K8sConnectorException.badRequest("tailLines must be >= 0");
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        try {
            return coreV1Api.readNamespacedPodLog(podName, ns, null,null,null,null,null,null,null, tailLines, null);
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Pod not found: " + podName + " in namespace " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while reading pod logs in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while reading pod logs in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error reading pod logs in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error fetching logs for pod " + podName + " in namespace " + ns, e);
        }
    }

    // Restart pod (delete) -> message
    public String restartPod(String namespace, String podName) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        try {
            // ensure exists
            coreV1Api.readNamespacedPod(podName, ns, null);
            coreV1Api.deleteNamespacedPod(podName, ns, null, null, null, null, null, null);
            return "Pod " + podName + " scheduled for restart (deleted) in namespace " + ns;
        } catch (ApiException e) {
            int code = e.getCode();
            String body = (e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage();
            if (code == 404) throw K8sConnectorException.notFound("Pod not found: " + podName + " in namespace " + ns);
            if (code == 400) throw K8sConnectorException.badRequest("Bad request while restarting pod in namespace " + ns + ": " + body);
            if (code == 409) throw K8sConnectorException.conflict("Conflict while restarting pod in namespace " + ns + ": " + body);
            throw K8sConnectorException.serverError("Error restarting pod in namespace " + ns + ": HTTP " + code + " - " + body, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error restarting pod " + podName + " in namespace " + ns, e);
        }
    }

    // Check TCP port on pod IP
    public boolean checkPort(String namespace, String podName, int port, int timeoutMs) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        if (port <=0 || port>65535) throw K8sConnectorException.badRequest("Port must be between 1 and 65535");
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        try {
            PodInfoDto pod = getPodDetails(ns, podName); // will throw if not found
            if (pod.getPodIp() == null || pod.getPodIp().isBlank()) return false;
            return NetworkUtil.isTcpPortOpen(pod.getPodIp(), port, timeoutMs);
        } catch (K8sConnectorException ex) {
            throw ex;
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Error checking port " + port + " for pod " + podName + " in namespace " + ns, e);
        }
    }

    // Remediation flow
    public String remediatePodIssue(String namespace, String podName, int port, int timeoutMs) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        if (port <=0 || port>65535) throw K8sConnectorException.badRequest("Port must be between 1 and 65535");
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + ((e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage()), e); }
        StringBuilder report = new StringBuilder();
        try {
            PodInfoDto pod = getPodDetails(ns, podName);
            report.append("Pod phase: ").append(pod.getPhase()).append('\n');
            if (!"Running".equalsIgnoreCase(pod.getPhase())) {
                report.append("Pod not running. Restarting...\n");
                restartPod(ns, podName);
                Thread.sleep(4000);
                pod = getPodDetails(ns, podName);
                report.append("Post-restart phase: ").append(pod.getPhase()).append('\n');
            }
            boolean portOk = checkPort(ns, podName, port, timeoutMs);
            report.append("Port ").append(port).append(" reachable: ").append(portOk).append('\n');
            if (!portOk) {
                report.append("Port unreachable. Restarting pod and rechecking...\n");
                restartPod(ns, podName);
                Thread.sleep(4000);
                portOk = checkPort(ns, podName, port, timeoutMs);
                report.append("Port ").append(port).append(" reachable after restart: ").append(portOk).append('\n');
            }
            report.append("Remediation complete.\n");
            return report.toString();
        } catch (K8sConnectorException ex) {
            throw ex;
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Error during remediation for pod " + podName + " in namespace " + ns, e);
        }
    }
}
