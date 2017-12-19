/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.portal.controllers.settings.CdbDomainEntitySettings;
import gov.anl.aps.cdb.portal.model.db.beans.CdbEntityFacade;
import gov.anl.aps.cdb.portal.model.db.beans.PropertyTypeFacade;
import gov.anl.aps.cdb.portal.model.db.beans.PropertyValueFacade;
import gov.anl.aps.cdb.portal.model.db.entities.CdbDomainEntity;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.Log;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.model.db.utilities.LogUtility;
import gov.anl.aps.cdb.portal.model.db.utilities.PropertyValueUtility;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.model.DataModel;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author djarosz
 * @param <EntityType>
 * @param <FacadeType>
 */
public abstract class CdbDomainEntityController<EntityType extends CdbDomainEntity, FacadeType extends CdbEntityFacade<EntityType>, DomainEntitySettingsObject extends CdbDomainEntitySettings> extends CdbEntityController<EntityType, FacadeType, DomainEntitySettingsObject> implements Serializable, ICdbDomainEntityController<EntityType> {

    private PropertyValue currentEditPropertyValue;

    @EJB
    private PropertyValueFacade propertyValueDbFacade;
    @EJB
    private PropertyTypeFacade propertyTypeFacade;   

    protected List<Integer> loadedDisplayPropertyTypes = null;

    protected Log newLogEdit;

    private static final Logger logger = Logger.getLogger(CdbDomainEntity.class.getName());    

    private List<PropertyValue> filteredPropertyValueList;

    protected Integer preProcessLoadedListDataModelHashCode = null;
    protected DataModel preProcessDomainEntityListDataModel = null;

    public void selectPropertyTypes(List<PropertyType> propertyTypeList) {
        for (PropertyType propertyType : propertyTypeList) {
            preparePropertyTypeValueAdd(propertyType);
        }
    }

    public void selectPropertyType(PropertyType propertyType, String onSuccessCommand) {
        if (propertyType != null) {
            PropertyValue propertyValue = preparePropertyTypeValueAdd(propertyType);
            setCurrentEditPropertyValue(propertyValue);
            RequestContext.getCurrentInstance().execute(onSuccessCommand);
        } else {
            SessionUtility.addWarningMessage("No property type selected", "Please select a property type.");
            currentEditPropertyValue = null;
        }
    }

    public PropertyValue preparePropertyTypeValueAdd(PropertyType propertyType) {
        return preparePropertyTypeValueAdd(propertyType, propertyType.getDefaultValue());
    }

    public PropertyValue preparePropertyTypeValueAdd(PropertyType propertyType, String propertyValueString) {
        return preparePropertyTypeValueAdd(propertyType, propertyValueString, null);
    }

    public PropertyValue preparePropertyTypeValueAdd(PropertyType propertyType, String propertyValueString, String tag) {
        EntityType cdbDomainEntity = getCurrent();
        return preparePropertyTypeValueAdd(cdbDomainEntity, propertyType, propertyValueString, tag); 
    }
    
    public PropertyValue preparePropertyTypeValueAdd(EntityType cdbDomainEntity, PropertyType propertyType) {
        return preparePropertyTypeValueAdd(cdbDomainEntity, propertyType, propertyType.getDefaultValue(), null); 
    }
    
    public PropertyValue preparePropertyTypeValueAdd(EntityType cdbDomainEntity, 
            PropertyType propertyType, String propertyValueString, String tag) {       
        UserInfo lastModifiedByUser = (UserInfo) SessionUtility.getUser();
        Date lastModifiedOnDateTime = new Date();

        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setPropertyType(propertyType);
        propertyValue.setValue(propertyValueString);
        propertyValue.setUnits(propertyType.getDefaultUnits());
        cdbDomainEntity.addPropertyValueToPropertyValueList(propertyValue);        
        propertyValue.setEnteredByUser(lastModifiedByUser);
        propertyValue.setEnteredOnDateTime(lastModifiedOnDateTime);
        if (tag != null) {
            propertyValue.setTag(tag);
        }

        cdbDomainEntity.resetPropertyValueLists();

        return propertyValue;
    }
    
    public List<PropertyValue> prepareImageListForCurrent() {
        return prepareImageList(getCurrent()); 
    }

