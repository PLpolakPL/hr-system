package pl.atins.dto;

import lombok.Data;

@Data
public class DepartmentResponse {

    private Long id;
    private String name;
    private String description;
    private String location;
    private Long headId;
    private String headName;
}