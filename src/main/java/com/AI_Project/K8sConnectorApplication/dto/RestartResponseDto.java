package com.AI_Project.K8sConnectorApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestartResponseDto {
    private String podName;
    private String namespace;
    private String status;
    private String message;

}