    public List<PropertyValue> prepareImageList(EntityType cdbDomainEntity) {
        if (cdbDomainEntity == null) {
            return null;
        }
        List<PropertyValue> imageList = PropertyValueUtility.prepareImagePropertyValueList(cdbDomainEntity.getPropertyValueList(), settingObject.getDisplayGalleryViewableDocuments());
        cdbDomainEntity.setImagePropertyList(imageList);
        return imageList;
    }

    public List<PropertyValue> getImageList() {
        EntityType cdbDomainEntity = getCurrent();
        if (cdbDomainEntity == null) {
            return null;
        }
        List<PropertyValue> cdbDomainEntityImageList = cdbDomainEntity.getImagePropertyList();
        if (cdbDomainEntityImageList == null) {
            cdbDomainEntityImageList = prepareImageList(cdbDomainEntity);
        }
        return cdbDomainEntityImageList;
    }
    
    public Boolean getDisplayPropertyMetadata(PropertyValue propertyValue) {
        // Maybe future property types will have internal property metadata. 
        // Temporarly done for disabling the metadata for specific entitiees. 
        return true; 
    }

    public Boolean getDisplayImages() {
        List<PropertyValue> cdbDomainEntityImageList = getImageList();
        return (cdbDomainEntityImageList != null && !cdbDomainEntityImageList.isEmpty());
    }

    public PropertyValue getCurrentEditPropertyValue() {
        return currentEditPropertyValue;
    }

    public void setCurrentEditPropertyValue(PropertyValue currentEditPropertyValue) {
        this.currentEditPropertyValue = currentEditPropertyValue;
    }

    public void restoreCurrentEditPropertyValueToOriginalState() {
        if (currentEditPropertyValue != null) {
            EntityType cdbDomainEntity = getCurrent();
            if (currentEditPropertyValue.getId() == null) {
                // Never saved so it should be removed from the property value list
                cdbDomainEntity.getPropertyValueList().remove(currentEditPropertyValue);
                cdbDomainEntity.resetPropertyValueLists();
            } else {
                // Update the current Edit value pointer to db info.
                PropertyValue originalValue = propertyValueDbFacade.find(currentEditPropertyValue.getId());
                currentEditPropertyValue.setValue(originalValue.getValue());
                currentEditPropertyValue.setTag(originalValue.getTag());
                currentEditPropertyValue.setDescription(originalValue.getDescription());
                currentEditPropertyValue.setUnits(originalValue.getUnits());
                currentEditPropertyValue.setIsDynamic(originalValue.getIsDynamic());
                currentEditPropertyValue.setIsUserWriteable(originalValue.getIsUserWriteable());
            }
            currentEditPropertyValue = null;
        }

    }

    public void updateEditProperty() {        
        this.update();
    }

    public void deleteCurrentEditPropertyValue() {
        this.deleteProperty(currentEditPropertyValue);
        currentEditPropertyValue = null;
    }

    public void deleteProperty(PropertyValue cdbDomainEntityProperty) {
        EntityType entity = getCurrent();
        List<PropertyValue> cdbDomainEntityPropertyList = entity.getPropertyValueList();
        cdbDomainEntityPropertyList.remove(cdbDomainEntityProperty);
        updateOnRemoval();
    }

    public void savePropertyList() {
        update();
    }   

    @Override
    public String customizeListDisplay() {
        resetDomainEntityPropertyTypeIdIndexMappings();
        forceLoadPreProcessListDataModel();

        return super.customizeListDisplay();
    }   

    @Override
    public DataModel getListDataModel() {
        DataModel cdbDomainEntityDataModel = super.getListDataModel();

        loadPreProcessListDataModelIfNeeded(listDataModel);

        return cdbDomainEntityDataModel;
    }

    /**
     * Get hash code of the currently loaded data model.
     *
     * @return hash code integer.
     */
    private int getPreProcessDomainEntityListDataModelHashCode() {
        return preProcessDomainEntityListDataModel.hashCode();
    }

