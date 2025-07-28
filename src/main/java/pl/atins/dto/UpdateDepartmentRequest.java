package pl.atins.dto;

import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    private String name;

    private String description;

    private String location;

    private Long headId;
}