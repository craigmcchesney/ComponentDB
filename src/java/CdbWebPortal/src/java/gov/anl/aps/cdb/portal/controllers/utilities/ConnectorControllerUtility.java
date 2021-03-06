/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.utilities;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.model.db.beans.ConnectorFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Connector;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemConnector;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;

/**
 *
 * @author darek
 */
public class ConnectorControllerUtility extends CdbEntityControllerUtility<Connector, ConnectorFacade> {
        
    @Override
    public Connector createEntityInstance(UserInfo sessionUser) {
        return new Connector(); 
    }
    
    @Override
    protected void prepareEntityDestroy(Connector connector, UserInfo userInfo) throws CdbException {
        if (verifySafeRemovalOfConnector(connector) == false) {
            throw new CdbException("Cannot remove connector, it has invalid usages."); 
        }
        super.prepareEntityDestroy(connector, userInfo); 
    }
    
    public boolean verifySafeRemovalOfConnector(Connector connector) {
        // Get latest version from DB. 
        connector = findById(connector.getId());        
        if (connector.getItemConnectorList().size() != 1) {
            return false;
        } else {
            // connector itemconnector list has size of 1 
            ItemConnector itemConnector = connector.getItemConnectorList().get(0); 
            Item item = itemConnector.getItem(); 
            if (!item.getDomain().getName().equals(ItemDomainName.catalog.getValue())) {
                return false; 
            }
        } 
        return true; 
    }  

    @Override
    protected ConnectorFacade getEntityDbFacade() {
        return ConnectorFacade.getInstance(); 
    }        

    @Override
    public String getEntityTypeName() {
        return "connector"; 
    }
    
}