    /**
     * Should be executed whenever a relevant list datamodel is fetched.
     *
     * @param preProcessListDataModel dataModel that will be used for pre
     * process actions.
     */
    protected void loadPreProcessListDataModelIfNeeded(DataModel preProcessListDataModel) {
        if (preProcessListDataModel != null) {
            preProcessDomainEntityListDataModel = preProcessListDataModel;

            // Check if requested list dataMode was loaded.
            if (preProcessLoadedListDataModelHashCode == null
                    || getPreProcessDomainEntityListDataModelHashCode() != preProcessLoadedListDataModelHashCode) {
                logger.debug("Desired list data model changed. Starting data model pre process.");
                resetPreProcessVariables();
                processPreProcessDomainEntityListDataModel();
            }

            preProcessLoadedListDataModelHashCode = getPreProcessDomainEntityListDataModelHashCode();
        }
    }
    
    /**
     * Variables that need to be reset when list data model changes. 
     * 
     */
    protected void resetPreProcessVariables() {
        loadedDisplayPropertyTypes = null; 
    }

    /**
     * Check to determine if based on the current settings, a pre process is
     * required.
     *
     * @return preProcess iterate should be executed.
     */
    protected boolean isPreProcessListDataModelIterateNeeded() {
        if (isFilterByPropertiesAutoLoad()) {
            return isPreProcessPropertyValueInformationSettingsPresent();
        }
        return false;
    }

    /**
     * preProcess iterate was execute. Update any necessary variables prior to
     * processing.
     */
    protected void preparePreProcessListDataModelIterate() {
        if (isFilterByPropertiesAutoLoad()) {
            loadedDisplayPropertyTypes = new ArrayList<>();
        }
    }

    /**
     * Iterate over preprocess datamodel if required by current settings.
     */
    private void processPreProcessDomainEntityListDataModel() {
        if (isPreProcessListDataModelIterateNeeded()) {            
            iteratePreProcessListDataModel();
        }
    }
    
    public void resetDomainEntityPropertyTypeIdIndexMappings() {
        ItemElement.setPropertyTypeIdIndex(1, settingObject.getDisplayPropertyTypeId1());
        ItemElement.setPropertyTypeIdIndex(2, settingObject.getDisplayPropertyTypeId2());
        ItemElement.setPropertyTypeIdIndex(3, settingObject.getDisplayPropertyTypeId3());
        ItemElement.setPropertyTypeIdIndex(4, settingObject.getDisplayPropertyTypeId4());
        ItemElement.setPropertyTypeIdIndex(5, settingObject.getDisplayPropertyTypeId5());
    }

    /**
     * Performs pre process skipping list data model hash check.
     */
    public void forceLoadPreProcessListDataModel() {
        processPreProcessDomainEntityListDataModel();
    }
    
    protected Boolean isFilterByPropertiesAutoLoad() {
        Boolean filterByPropertiesAutoLoad = settingObject.getFilterByPropertiesAutoLoad(); 
        return filterByPropertiesAutoLoad != null && filterByPropertiesAutoLoad;
    }
    
    public void loadPreProcessPropertyValueInformationColumns() {
        logger.debug("User requested loading of property values for current data model.");
        if (isFilterByPropertiesAutoLoad()) {
            forceLoadPreProcessListDataModel();
        } else {
            settingObject.setFilterByPropertiesAutoLoad(true);
            forceLoadPreProcessListDataModel();
            settingObject.setFilterByPropertiesAutoLoad(false);
        }
    }

    /**
     * Execute on each iteration of pre process iterateListDataModel.
     *
     * @param entity
     */
    protected void processPreProcessIteratedDomainEntity(CdbDomainEntity entity) {
        if (isFilterByPropertiesAutoLoad()) {
            setPreProcessPropertyValueInformation(entity);
        }
    }

    /**
     * PreProcess iterate execution is completed; update any necessary
     * variables.
     */
    protected void updatePreProcessListProcessedVariables() {
        if (isFilterByPropertiesAutoLoad()) {
            updatePreProcessCurrentPropertyValueSettingsLoaded();
        }
    }

    protected boolean isPreProcessPropertyValueInformationSettingsPresent() {
        return settingObject.getDisplayPropertyTypeId1() != null
                || settingObject.getDisplayPropertyTypeId2() != null
                || settingObject.getDisplayPropertyTypeId3() != null
                || settingObject.getDisplayPropertyTypeId4() != null
                || settingObject.getDisplayPropertyTypeId5() != null;
    }

