/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.anl.aps.cdb.portal.model.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author sveseli
 */
@Embeddable
public class ComponentPropertyPK implements Serializable
{
    @Basic(optional = false)
    @NotNull
    @Column(name = "component_id")
    private int componentId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "property_type_id")
    private int propertyTypeId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "property_value_id")
    private int propertyValueId;

    public ComponentPropertyPK() {
    }

    public ComponentPropertyPK(int componentId, int propertyTypeId, int propertyValueId) {
        this.componentId = componentId;
        this.propertyTypeId = propertyTypeId;
        this.propertyValueId = propertyValueId;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public int getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(int propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }

    public int getPropertyValueId() {
        return propertyValueId;
    }

    public void setPropertyValueId(int propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) componentId;
        hash += (int) propertyTypeId;
        hash += (int) propertyValueId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ComponentPropertyPK)) {
            return false;
        }
        ComponentPropertyPK other = (ComponentPropertyPK) object;
        if (this.componentId != other.componentId) {
            return false;
        }
        if (this.propertyTypeId != other.propertyTypeId) {
            return false;
        }
        if (this.propertyValueId != other.propertyValueId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.anl.aps.cdb.portal.model.entities.ComponentPropertyPK[ componentId=" + componentId + ", propertyTypeId=" + propertyTypeId + ", propertyValueId=" + propertyValueId + " ]";
    }
    
}
