package com.AI_Project.K8sConnectorApplication.dto;

import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.Data;

@Data
public class DeploymentInfoDto {

    private String namespace;
    private String name;
    private Integer replicas;

    public static DeploymentInfoDto fromV1Deployment(V1Deployment dep) {
        DeploymentInfoDto dto = new DeploymentInfoDto();
        dto.setName(dep.getMetadata().getName());
        dto.setNamespace(dep.getMetadata().getNamespace());
        dto.setReplicas(dep.getSpec().getReplicas());
        return dto;
    }

}