    /**
     * Loads all of the necessary property types for the entity passed.
     *
     * @param entity
     */
    protected void setPreProcessPropertyValueInformation(CdbDomainEntity entity) {
        loadPropertyValueInformation(settingObject.getDisplayPropertyTypeId1(), entity);
        loadPropertyValueInformation(settingObject.getDisplayPropertyTypeId2(), entity);
        loadPropertyValueInformation(settingObject.getDisplayPropertyTypeId3(), entity);
        loadPropertyValueInformation(settingObject.getDisplayPropertyTypeId4(), entity);
        loadPropertyValueInformation(settingObject.getDisplayPropertyTypeId5(), entity);
    }

    /**
     * Marks all of the currently set property types
     */
    protected void updatePreProcessCurrentPropertyValueSettingsLoaded() {
        addPropertyTypeToLoadedDisplayPropertyTypes(settingObject.getDisplayPropertyTypeId1());
        addPropertyTypeToLoadedDisplayPropertyTypes(settingObject.getDisplayPropertyTypeId2());
        addPropertyTypeToLoadedDisplayPropertyTypes(settingObject.getDisplayPropertyTypeId3());
        addPropertyTypeToLoadedDisplayPropertyTypes(settingObject.getDisplayPropertyTypeId4());
        addPropertyTypeToLoadedDisplayPropertyTypes(settingObject.getDisplayPropertyTypeId5());
    }

    /**
     * Execute iterate over preProcess List data model.
     */
    private void iteratePreProcessListDataModel() {
        if (preProcessDomainEntityListDataModel != null) {
            logger.debug("Data model pre process iterate starting.");
            preparePreProcessListDataModelIterate();
            DataModel<CdbDomainEntity> cdbDomainEntityList = preProcessDomainEntityListDataModel;
            Iterator<CdbDomainEntity> cdbDomainEntityIterator = cdbDomainEntityList.iterator();
            while (cdbDomainEntityIterator.hasNext()) {
                CdbDomainEntity cdbDomainEntity = cdbDomainEntityIterator.next();
                processPreProcessIteratedDomainEntity(cdbDomainEntity);
            }
            updatePreProcessListProcessedVariables();
        }        
    }

    protected void addPropertyTypeToLoadedDisplayPropertyTypes(Integer propertyTypeId) {
        if (propertyTypeId != null) {
            if (!loadedDisplayPropertyTypes.contains(propertyTypeId)) {
                loadedDisplayPropertyTypes.add(propertyTypeId);
            }
        }
    }

    protected void loadPropertyValueInformation(Integer propertyTypeId, CdbDomainEntity entity) {
        if (propertyTypeId != null) {
            entity.getPropertyValueInformation(propertyTypeId);
        }
    }

