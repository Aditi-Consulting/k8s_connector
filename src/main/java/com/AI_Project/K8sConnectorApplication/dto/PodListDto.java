package com.AI_Project.K8sConnectorApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodListDto {

    private String namespace;
    private int totalPods;
    private List<PodInfoDto> pods;

}
