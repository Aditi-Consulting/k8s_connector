package com.AI_Project.K8sConnectorApplication.dto;

import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ServiceInfoDto {

    private String name;
    private String namespace;
    private List<Integer> ports;

    public static ServiceInfoDto fromV1Service(V1Service svc) {
        ServiceInfoDto dto = new ServiceInfoDto();
        dto.setName(svc.getMetadata().getName());
        dto.setNamespace(svc.getMetadata().getNamespace());
        dto.setPorts(
                svc.getSpec().getPorts()
                        .stream()
                        .map(V1ServicePort::getPort)
                        .collect(Collectors.toList())
        );
        return dto;
    }

}
