package pl.lodz.p.ias.io.zasoby.dto;

import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.ias.io.zasoby.utils.ResourceStatus;



@Getter
@Setter
public class ResourceDTO {
    private long resourceId;
    private String resourceName;
    private String resourceType;
    private int resourceQuantity;
    private ResourceStatus resourceStatus;
    private long warehouseId;

    public ResourceDTO(long resourceId, String resourceName, String resourceType,
                       int resourceQuantity, ResourceStatus resourceStatus,
                       long warehouseId) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceQuantity = resourceQuantity;
        this.resourceStatus = resourceStatus;
        this.warehouseId = warehouseId;
    }
}