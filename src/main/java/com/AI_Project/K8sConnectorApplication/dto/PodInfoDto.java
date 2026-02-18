package com.AI_Project.K8sConnectorApplication.dto;

import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PodInfoDto {
    private String name;
    private String namespace;
    private String podIp;
    private String phase;
    private OffsetDateTime startTime;
    private List<String> containerStatuses;


    public static PodInfoDto fromV1Pod(V1Pod pod) {
        PodInfoDto d = new PodInfoDto();
        d.name = pod.getMetadata().getName();
        d.namespace = pod.getMetadata().getNamespace();
        if (pod.getStatus() != null) {
            d.podIp = pod.getStatus().getPodIP();
            d.phase = pod.getStatus().getPhase();
            d.startTime = pod.getStatus().getStartTime();
            if (pod.getStatus().getContainerStatuses() != null) {
                d.containerStatuses = pod.getStatus().getContainerStatuses().stream()
                        .map(V1ContainerStatus::getName)
                        .collect(Collectors.toList());
            }
        }
        return d;
    }

}