    protected Boolean fetchFilterablePropertyValue(Integer propertyTypeId) {
        if (propertyTypeId != null) {
            if (loadedDisplayPropertyTypes != null) {
                if (preProcessLoadedListDataModelHashCode != null && getPreProcessDomainEntityListDataModelHashCode() != preProcessLoadedListDataModelHashCode) {
                    loadedDisplayPropertyTypes = null;
                    return false;
                }
                for (Integer loadedPropertyTypeId : loadedDisplayPropertyTypes) {
                    if (Objects.equals(propertyTypeId, loadedPropertyTypeId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }        

    protected Boolean checkDisplayLoadPropertyValueButtonByProperty(Integer propertyTypeId) {
        if (propertyTypeId == null) {
            return false;
        } else {
            return !fetchFilterablePropertyValue(propertyTypeId);
        }
    }

    public Boolean getFilterablePropertyValue1() {
        return fetchFilterablePropertyValue(settingObject.getDisplayPropertyTypeId1());
    }

    public Boolean getFilterablePropertyValue2() {
        return fetchFilterablePropertyValue(settingObject.getDisplayPropertyTypeId2());
    }

    public Boolean getFilterablePropertyValue3() {
        return fetchFilterablePropertyValue(settingObject.getDisplayPropertyTypeId3());
    }

    public Boolean getFilterablePropertyValue4() {
        return fetchFilterablePropertyValue(settingObject.getDisplayPropertyTypeId4());
    }

    public Boolean getFilterablePropertyValue5() {
        return fetchFilterablePropertyValue(settingObject.getDisplayPropertyTypeId5());
    }
    
    @Override
    public Boolean getDisplayLoadPropertyValuesButton() {
        if (isFilterByPropertiesAutoLoad()) {
            return false;
        }

        return (checkDisplayLoadPropertyValueButtonByProperty(settingObject.getDisplayPropertyTypeId1())
                || checkDisplayLoadPropertyValueButtonByProperty(settingObject.getDisplayPropertyTypeId2())
                || checkDisplayLoadPropertyValueButtonByProperty(settingObject.getDisplayPropertyTypeId3())
                || checkDisplayLoadPropertyValueButtonByProperty(settingObject.getDisplayPropertyTypeId4())
                || checkDisplayLoadPropertyValueButtonByProperty(settingObject.getDisplayPropertyTypeId5()));
    }

    public String getDisplayPropertyTypeName(Integer propertyTypeId) {
        if (propertyTypeId != null) {

            try {
                PropertyType propertyType = propertyTypeFacade.find(propertyTypeId);
                return propertyType.getName();
            } catch (Exception ex) {
                return "Unknown Property";
            }
        }
        return null;
    }

    public boolean isDisplayRowExpansionLogs(CdbDomainEntity domainEntity) {
        if (domainEntity != null) {
            List<Log> itemLog = domainEntity.getLogList();
            return itemLog != null && !itemLog.isEmpty();
        }
        return false; 
    }

    public boolean isDisplayRowExpansionProperties(CdbDomainEntity domainEntity) {
        if (domainEntity != null) {
            List<PropertyValue> itemProperties = domainEntity.getPropertyValueDisplayList();
            return itemProperties != null && !itemProperties.isEmpty();
        }
        return false;
    }   

    public Boolean getDisplayLogList() {
        EntityType cdbDomainEntity = getCurrent();
        if (cdbDomainEntity != null) {
            List<Log> logList = cdbDomainEntity.getLogList();
            return logList != null && !logList.isEmpty();
        }
        return false;
    }

    public Boolean getDisplayPropertyList() {
        EntityType cdbDomainEntity = getCurrent();
        if (cdbDomainEntity != null) {
            List<PropertyValue> propertyValueList = cdbDomainEntity.getPropertyValueDisplayList();
            return propertyValueList != null && !propertyValueList.isEmpty();
        }
        return false;
    }

    public void logObjectEditRowEvent(RowEditEvent event) {
        this.saveLogList();
    }

    public Log getNewLogEdit() {
        return newLogEdit;
    }

    public void setNewLogEdit(Log newLogEdit) {
        this.newLogEdit = newLogEdit;
    }

    public void removeNewLog() {
        if (newLogEdit != null) {
            EntityType cdbDomainEntity = this.current;
            cdbDomainEntity.getLogList().remove(newLogEdit);
            newLogEdit = null;
        }
    }

    public void saveLogList() {
        newLogEdit = null;
        update();
    }

    public void prepareAddLog(EntityType cdbDomainEntity) {
        Log logEntry = LogUtility.createLogEntry();
        setNewLogEdit(logEntry);
        List<Log> cdbDomainEntityLogList = cdbDomainEntity.getLogList();
        cdbDomainEntityLogList.add(0, logEntry);
    }

    public List<Log> getLogList() {
        EntityType cdbDomainEntity = getCurrent();
        List<Log> domainEntityLogList = cdbDomainEntity.getLogList();
        return domainEntityLogList;
    }

    public void deleteLog(Log cdbDomainEntityLog) {
        EntityType cdbDomainEntity = getCurrent();
        List<Log> cdbDomainEntityLogList = cdbDomainEntity.getLogList();
        cdbDomainEntityLogList.remove(cdbDomainEntityLog);
        updateOnRemoval();
    }

    public List<PropertyValue> getFilteredPropertyValueList() {
        return filteredPropertyValueList;
    }

    public void setFilteredPropertyValueList(List<PropertyValue> filteredPropertyValueList) {
        this.filteredPropertyValueList = filteredPropertyValueList;
    }
}
