/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.anl.aps.cdb.portal.model.beans;

import gov.anl.aps.cdb.portal.model.entities.ComponentTypeCategory;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author sveseli
 */
@Stateless
public class ComponentTypeCategoryFacade extends AbstractFacade<ComponentTypeCategory>
{
    @PersistenceContext(unitName = "CdbWebPortalPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ComponentTypeCategoryFacade() {
        super(ComponentTypeCategory.class);
    }
    
}
