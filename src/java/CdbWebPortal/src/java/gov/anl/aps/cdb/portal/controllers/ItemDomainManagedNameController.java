/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.extensions.ItemMultiEditController;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainManagedNameSettings;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainManagedNameFacade;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainManagedName;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author djarosz
 */
@Named(ItemDomainManagedNameController.CONTROLLER_NAMED)
@SessionScoped
public class ItemDomainManagedNameController extends ItemController<ItemDomainManagedName, ItemDomainManagedNameFacade, ItemDomainManagedNameSettings> {
    
    public static final String CONTROLLER_NAMED = "itemDomainManagedNameController";
    
    @EJB
    ItemDomainManagedNameFacade itemDomainManagedNameFacade; 
    
    public static ItemDomainManagedNameController getInstance() {
        if (SessionUtility.runningFaces()) {
            return (ItemDomainManagedNameController) SessionUtility.findBean(ItemDomainManagedNameController.CONTROLLER_NAMED);
        } else {
            // TODO add apiInstance
            return null;
        }
    }
    
    @Override
    protected ItemDomainManagedName instenciateNewItemDomainEntity() {
        return new ItemDomainManagedName(); 
    }

    @Override
    protected ItemDomainManagedNameSettings createNewSettingObject() {
        return new ItemDomainManagedNameSettings(this);
    }

    @Override
    protected ItemDomainManagedNameFacade getEntityDbFacade() {
        return itemDomainManagedNameFacade; 
    }

    @Override
    public String getEntityTypeName() {
        return "managedName"; 
    } 

    @Override
    public String getDisplayEntityTypeName() {
        return "Managed Name";
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.managedName.getValue(); 
    }

    @Override
    public boolean getEntityDisplayItemConnectors() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemName() {
        return true;
    }

    @Override
    public boolean getEntityDisplayDerivedFromItem() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayQrId() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemGallery() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemLogs() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemSources() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemProperties() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemElements() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemsDerivedFromItem() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemMemberships() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemProject() {
        return false; 
    }

    @Override
    public boolean getEntityDisplayItemEntityTypes() {
        return false; 
    }

    @Override
    public String getItemsDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStyleName() {
        return "content"; 
    }

    @Override
    public String getDefaultDomainDerivedFromDomainName() {
        return null; 
               
    }

    @Override
    public String getDefaultDomainDerivedToDomainName() {
        return null; 
    } 
    
}
