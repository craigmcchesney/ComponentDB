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
import java.util.ArrayList;
import java.util.List;
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
    
    public class ValidateInfo {
        
        public boolean valid = false;
        public String message = "";
        
        public ValidateInfo(boolean res, String msg) {
            valid = res;
            message = msg;
        }
    }
    
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
    
    private String matchNameFromList(String str, int startIndex, List<String> names) {
        
        if (startIndex >= str.length()) {
            return "";
        }
        
        for (String name : names) {
            
            if (str.length() < name.length()) {
                break;
            } 
            
            for (int nameIndex = 0 ; nameIndex < name.length() ; nameIndex++) {
                if (name.charAt(nameIndex) == str.charAt(startIndex + nameIndex)) {
                    if (nameIndex == name.length() - 1) {
                        // found a match
                        return name;
                    }
                } else {
                    break;
                }
            }
        }
        return "";
    }
    
    private String matchInstanceNumber(String str, int startIndex) {
        
        int index = startIndex;
        String instanceNumber = "";
        
        while ((index < str.length()) && (Character.isDigit(str.charAt(index)))) {
            instanceNumber = instanceNumber + str.charAt(index);
            index = index + 1;
        }
        
        return instanceNumber;
    }
    
    public ValidateInfo validateDeviceName(String name) {
        
        String systemName = "";
        String systemInstance = "";
        List<String> subsystemNames = new ArrayList<>();
        List<String> subsystemInstances = new ArrayList<>();
        String deviceName = "";
        String deviceInstance = "";
        
        String[] tokens = name.split(":");
        
        if ((tokens.length != 2)) {
            return new ValidateInfo(false, 
                    "device name must include 2 parts separated by a colon");
        }
        
        String systemToken = tokens[0];
        String deviceToken = tokens[1];

        List<String> dbSystemNames = getEntityDbFacade().getSystemNames();
        List<String> dbSubsystemNames = getEntityDbFacade().getSubsystemNames();
        List<String> dbDeviceNames = getEntityDbFacade().getDeviceNames();
        
        int parseIndex = 0;
        
        // match system name
        systemName = matchNameFromList(systemToken, parseIndex, dbSystemNames);
        if (systemName == "") {
            // no match on system
            return new ValidateInfo(false, "missing or invalid system: " + systemToken);
        } else {
            parseIndex = parseIndex + systemName.length();
        }
        
        System.out.println("system: " + systemName + " index: " + parseIndex);
        
        // match system instance if present
        systemInstance = matchInstanceNumber(systemToken, parseIndex);
        if (systemInstance != "") {
            parseIndex = parseIndex + systemInstance.length();
        }
        
        System.out.println("instance: " + systemInstance + " index: " + parseIndex);
        
        // can include multiple subsystems with instance
        while (parseIndex < systemToken.length()) {
        
            // match subsystem name
            String subsystemName = matchNameFromList(systemToken, parseIndex, dbSubsystemNames);
            if (subsystemName == "") {
                // make sure we have at least one subsystem name
                if (subsystemNames.size() == 0) {
                    return new ValidateInfo(false, "missing or invalid subsystem: " + 
                            systemToken.substring(parseIndex));
                } else {
                    break;
                }
            } else {
                // add subsystem name to list
                subsystemNames.add(subsystemName);
                parseIndex = parseIndex + subsystemName.length();

                System.out.println("subsystem: " + subsystemName + " index: " + parseIndex);
            
                // match subsystem instance if present
                String subsystemInstance = matchInstanceNumber(systemToken, parseIndex);
                // add subsystem instance to list, even if empty so list indexes match
                subsystemInstances.add(subsystemInstance);
                if (subsystemInstance != "") {
                    parseIndex = parseIndex + subsystemInstance.length();
                }
            }
        }
        
        // make sure all system token characters processed
        if (parseIndex < systemToken.length()) {
            return new ValidateInfo(false, "extra characters after subsystem: " + 
                    systemToken.substring(parseIndex));
        }
      
        // now validate device token
        parseIndex = 0;
        deviceName = matchNameFromList(deviceToken, parseIndex, dbDeviceNames);
        if (deviceName == "") {
            // no match on device
            return new ValidateInfo(false, "missing or invalid device: " + deviceToken);
        } else {
            parseIndex = parseIndex + deviceName.length();
        }
        
        System.out.println("device: " + deviceName + " index: " + parseIndex);
        
        // match device instance if present
        deviceInstance = matchInstanceNumber(deviceToken, parseIndex);
        if (deviceInstance != "") {
            parseIndex = parseIndex + deviceInstance.length();
        }
        
        System.out.println("instance: " + deviceInstance + " index: " + parseIndex);
        
        // make sure all device token characters processed
        if (parseIndex < deviceToken.length()) {
            return new ValidateInfo(false, "extra characters after device: " + 
                    deviceToken.substring(parseIndex));
        }
        
        String msg = "valid system: " + systemName +
                " instance: " + systemInstance +
                " subsystems: " + subsystemNames +
                " instances: " + subsystemInstances +
                " device: " + deviceName +
                " instance: " + deviceInstance;
        
        System.out.println(msg);
        
        return new ValidateInfo(true, msg);
    }
    
}
