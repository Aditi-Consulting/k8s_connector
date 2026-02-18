package com.AI_Project.K8sConnectorApplication.service;

import com.AI_Project.K8sConnectorApplication.dto.DeploymentInfoDto;
import com.AI_Project.K8sConnectorApplication.exception.K8sConnectorException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KubernetesDeploymentService {

    private final AppsV1Api appsV1Api;
    private final CoreV1Api coreV1Api;

    public KubernetesDeploymentService(AppsV1Api appsV1Api, CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
        this.appsV1Api = appsV1Api;
    }

    public List<Map<String, Object>> listDeployments(String namespace) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        // Validate namespace
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + body(e), e); }
        try {
            V1DeploymentList dList = appsV1Api.listNamespacedDeployment(ns, null,null,null,null,null,null,null,null,null,null,null);
            if (dList == null || dList.getItems() == null || dList.getItems().isEmpty()) {
                throw K8sConnectorException.notFound("No deployments found in namespace: " + ns);
            }
            List<Map<String,Object>> out = new ArrayList<>();
            for (V1Deployment dep : dList.getItems()) {
                Map<String,Object> m = new HashMap<>();
                m.put("name", dep.getMetadata()!=null? dep.getMetadata().getName(): null);
                m.put("replicas", dep.getSpec()!=null? dep.getSpec().getReplicas(): null);
                m.put("availableReplicas", dep.getStatus()!=null? dep.getStatus().getAvailableReplicas(): null);
                m.put("labels", dep.getMetadata()!=null? dep.getMetadata().getLabels(): null);
                out.add(m);
            }
            return out;
        } catch (ApiException e) {
            int code = e.getCode(); String b = body(e);
            if (code==404) throw K8sConnectorException.notFound("Resource not found in namespace: " + ns);
            if (code==400) throw K8sConnectorException.badRequest("Bad request while listing deployments in namespace " + ns + ": " + b);
            if (code==409) throw K8sConnectorException.conflict("Conflict while listing deployments in namespace " + ns + ": " + b);
            throw K8sConnectorException.serverError("Error listing deployments in namespace " + ns + ": HTTP " + code + " - " + b, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error listing deployments in namespace " + ns, e);
        }
    }

    public DeploymentInfoDto getDeploymentDetails(String namespace, String deploymentName) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + body(e), e); }
        try {
            V1Deployment dep = appsV1Api.readNamespacedDeployment(deploymentName, ns, null);
            if (dep == null) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            return DeploymentInfoDto.fromV1Deployment(dep);
        } catch (ApiException e) {
            int code = e.getCode(); String b = body(e);
            if (code==404) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            if (code==400) throw K8sConnectorException.badRequest("Bad request while reading deployment in namespace " + ns + ": " + b);
            if (code==409) throw K8sConnectorException.conflict("Conflict while reading deployment in namespace " + ns + ": " + b);
            throw K8sConnectorException.serverError("Error reading deployment in namespace " + ns + ": HTTP " + code + " - " + b, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error fetching deployment " + deploymentName + " in namespace " + ns, e);
        }
    }

    public String restartDeployment(String namespace, String deploymentName) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + body(e), e); }
        try {
            V1Deployment dep = appsV1Api.readNamespacedDeployment(deploymentName, ns, null);
            if (dep == null) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            String matchLabel = dep.getSpec()!=null && dep.getSpec().getSelector()!=null? String.valueOf(dep.getSpec().getSelector().getMatchLabels()): "{}";
            return "Deployment " + deploymentName + " restart requested (pods with labels " + matchLabel + " will be recreated)";
        } catch (ApiException e) {
            int code = e.getCode(); String b = body(e);
            if (code==404) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            if (code==400) throw K8sConnectorException.badRequest("Bad request while restarting deployment in namespace " + ns + ": " + b);
            if (code==409) throw K8sConnectorException.conflict("Conflict while restarting deployment in namespace " + ns + ": " + b);
            throw K8sConnectorException.serverError("Error restarting deployment in namespace " + ns + ": HTTP " + code + " - " + b, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error restarting deployment " + deploymentName + " in namespace " + ns, e);
        }
    }

    public String scaleDeployment(String namespace, String deploymentName, int replicas) {
        String ns = (namespace == null || namespace.isBlank()) ? "default" : namespace.trim();
        if (replicas < 0) throw K8sConnectorException.badRequest("Replicas must be >= 0");
        try { coreV1Api.readNamespace(ns, null); } catch (ApiException e) { if (e.getCode()==404) throw K8sConnectorException.notFound("Namespace not found: " + ns); throw K8sConnectorException.serverError("Error validating namespace " + ns + ": " + body(e), e); }
        try {
            V1Deployment dep = appsV1Api.readNamespacedDeployment(deploymentName, ns, null);
            if (dep == null) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            if (dep.getSpec()==null) throw K8sConnectorException.serverError("Deployment spec missing for " + deploymentName + " in namespace " + ns, null);
            dep.getSpec().setReplicas(replicas);
            appsV1Api.replaceNamespacedDeployment(deploymentName, ns, dep, null, null, null, null);
            return "Deployment " + deploymentName + " scaled to " + replicas + " replicas";
        } catch (ApiException e) {
            int code = e.getCode(); String b = body(e);
            if (code==404) throw K8sConnectorException.notFound("Deployment not found: " + deploymentName + " in namespace " + ns);
            if (code==400) throw K8sConnectorException.badRequest("Bad request while scaling deployment in namespace " + ns + ": " + b);
            if (code==409) throw K8sConnectorException.conflict("Conflict while scaling deployment in namespace " + ns + ": " + b);
            throw K8sConnectorException.serverError("Error scaling deployment in namespace " + ns + ": HTTP " + code + " - " + b, e);
        } catch (Exception e) {
            throw K8sConnectorException.serverError("Unexpected error scaling deployment " + deploymentName + " in namespace " + ns, e);
        }
    }

    private String body(ApiException e) { return (e.getResponseBody()!=null && !e.getResponseBody().isBlank())? e.getResponseBody(): e.getMessage(); }
}
