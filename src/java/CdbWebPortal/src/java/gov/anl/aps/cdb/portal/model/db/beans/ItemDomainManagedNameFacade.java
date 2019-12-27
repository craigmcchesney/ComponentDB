/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.beans;

import gov.anl.aps.cdb.portal.model.db.entities.Domain;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainManagedName;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;

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
    
    public List<String> getSystemNames() {
        
        String queryString = 
                "SELECT i.name from Item i " +
                "JOIN i.itemCategoryList cl " +
                "WHERE i.domain.name = " + "'Managed Name' " +
                "AND cl.name = " + "'System' " +
                "ORDER BY i.name";

        try {
            return (List<String>) em.createQuery(queryString).getResultList();
        } catch (NoResultException ex) {
        }

        return new ArrayList<>();
    }

    public List<String> getSubsystemNames() {
        
        String queryString = 
                "SELECT i.name from Item i " +
                "JOIN i.itemCategoryList cl " +
                "WHERE i.domain.name = " + "'Managed Name' " +
                "AND cl.name = " + "'Subsystem' " +
                "ORDER BY i.name";

        try {
            return (List<String>) em.createQuery(queryString).getResultList();
        } catch (NoResultException ex) {
        }

        return new ArrayList<>();
    }
    
    public List<String> getDeviceNames() {
        
        String queryString = 
                "SELECT i.name from Item i " +
                "JOIN i.itemCategoryList cl " +
                "WHERE i.domain.name = " + "'Managed Name' " +
                "AND cl.name = " + "'Device Type' " +
                "ORDER BY i.name";

        try {
            return (List<String>) em.createQuery(queryString).getResultList();
        } catch (NoResultException ex) {
        }

        return new ArrayList<>();
    }
    
    public List<String> getSignalNames() {
        
        String queryString = 
                "SELECT i.name from Item i " +
                "JOIN i.itemCategoryList cl " +
                "WHERE i.domain.name = " + "'Managed Name' " +
                "AND cl.name = " + "'Signal' " +
                "ORDER BY i.name";

        try {
            return (List<String>) em.createQuery(queryString).getResultList();
        } catch (NoResultException ex) {
        }

        return new ArrayList<>();
    }
    
}
