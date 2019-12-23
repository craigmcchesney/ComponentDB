/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.beans;

import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainManagedName;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import javax.ejb.Stateless;

/**
 *
 * @author craig
 */
@Stateless
public class ItemDomainManagedNameFacade extends ItemFacadeBase<ItemDomainManagedName> {
    
    public ItemDomainManagedNameFacade() {
        super(ItemDomainManagedName.class);
    }
    
    public static ItemDomainManagedNameFacade getInstance() {
        return (ItemDomainManagedNameFacade) SessionUtility.findFacade(ItemDomainManagedNameFacade.class.getSimpleName()); 
    }
    
}
