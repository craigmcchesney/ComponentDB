/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.entities;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author craig
 */
@Entity
@DiscriminatorValue(value = ItemDomainName.MANAGED_NAME_ID + "")   
public class ItemDomainManagedName extends Item {

    @Override
    public Item createInstance() {
        return new ItemDomainCableInventory(); 
    }

}
