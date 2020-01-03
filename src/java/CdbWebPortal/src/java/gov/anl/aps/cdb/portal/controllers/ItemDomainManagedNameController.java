/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.extensions.ImportHelperManagedNameValidateDevices;
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

    public class NameParseInfo {
        public boolean valid = false;
        public String message = "";
        public SystemNameInfo systemNameInfo;
        public DeviceNameInfo deviceNameInfo;
        public SignalNameInfo signalNameInfo;
        
        public String generateValidDeviceNameMessage() {
            String subsystemString = "";
            for (SubsystemNameInfo info : systemNameInfo.subsystems) {
                subsystemString = subsystemString +
                        " subsystem: " + info.name + 
                        " instance: " + info.instance;
            }
            return "valid system: " + systemNameInfo.name +
                " instance: " + systemNameInfo.instance +
                subsystemString +
                " device: " + deviceNameInfo.name +
                " instance: " + deviceNameInfo.instance;
        }
        
        public String generateValidSignalNameMessage() {
            String deviceMessage = generateValidDeviceNameMessage();
            return deviceMessage +
                    " signal: " + signalNameInfo.name;
        }
    }
    
    public class SubsystemNameInfo {
        String name = "";
        String instance = "";
        
        public SubsystemNameInfo(String n, String i) {
            name = n;
            instance = i;
        }
    }
    
    public class SystemNameInfo {
        public boolean valid = false;
        public String validMessage = "";
        public String name = "";
        public String instance = "";
        public List<SubsystemNameInfo> subsystems = new ArrayList<>();
    }
    
    public class DeviceNameInfo {
        public boolean valid = false;
        public String validMessage = "";
        public String name = "";
        public String instance = "";
    }

    public class SignalNameInfo {
        public boolean valid = false;
        public String validMessage = "";
        public String name = "";
    }

    public static final String CONTROLLER_NAMED = "itemDomainManagedNameController";

    protected ImportHelperManagedNameValidateDevices importHelperValidateDevices = new ImportHelperManagedNameValidateDevices();
    
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

    /**
     * Prepares import wizard.
     */
    public String prepareWizardValidateDevices() {
        importHelperValidateDevices.reset();
        ItemDomainImportWizard.getInstance().registerHelper(importHelperValidateDevices);
        return "/views/itemDomainManagedName/validateDevices?faces-redirect=true";
    }

    /**
     * Prepares import wizard.
     */
    public String prepareWizardValidateSignals() {
//        importHelperValidateDevices.reset();
//        ItemDomainImportWizard.getInstance().registerHelper(importHelperValidateDevices);
//        return "/views/itemDomainCableCatalog/validateDevices?faces-redirect=true";
        return "";
    }

    /**
     * Prepares import wizard.
     */
    public String prepareWizardImportDevices() {
//        importHelperValidateDevices.reset();
//        ItemDomainImportWizard.getInstance().registerHelper(importHelperValidateDevices);
//        return "/views/itemDomainCableCatalog/validateDevices?faces-redirect=true";
        return "";
    }

    /**
     * Prepares import wizard.
     */
    public String prepareWizardImportSignals() {
//        importHelperValidateDevices.reset();
//        ItemDomainImportWizard.getInstance().registerHelper(importHelperValidateDevices);
//        return "/views/itemDomainCableCatalog/validateDevices?faces-redirect=true";
        return "";
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

            for (int nameIndex = 0; nameIndex < name.length(); nameIndex++) {
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
    
    protected SystemNameInfo parseSystemToken(String systemToken, List<String> dbSystemNames, List<String> dbSubsystemNames) {
        
        SystemNameInfo info = new SystemNameInfo();
        
        String systemName = "";
        String systemInstance = "";
        int parseIndex = 0;

        // match system name
        systemName = matchNameFromList(systemToken, parseIndex, dbSystemNames);
        if (systemName == "") {
            // no match on system
            info.valid = false;
            info.validMessage = "missing or invalid system: " + systemToken;
            return info;
        } else {
            info.name = systemName;
            parseIndex = parseIndex + systemName.length();
        }

        System.out.println("system: " + systemName + " index: " + parseIndex);

        // match system instance if present
        systemInstance = matchInstanceNumber(systemToken, parseIndex);
        if (systemInstance != "") {
            info.instance = systemInstance;
            parseIndex = parseIndex + systemInstance.length();
        }

        System.out.println("instance: " + systemInstance + " index: " + parseIndex);

        // can include multiple subsystems with instance
        while (parseIndex < systemToken.length()) {

            // match subsystem name
            String subsystemName = matchNameFromList(systemToken, parseIndex, dbSubsystemNames);
            if (subsystemName == "") {
                // make sure we have at least one subsystem name
                if (info.subsystems.size() == 0) {
                    info.valid = false;
                    info.validMessage = "missing or invalid subsystem: "
                            + systemToken.substring(parseIndex);
                    return info;
                } else {
                    break;
                }
            } else {
                // save subsystem name and instance if present, and update parseIndex
                parseIndex = parseIndex + subsystemName.length();
                // match subsystem instance if present
                String subsystemInstance = matchInstanceNumber(systemToken, parseIndex);
                info.subsystems.add(new SubsystemNameInfo(subsystemName, subsystemInstance));
                if (subsystemInstance != "") {
                    parseIndex = parseIndex + subsystemInstance.length();
                }
                System.out.println("subsystem: " + subsystemName + " instance: " + subsystemInstance + " index: " + parseIndex);
            }
        }

        // make sure all system token characters processed
        if (parseIndex < systemToken.length()) {
            info.valid = false;
            info.validMessage = "extra characters after subsystem: "
                    + systemToken.substring(parseIndex);
        } else {
            info.valid = true;
        }
        
        return info;
    }
    
    protected DeviceNameInfo parseDeviceToken(String deviceToken, List<String> dbDeviceNames) {
        
        DeviceNameInfo info = new DeviceNameInfo();
        
        // match device name
        String deviceName = "";
        String deviceInstance = "";
        int parseIndex = 0;
        deviceName = matchNameFromList(deviceToken, parseIndex, dbDeviceNames);
        if (deviceName == "") {
            // no match on device
            info.valid = false;
            info.validMessage = "missing or invalid device: " + deviceToken;
            return info;
        } else {
            info.name = deviceName;
            parseIndex = parseIndex + deviceName.length();
        }

        System.out.println("device: " + deviceName + " index: " + parseIndex);

        // match device instance if present
        deviceInstance = matchInstanceNumber(deviceToken, parseIndex);
        if (deviceInstance != "") {
            info.instance = deviceInstance;
            parseIndex = parseIndex + deviceInstance.length();
        }

        System.out.println("instance: " + deviceInstance + " index: " + parseIndex);

        // make sure all device token characters processed
        if (parseIndex < deviceToken.length()) {
            info.valid = false;
            info.validMessage = "extra characters after device: "
                    + deviceToken.substring(parseIndex);
        } else {
            info.valid = true;
        }

        return info;
    }

    protected SignalNameInfo parseSignalToken(String signalToken, List<String> dbSignalNames) {
        
        SignalNameInfo info = new SignalNameInfo();
        
        // match signal name
        String signalName = "";
        int parseIndex = 0;
        signalName = matchNameFromList(signalToken, parseIndex, dbSignalNames);
        if (signalName == "") {
            // no match on signal
            info.valid = false;
            info.validMessage = "missing or invalid signal: " + signalToken;
            return info;
        } else {
            info.name = signalName;
            parseIndex = parseIndex + signalName.length();
        }

        System.out.println("signal: " + signalName + " index: " + parseIndex);

        // make sure all signal token characters processed
        if (parseIndex < signalToken.length()) {
            info.valid = false;
            info.validMessage = "extra characters after signal: "
                    + signalToken.substring(parseIndex);
        } else {
            info.valid = true;
        }

        return info;
    }

    protected NameParseInfo parseName(String name, boolean parseSignal) {
        
        NameParseInfo info = new NameParseInfo();

        List<String> dbSystemNames = getEntityDbFacade().getSystemNames();
        List<String> dbSubsystemNames = getEntityDbFacade().getSubsystemNames();
        List<String> dbDeviceNames = getEntityDbFacade().getDeviceNames();
        List<String> dbSignalNames = new ArrayList<>();
        
        String[] tokens = name.split(":");

        if (parseSignal) {
            dbSignalNames = getEntityDbFacade().getSignalNames();
            if ((tokens.length != 3)) {
                info.valid = false;
                info.message = "signal name must include 3 parts separated by a colon";
                return info;
            }

        } else {
            if ((tokens.length != 2)) {
                info.valid = false;
                info.message = "device name must include 2 parts separated by a colon";
                return info;
            }
        }

        String systemToken = tokens[0];
        String deviceToken = tokens[1];
        String signalToken = "";
        if (parseSignal) {
            signalToken = tokens[2];
        }
        
        // parse system and subsystem
        SystemNameInfo systemNameInfo = parseSystemToken(systemToken, dbSystemNames, dbSubsystemNames);        
        if (systemNameInfo.valid == false) {
            info.valid = false;
            info.message = systemNameInfo.validMessage;
            return info;
        } else {
            info.systemNameInfo = systemNameInfo;
        }

        // parse device
        DeviceNameInfo deviceNameInfo = parseDeviceToken(deviceToken, dbDeviceNames);        
        if (deviceNameInfo.valid == false) {
            info.valid = false;
            info.message = deviceNameInfo.validMessage;
            return info;
        } else {
            info.deviceNameInfo = deviceNameInfo;
        }
        
        // optionally, parse signal
        if (parseSignal) {
            SignalNameInfo signalNameInfo = parseSignalToken(signalToken, dbSignalNames);
            if (signalNameInfo.valid == false) {
                info.valid = false;
                info.message = signalNameInfo.validMessage;
                return info;
            } else {
                info.signalNameInfo = signalNameInfo;
            }
        }

        String msg;
        if (parseSignal) {
            msg = info.generateValidSignalNameMessage();
        } else {
            msg = info.generateValidDeviceNameMessage();
        }

        System.out.println(msg);
        
        info.valid = true;
        info.message = msg;
        return info;
    }

    public NameParseInfo parseDeviceName(String name) {        
        return parseName(name, false);        
    }
    
    public NameParseInfo parseSignalName(String name) {        
        return parseName(name, true);        
    }
    
}
