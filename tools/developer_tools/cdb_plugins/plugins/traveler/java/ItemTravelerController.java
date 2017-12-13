/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.plugins.support.traveler;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.common.exceptions.ConfigurationError;

import gov.anl.aps.cdb.portal.controllers.ICdbDomainEntityController;
import gov.anl.aps.cdb.portal.controllers.ItemControllerExtensionHelper;
import gov.anl.aps.cdb.portal.controllers.PropertyTypeController;
import gov.anl.aps.cdb.portal.model.db.entities.CdbDomainEntity;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyTypeHandler;

import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.model.jsf.handlers.PropertyTypeHandlerFactory;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import gov.anl.aps.cdb.portal.plugins.support.traveler.api.TravelerApi;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.Binder;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.BinderTraveler;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.BinderWorksReference;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.Form;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.Forms;
import gov.anl.aps.cdb.portal.plugins.support.traveler.objects.Traveler;
import javax.faces.model.SelectItem;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.component.html.HtmlInputText;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

public abstract class ItemTravelerController extends ItemControllerExtensionHelper {

    private TravelerApi travelerApi;
    private static final Logger logger = Logger.getLogger(ItemTravelerController.class.getName());

    private PropertyValue propertyValue;
    private Traveler currentTravelerInstance;
    private DateFormat dateFormat;
    private Date currentTravelerDeadline;
    private final Map statusOptions = new HashMap();
    private final Map statusNames = new HashMap();
    private String currentTravelerSelectedStatus;
    private String currentTravelerEditTitle;
    private String currentTravelerEditDescription;

    private Form selectedTemplate;
    private Form selectedTravelerInstanceTemplate;

    private String travelerTemplateTitle;
    private Forms travelerTemplates;

    private String travelerInstanceTitle;

    private List<Form> availableTemplates;

    private HtmlInputText travelerInstanceTitleInputText;

    private final String TRAVELER_WEB_APP_URL = TravelerPluginManager.getTravelerWebApplicationUrl();
    private final String TRAVELER_WEB_APP_TEMPLATE_PATH = TravelerPluginManager.getTravelerWebApplicationTemplatePath();
    private final String TRAVELER_WEB_APP_TRAVELER_PATH = TravelerPluginManager.getTravelerWebApplicationTravelerPath();
    private final String TRAVELER_WEB_APP_BINDER_PATH = TravelerPluginManager.getTravelerWebApplicationBinderPath(); 
    private final String TRAVELER_WEB_APP_TRAVELER_CONFIG_PATH = TravelerPluginManager.getTravelerWebApplicationTravelerConfigPath();

    private List<Form> templatesForCurrent;
    private List<BinderTraveler> travelersForCurrent;

    private PropertyType travelerTemplatePropertyType;
    private PropertyType travelerInstancePropertyType;
    private PropertyType travelerBinderPropertyType;

    private Binder newBinder;
    private List<Form> newBinderTemplateSelection;

    @PostConstruct
    public void init() {
        getItemController().subscribeResetVariablesForCurrent(this);

        String webServiceUrl = TravelerPluginManager.getTravelerWebServiceUrl();
        String username = TravelerPluginManager.getTravelerBasicAuthUsername();
        String password = TravelerPluginManager.getTravelerBasicAuthPassword();
        try {
            travelerApi = new TravelerApi(webServiceUrl, username, password);
        } catch (ConfigurationError ex) {
            String error = "Traveler Service is not accessible:  " + ex.getErrorMessage();
            SessionUtility.addErrorMessage("Error", error);
            logger.error(error);
        }

        dateFormat = new SimpleDateFormat("y-M-d");

        String initalized = "Initialized";
        String active = "Active";
        String submitted = "Submitted";
        String completed = "Complted";
        String frozen = "Frozen";

        statusNames.put(0.0, initalized);
        statusNames.put(1.0, active);
        statusNames.put(1.5, submitted);
        statusNames.put(2.0, completed);
        statusNames.put(3.0, frozen);

        List<SelectItem> initalizedStatusList = new ArrayList<>();
        initalizedStatusList.add(getNewSelectItem(initalized, false));
        initalizedStatusList.add(getNewSelectItem(active, false));
        initalizedStatusList.add(getNewSelectItem(frozen, true));
        initalizedStatusList.add(getNewSelectItem(submitted, true));
        initalizedStatusList.add(getNewSelectItem(completed, true));
        List<SelectItem> activeStatusList = new ArrayList<>();
        activeStatusList.add(getNewSelectItem(initalized, true));
        activeStatusList.add(getNewSelectItem(active, false));
        activeStatusList.add(getNewSelectItem(frozen, false));
        activeStatusList.add(getNewSelectItem(submitted, true));
        activeStatusList.add(getNewSelectItem(completed, true));
        List<SelectItem> submittedStatusList = new ArrayList<>();
        submittedStatusList.add(getNewSelectItem(initalized, true));
        submittedStatusList.add(getNewSelectItem(active, false));
        submittedStatusList.add(getNewSelectItem(frozen, true));
        submittedStatusList.add(getNewSelectItem(submitted, false));
        submittedStatusList.add(getNewSelectItem(completed, false));
        List<SelectItem> completedStatusList = new ArrayList<>();
        completedStatusList.add(getNewSelectItem(initalized, true));
        completedStatusList.add(getNewSelectItem(active, true));
        completedStatusList.add(getNewSelectItem(frozen, true));
        completedStatusList.add(getNewSelectItem(submitted, true));
        completedStatusList.add(getNewSelectItem(completed, false));

        statusOptions.put(0.0, initalizedStatusList);
        statusOptions.put(1.0, activeStatusList);
        statusOptions.put(1.5, submittedStatusList);
        statusOptions.put(2.0, completedStatusList);
        statusOptions.put(3.0, activeStatusList);
    }

    @Override
    public void resetExtensionVariablesForCurrent() {
        super.resetExtensionVariablesForCurrent();
        templatesForCurrent = null;
        travelersForCurrent = null;
        propertyValue = null;
        newBinder = null;
        newBinderTemplateSelection = null;
    }

    public void addTravelerBinderToCurrent(String onSuccess) {
        PropertyType travelerBinderPropertyType = getTravelerBinderPropertyType();

        if (travelerBinderPropertyType != null) {
            propertyValue = getItemController().preparePropertyTypeValueAdd(travelerBinderPropertyType);
            newBinder = new Binder();
            newBinderTemplateSelection = new ArrayList<>();
            loadEntityAvailableTemplateList(getCurrent());
            RequestContext.getCurrentInstance().execute(onSuccess);
        } else {
            SessionUtility.addErrorMessage("Traveler binder property type not found ",
                    " Please contact your admin to add a property type with traveler binder handler");
        }

    }

    public boolean isSelectedNewBinderTemplate(Form form) {
        return newBinderTemplateSelection.contains(form);
    }

    public String createBinder() {
        // Validataion before submission of anything... 
        String newBinderTitle = newBinder.getTitle();
        if (newBinderTitle == null || newBinderTitle.equals("")) {
            SessionUtility.addErrorMessage("Error",
                    "Please specify a binder title.");
            return null;
        }

        if (newBinderTemplateSelection.size() == 0) {
            SessionUtility.addErrorMessage("Error",
                    "Please select traveler templates to create travelers for the binder.");
            return null;
        } else {
            for (Form template : newBinderTemplateSelection) {
                if (template.getTravelerInstanceName().equals("")) {
                    SessionUtility.addErrorMessage("Error",
                            "Please specify a title for traveler of template: '" + template.getTitle() + "'.");
                    return null;
                }
            }
        }

        UserInfo currentUser = (UserInfo) SessionUtility.getUser();
        String username = currentUser.getUsername();
        try {
            String device = getDeviceName(getCurrent());
            newBinder = travelerApi.createBinder(newBinderTitle,
                    newBinder.getDescription(), username);

            String[] travelerIds = new String[newBinderTemplateSelection.size()];

            for (int i = 0; i < newBinderTemplateSelection.size(); i++) {
                Form selectedTemplate = newBinderTemplateSelection.get(i);
                String templateId = selectedTemplate.getId();
                String travelerName = selectedTemplate.getTravelerInstanceName();
                Traveler newTraveler = travelerApi.createTraveler(templateId, username, travelerName, device);
                travelerIds[i] = newTraveler.getId();
            }

            String newBinderId = newBinder.getId();
            travelerApi.addWorkToBinder(newBinderId, travelerIds, username);

            propertyValue.setValue(newBinderId);
            this.savePropertyList();

            return SessionUtility.getRedirectToCurrentView();

        } catch (CdbException ex) {
            SessionUtility.addErrorMessage("Error", ex.getMessage());
        }
        return null;
    }

    public void destroyTravelerTemplateFromCurrent(Form travelerTemplate) {
        List<PropertyValue> propertyValueInternalList = getCurrent().getPropertyValueInternalList();
        for (PropertyValue propertyValue : propertyValueInternalList) {
            PropertyTypeHandler propertyTypeHandler = propertyValue.getPropertyType().getPropertyTypeHandler();
            if (propertyTypeHandler != null) {
                if (propertyTypeHandler.getName().equals(TravelerTemplatePropertyTypeHandler.HANDLER_NAME)) {
                    if (propertyValue.getValue().equals(travelerTemplate.getId())) {
                        setCurrentEditPropertyValue(propertyValue);
                        deleteCurrentEditPropertyValue();
                        templatesForCurrent.remove(travelerTemplate);
                        break;
                    }
                }
            }
        }
    }

    public void addTravelerInstanceToCurrent(String onSuccess) {
        PropertyType travelerInstancePropertyType = getTravelerInstancePropertyType();

        if (travelerInstancePropertyType != null) {
            propertyValue = getItemController().preparePropertyTypeValueAdd(travelerInstancePropertyType);
            loadEntityAvailableTemplateList(getCurrent());
            RequestContext.getCurrentInstance().execute(onSuccess);
        } else {
            SessionUtility.addErrorMessage("Traveler instance property type not found ",
                    " Please contact your admin to add a property type with traveler instance handler");
        }
    }

    public void addTravelerTemplateToCurrent(String onSuccess) {
        PropertyType travelerTemplatePropertyType = getTravelerTemplatePropertyType();

        if (travelerTemplatePropertyType != null) {
            propertyValue = getItemController().preparePropertyTypeValueAdd(travelerTemplatePropertyType);
            RequestContext.getCurrentInstance().execute(onSuccess);
        } else {
            SessionUtility.addErrorMessage("Traveler template property type not found ",
                    " Please contact your admin to add a property type with traveler template handler");
        }
    }

    public String getTravelerTemplateURL(String id) {
        String travelerInstanceUrl = TRAVELER_WEB_APP_URL + TRAVELER_WEB_APP_TEMPLATE_PATH;
        return travelerInstanceUrl.replace("FORM_ID", id);
    }

    public PropertyType getTravelerBinderPropertyType() {
        if (travelerBinderPropertyType == null) {
            String travelerBinderPropertyTypeHandlerName = TravelerBinderPropertyTypeHandler.HANDLER_NAME;
            travelerBinderPropertyType = getFirstPropertyTypeWithHandler(travelerBinderPropertyTypeHandlerName);
        }

        return travelerBinderPropertyType;
    }

    public PropertyType getTravelerInstancePropertyType() {
        if (travelerInstancePropertyType == null) {
            String travelerInstancePropertyTypeHandlerName = TravelerInstancePropertyTypeHandler.HANDLER_NAME;
            travelerInstancePropertyType = getFirstPropertyTypeWithHandler(travelerInstancePropertyTypeHandlerName);
        }
        return travelerInstancePropertyType;
    }

    public PropertyType getTravelerTemplatePropertyType() {
        if (travelerTemplatePropertyType == null) {
            String travelerTemplateHandlerName = TravelerTemplatePropertyTypeHandler.HANDLER_NAME;
            travelerTemplatePropertyType = getFirstPropertyTypeWithHandler(travelerTemplateHandlerName);
        }
        return travelerTemplatePropertyType;
    }

    public PropertyType getFirstPropertyTypeWithHandler(String propertyTypeHandlerName) {
        List<PropertyType> availableItems = PropertyTypeController.getInstance().getAvailableItems();
        for (PropertyType propertyType : availableItems) {
            PropertyTypeHandler propertyTypeHandler = propertyType.getPropertyTypeHandler();
            if (propertyTypeHandler != null) {
                if (propertyTypeHandler.getName().equals(propertyTypeHandlerName)) {
                    return propertyType;
                }
            }
        }
        return null;
    }

    public List<Form> getTemplatesForCurrent() {
        if (templatesForCurrent == null) {
            templatesForCurrent = new ArrayList<>();
            loadPropertyTravelerTemplateList(getCurrent().getPropertyValueInternalList(), templatesForCurrent);
        }
        return templatesForCurrent;
    }

    public List<BinderTraveler> getTravelersForCurrent() {
        if (travelersForCurrent == null) {
            travelersForCurrent = new ArrayList<>();
            List<Traveler> travelerList = new ArrayList<>();
            loadPropertyTravelerInstanceList(getCurrent().getPropertyValueInternalList(), travelerList);

            List<Binder> binderList = new ArrayList<>();
            loadPropertyTravelerBinderList(getCurrent().getPropertyValueInternalList(), binderList);

            travelersForCurrent.addAll(travelerList);
            travelersForCurrent.addAll(binderList);
        }
        return travelersForCurrent;
    }

    public void loadTravelerListForBinder(Binder binder) {        
        List<Traveler> travelerList = binder.getTravelerList();
        if (travelerList == null) {
            travelerList = new ArrayList<>();
            for (BinderWorksReference work : binder.getWorks()) {
                String travelerId = work.getId();
                addTravelerFromTravelerId(travelerId, travelerList);
            }
            binder.setTravelerList(travelerList);
        }
    }

    public String getFormName(BinderTraveler binderTraveler) {
        if (binderTraveler.getFormName() == null) {
            if (binderTraveler instanceof Traveler) {
                Traveler traveler = (Traveler) binderTraveler;
                try {
                    Form form = travelerApi.getForm(traveler.getReferenceForm());
                    traveler.setFormName(form.getTitle());
                } catch (Exception ex) {
                    traveler.setFormName(traveler.getReferenceForm());
                }
            }
        }
        return binderTraveler.getFormName();
    }

    private SelectItem getNewSelectItem(String label, Boolean disabled) {
        return new SelectItem(label, label, label, disabled);
    }

    /**
     * Checks for property value and displays proper error message when null.
     * Used to determine if a function should execute.
     *
     * @return boolean that determines if function should execute.
     */
    private boolean checkPropertyValue() {
        if (propertyValue != null) {
            return true;
        } else {
            SessionUtility.addErrorMessage("Error", "Property value object is missing");
            logger.error("Property value object is missing");
            return false;
        }
    }

    /**
     * Checks for template and displays error message when null. Used to
     * determine if a function should execute.
     *
     * @param template Template variable that needs to be checked.
     * @return boolean that determines if function should execute.
     */
    private boolean checkSelectedTemplate(Form template) {
        if (template != null) {
            if (template.getId() != null) {
                for (Form ittrTemplate : getTemplatesForCurrent()) {
                    if (template.getId().equals(ittrTemplate.getId())) {
                        SessionUtility.addWarningMessage("Template has already been added to current item"
                                , "Selected traveler template is already a part of current item.");
                        return false; 
                    }
                }
            }
            return true;
        } else {
            SessionUtility.addWarningMessage("No Template Selected", "Traveler Template is not selected");
            logger.error("Traveler Template is not selected");
            return false;
        }
    }

    /**
     * Creates a traveler template or Form. Uses traveler API to create a
     * traveler template or Form. Assigns the newly created id with property and
     * saves.
     *
     * @param entityController controller for the entity currently being edited
     * by the user.
     * @param onSuccessCommand Remote command to execute only on successful
     * completion.
     */
    public void createTravelerTemplate(ICdbDomainEntityController entityController, String onSuccessCommand) {
        if (checkPropertyValue()) {

            if (!travelerTemplateTitle.equals("") && travelerTemplateTitle != null) {
                Form form;
                try {
                    UserInfo currentUser = (UserInfo) SessionUtility.getUser();
                    form = travelerApi.createForm(travelerTemplateTitle, currentUser.getUsername(), "");
                    SessionUtility.addInfoMessage("Template Created", "Traveler Template '" + form.getId() + "' has been created");
                    propertyValue.setValue(form.getId());
                    entityController.savePropertyList();
                    RequestContext.getCurrentInstance().execute(onSuccessCommand);
                } catch (CdbException ex) {
                    SessionUtility.addErrorMessage("Error", ex.getMessage());
                    logger.error(ex);
                }
            } else {
                SessionUtility.addWarningMessage("Template Title Missing", "Please specify a traveler template title.");
            }
        }
    }

    /**
     * Links to an existing traveler template. Uses an traveler template or Form
     * fetched from traveler web service to assign id to property value and
     * saves.
     *
     * @param entityController controller for the entity currently being edited
     * by the user.
     * @param onSuccessCommand Remote command to execute only on successful
     * completion.
     */
    public void linkTravelerTemplate(String onSuccessCommand) {
        if (checkPropertyValue()) {
            if (checkSelectedTemplate(selectedTemplate)) {
                propertyValue.setValue(selectedTemplate.getId());
                savePropertyList();
                RequestContext.getCurrentInstance().execute(onSuccessCommand);
            }
        }
    }

    /**
     * Removes the id associated with template property.
     *
     * @param onSuccessCommand Remote command to execute only on successful
     * completion.
     */
    public void unlinkTravelerTemplate(String onSuccessCommand) {
        if (checkPropertyValue()) {
            travelerTemplateTitle = "";
            propertyValue.setValue("");
            RequestContext.getCurrentInstance().execute(onSuccessCommand);
        }
    }

    /**
     * Generates a (traveler template)/form view URL using id and CDB
     * properties.
     *
     * @param formId traveler template or form id to generate URL for.
     * @return generated URL to traveler web application.
     */
    public String getTravelerTemplateUrl(String formId) {
        String travelerTemplateUrl = TRAVELER_WEB_APP_URL + TRAVELER_WEB_APP_TEMPLATE_PATH;
        return travelerTemplateUrl.replace("FORM_ID", formId);
    }

    /**
     * Generates a (traveler instance)/traveler view URL using id and CDB
     * properties. Uses currentTravlerInstance variable and its ID along with
     * CDB properties to generate URL.
     *
     * @return generated URL to traveler web application.
     */
    public String getBinderTravelerUrl(BinderTraveler binderTraveler) {
        String binderTravelerInstanceUrl = TRAVELER_WEB_APP_URL;
        String id = binderTraveler.getId(); 
        if (binderTraveler instanceof Traveler) {
            binderTravelerInstanceUrl += TRAVELER_WEB_APP_TRAVELER_PATH;
            return binderTravelerInstanceUrl.replace("TRAVELER_ID", id);
        } else {
            binderTravelerInstanceUrl += TRAVELER_WEB_APP_BINDER_PATH; 
            return binderTravelerInstanceUrl.replace("BINDER_ID", id);
        }                         
    }

    /**
     * Generates a (traveler instance)/traveler configuration URL using id and
     * CDB properties. Uses currentTravlerInstance variable and its ID along
     * with CDB properties to generate URL.
     *
     * @return generated configuration URL to traveler web application.
     */
    public String getCurrentTravelerInstanceConfigUrl() {
        if (currentTravelerInstance != null) {
            String travelerInstanceUrl = TRAVELER_WEB_APP_URL + TRAVELER_WEB_APP_TRAVELER_CONFIG_PATH;
            return travelerInstanceUrl.replace("TRAVELER_ID", currentTravelerInstance.getId());
        }
        return "";
    }

    /**
     * Gets a string representation of status stored in traveler db. Uses
     * currentTravelerInstance variable and its status to generate string
     * status.
     *
     * @return string representation of status.
     */
    public String getCurrentTravelerStatus() {
        if (currentTravelerInstance != null) {
            return (String) statusNames.get(currentTravelerInstance.getStatus());
        }
        return "";
    }

    public List<String> getCurrentTravelerStatusOptions() {
        if (currentTravelerInstance != null) {
            return (List<String>) statusOptions.get(currentTravelerInstance.getStatus());
        }
        return null;
    }

    public Double getStatusKey(String statusName) {
        Iterator statusNamesIterator = statusNames.entrySet().iterator();

        while (statusNamesIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) statusNamesIterator.next();
            if (entry.getValue().equals(statusName)) {
                return (Double) entry.getKey();
            }
        }

        return -1.0;
    }

    /**
     * Determines if the current owner owns the traveler instance. Uses
     * currentTravlerInstance to determine if current user owns it. Currently,
     * the traveler could only be configured by user who owns it.
     *
     * @return boolean, if user can configure the traveler instance.
     */
    public boolean getCurrentTravelerConfigPermission() {
        if (currentTravelerInstance != null) {
            String travelerUser = currentTravelerInstance.getCreatedBy();
            UserInfo currentUser = (UserInfo) SessionUtility.getUser();
            if (currentUser != null && travelerUser.equals(currentUser.getUsername())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a (traveler instance)/traveler title. Uses the traveler web API to
     * get a title. Default text is returned if fetch fails. returned value is
     * used for a link that user may click to get to traveler.
     *
     * @param propertyValue value of propertyValue holds an id to a traveler
     * instance.
     * @return String that will be displayed to user on link to traveler
     * instance.
     */
    public String getTravelerInstanceTitle(PropertyValue propertyValue) {
        if (propertyValue.getDisplayValue() != null) {
            return propertyValue.getDisplayValue();
        }
        try {
            Traveler traveler = travelerApi.getTraveler(propertyValue.getValue());
            propertyValue.setDisplayValue(traveler.getTitle());
            return traveler.getTitle();
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
        }
        return "Go to Traveler Instance";

    }

    /**
     * Determines all entities that need to have (traveler templates)/forms
     * loaded
     *
     * @param entityController controller for the entity currently being edited
     * by the user.
     */
    public void loadEntityAvailableTemplateList(CdbDomainEntity domainEntity) {
        availableTemplates = new ArrayList<>();
        if (domainEntity instanceof Item) {
            Item item = (Item) domainEntity;
            loadPropertyTravelerTemplateList(item.getPropertyValueInternalList(), availableTemplates);
            if (item.getDerivedFromItem() != null) {
                loadPropertyTravelerTemplateList(item.getDerivedFromItem().getPropertyValueInternalList(), availableTemplates);
            }
        } else if (domainEntity instanceof ItemElement) {
            ItemElement itemElement = (ItemElement) domainEntity;
            loadPropertyTravelerTemplateList(itemElement.getPropertyValueList(), availableTemplates);
            Item parentItem = itemElement.getParentItem();
            Item containedItem = itemElement.getContainedItem();
            loadPropertyTravelerTemplateList(parentItem.getPropertyValueInternalList(), availableTemplates);
            loadPropertyTravelerTemplateList(containedItem.getPropertyValueInternalList(), availableTemplates);
        }
    }

    /**
     * Check all property values for templates and call function that add each
     * one.
     *
     * @param propertyValues List of properties for a specific entity.
     * @param formList List of (traveler templates)/forms that will be displayed
     * to the user.
     */
    private void loadPropertyTravelerTemplateList(List<PropertyValue> propertyValues, List<Form> formList) {
        for (PropertyValue curPropertyValue : propertyValues) {
            // Check that they use the traveler template handler. 
            if (curPropertyValue.getPropertyType().getPropertyTypeHandler() != null) {
                if (curPropertyValue.getPropertyType().getPropertyTypeHandler().getName().equals(TravelerTemplatePropertyTypeHandler.HANDLER_NAME)) {
                    addFormFromPropertyValue(curPropertyValue.getValue(), formList);
                }
            }
        }
    }

    /**
     * Load the form from web service using its ID and add to list.
     *
     * @param formId id of the (traveler template)/form to fetch from web
     * service
     * @param formList List of (traveler templates)/forms that will be displayed
     * to the user.
     */
    private void addFormFromPropertyValue(String formId, List<Form> formList) {
        if (formId == null || formId.equals("")) {
            return;
        }
        try {
            formList.add(travelerApi.getForm(formId));
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getMessage());
        }
    }

    /**
     * Check all property values for traveler instances and call function that
     * add each one.
     *
     * @param propertyValues List of properties for a specific entity.
     * @param travelerList List of (traveler templates)/forms that will be
     * displayed to the user.
     */
    private void loadPropertyTravelerInstanceList(List<PropertyValue> propertyValues, List<Traveler> travelerList) {
        for (PropertyValue curPropertyValue : propertyValues) {
            // Check that they use the traveler template handler. 
            if (curPropertyValue.getPropertyType().getPropertyTypeHandler() != null) {
                if (curPropertyValue.getPropertyType().getPropertyTypeHandler().getName().equals(TravelerInstancePropertyTypeHandler.HANDLER_NAME)) {
                    addTravelerFromTravelerId(curPropertyValue.getValue(), travelerList);
                }
            }
        }
    }

    private void loadPropertyTravelerBinderList(List<PropertyValue> propertyValues, List<Binder> binderList) {
        for (PropertyValue curPropertyValue : propertyValues) {
            if (curPropertyValue.getPropertyType().getPropertyTypeHandler() != null) {
                if (curPropertyValue.getPropertyType().getPropertyTypeHandler().getName().equals(TravelerBinderPropertyTypeHandler.HANDLER_NAME)) {
                    String binderId = curPropertyValue.getValue();
                    try {
                        Binder binder = travelerApi.getBinder(binderId);
                        binderList.add(binder);
                    } catch (Exception ex) {
                        SessionUtility.addErrorMessage("Error", ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Load the traveler from web service using its ID and add to list.
     *
     * @param travelerId id of the (traveler instance)/traveler to fetch from
     * web service
     * @param travelerList List of (traveler instances)/travelers that will be
     * displayed to the user.
     */
    private void addTravelerFromTravelerId(String travelerId, List<Traveler> travelerList) {
        if (travelerId == null || travelerId.equals("")) {
            return;
        }
        try {
            travelerList.add(travelerApi.getTraveler(travelerId));
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getMessage());
        }
    }

    /**
     * Generates a name to be used in traveler application. Allows the traveler
     * application to request information from CDB.
     *
     * @param deviceObject Item or ItemElement that will get traveler instance
     * property.
     * @return device name for traveler application.
     * @throws CdbException when non valid object was submitted for device name.
     */
    private String getDeviceName(Object deviceObject) throws CdbException {
        if (deviceObject instanceof Item) {
            return "item:" + ((Item) deviceObject).getId();
        } else if (deviceObject instanceof ItemElement) {
            return "itemElement:" + ((ItemElement) deviceObject).getId();
        } else {
            throw new CdbException("Device for traveler is not of valid CDB entity");
        }
    }

    /**
     * Create a traveler instance based on the currently selected template and
     * entered title.
     *
     * @param entityController controller for the entity currently being edited
     * by the user.
     * @param onSuccessCommand Remote command to execute only on successful
     * completion
     */
    public void createTravelerInstance(ICdbDomainEntityController entityController, String onSuccessCommand) {
        if (checkPropertyValue()) {
            if (checkSelectedTemplate(selectedTravelerInstanceTemplate)) {
                if (!travelerInstanceTitle.equals("") || travelerInstanceTitle != null) {
                    try {
                        String device = getDeviceName(entityController.getCurrent());
                        UserInfo currentUser = (UserInfo) SessionUtility.getUser();
                        Traveler travelerInstance = travelerApi.createTraveler(
                                selectedTravelerInstanceTemplate.getId(),
                                currentUser.getUsername(),
                                travelerInstanceTitle,
                                device);
                        setCurrentTravelerInstance(travelerInstance);

                        SessionUtility.addInfoMessage(
                                "Traveler Instance Created",
                                "Traveler Instance '" + travelerInstance.getId() + "' has been created");

                        propertyValue.setValue(travelerInstance.getId());
                        entityController.savePropertyList();
                        RequestContext.getCurrentInstance().execute(onSuccessCommand);

                    } catch (CdbException ex) {
                        logger.error(ex);
                        SessionUtility.addErrorMessage("Error", ex.getMessage());
                    }
                } else {
                    SessionUtility.addWarningMessage("No title", "Please enter a title for a traveler instance");
                    logger.error("No title for traveler instance was provided");
                }

            }
        }
    }

    /**
     * Load information about the traveler instance property currently loaded.
     * Add progress information based on its information which will be displayed
     * to user.
     *
     * @param onSuccessCommand Remote command to execute only on successful
     * completion
     */
    public void reloadCurrentTravelerInstance(String onSuccessCommand) {
        try {
            Traveler travelerInstance = travelerApi.getTraveler(currentTravelerInstance.getId());
            setCurrentTravelerInstance(travelerInstance);
            //Show the GUI since all execution was successful. 
            RequestContext.getCurrentInstance().execute(onSuccessCommand);
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
        }
    }

    /**
     * Get a list of all (traveler templates)/forms for user to view.
     *
     * @param onSuccessCommand Remote command to execute only on successful
     * completion
     */
    public void loadTravelerTemplates(String onSuccessCommand) {
        try {
            travelerTemplates = travelerApi.getForms();
            RequestContext.getCurrentInstance().execute(onSuccessCommand);
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getMessage());
        }

    }

    /**
     * Load information about the traveler template property currently loaded.
     */
    public void loadTravelerTemplateInformation() {
        if (propertyValue != null) {
            if (!propertyValue.getValue().equals("") || propertyValue.getDisplayValue() == null) {
                try {
                    TravelerTemplatePropertyTypeHandler travelerTemplatePropertyTypeHandler
                            = (TravelerTemplatePropertyTypeHandler) PropertyTypeHandlerFactory.getHandler(propertyValue);
                    travelerTemplatePropertyTypeHandler.setDisplayValue(propertyValue);
                    travelerTemplateTitle = propertyValue.getDisplayValue();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }

    }

    /**
     * Function gets called when user selects traveler template to set the
     * default traveler instance title.
     */
    public void updateTravelerInstanceTitleInputText() {
        if (selectedTravelerInstanceTemplate != null) {
            if (travelerInstanceTitleInputText != null) {
                travelerInstanceTitleInputText.setValue(selectedTravelerInstanceTemplate.getTitle());
            }
        }
    }

    public void updateTravelerInstanceConfiguration(String onSuccessCommand) {
        String travelerId = currentTravelerInstance.getId();
        String travelerTitle;
        if (currentTravelerEditTitle == null) {
            travelerTitle = getCurrentTravelerInstanceTitle();
        } else {
            travelerTitle = currentTravelerEditTitle;
        }
        String travelerDescription;
        if (currentTravelerEditDescription == null) {
            travelerDescription = getCurrentTravelerInstanceDescription();
        } else {
            travelerDescription = currentTravelerEditDescription;
        }

        Double status = getStatusKey(currentTravelerSelectedStatus);

        UserInfo currentUser = (UserInfo) SessionUtility.getUser();
        String userName = currentUser.getUsername();
        try {
            Traveler travelerInstance = travelerApi.updateTraveler(travelerId, userName, travelerTitle, travelerDescription, currentTravelerDeadline, status);
            setCurrentTravelerInstance(travelerInstance);
            RequestContext.getCurrentInstance().execute(onSuccessCommand);
        } catch (CdbException ex) {
            logger.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getErrorMessage());
        }
    }

    public void resetUpdateTravelerInstanceConfiguration() {
        currentTravelerSelectedStatus = null;
        currentTravelerDeadline = null;
        currentTravelerEditTitle = null;
        currentTravelerEditDescription = null;
    }

    public void setPropertyValue(PropertyValue propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getTravelerTemplateTitle() {
        return travelerTemplateTitle;
    }

    public void setTravelerTemplateTitle(String travelerTemplateTitle) {
        this.travelerTemplateTitle = travelerTemplateTitle;
    }

    public List<Form> getTravelerTemplates() {
        if (travelerTemplates == null) {
            return null;
        }
        return travelerTemplates.getForms();
    }

    public void setSelectedTemplate(Form selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public Form getSelectedTemplate() {
        return selectedTemplate;
    }

    public List<Form> getAvailableTemplates() {
        return availableTemplates;
    }

    public PropertyValue getPropertyValue() {
        return propertyValue;
    }

    public Binder getNewBinder() {
        return newBinder;
    }

    public void setNewBinder(Binder newBinder) {
        this.newBinder = newBinder;
    }

    public List<Form> getNewBinderTemplateSelection() {
        return newBinderTemplateSelection;
    }

    public void setNewBinderTemplateSelection(List<Form> newBinderTemplateSelection) {
        this.newBinderTemplateSelection = newBinderTemplateSelection;
    }

    public void setTravelerInstanceTitle(String travelerInstanceTitle) {
        this.travelerInstanceTitle = travelerInstanceTitle;
    }

    public String getTravelerInstanceTitle() {
        return travelerInstanceTitle;
    }

    public void setSelectedTravelerInstanceTemplate(Form selectedTravelerInstanceTemplate) {
        this.selectedTravelerInstanceTemplate = selectedTravelerInstanceTemplate;
    }

    public Form getSelectedTravelerInstanceTemplate() {
        return selectedTravelerInstanceTemplate;
    }

    public void setTravelerInstanceTitleInputText(HtmlInputText travelerInstanceTitleInputText) {
        this.travelerInstanceTitleInputText = travelerInstanceTitleInputText;
    }

    public HtmlInputText getTravelerInstanceTitleInputText() {
        return travelerInstanceTitleInputText;
    }

    public Traveler getCurrentTravelerInstance() {
        return currentTravelerInstance;
    }

    public String getCurrentTravelerInstanceTitle() {
        if (currentTravelerInstance != null && currentTravelerInstance.getTitle() != null) {
            return currentTravelerInstance.getTitle();
        }

        return "";
    }

    public String getCurrentTravelerInstanceDescription() {
        if (currentTravelerInstance != null && currentTravelerInstance.getDescription() != null) {
            return currentTravelerInstance.getDescription();
        }

        return "";
    }

    public void setCurrentTravelerInstance(Traveler currentTravelerInstance) {
        this.currentTravelerInstance = currentTravelerInstance;
        resetUpdateTravelerInstanceConfiguration();
    }

    public String getTRAVELER_WEB_APP_URL() {
        return TRAVELER_WEB_APP_URL;
    }

    public Date getCurrentTravelerDeadline() {
        if (currentTravelerInstance.getDeadline() != null) {
            String deadline = currentTravelerInstance.getDeadline();
            String dateString = deadline.split("T")[0];
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException ex) {
                logger.error(ex);
            }
        }

        return null;
    }

    public void setCurrentTravelerDeadline(Date currentTravelerDeadline) {
        this.currentTravelerDeadline = currentTravelerDeadline;
    }

    /**
     * Makes the default selection in the select one button widget.
     *
     * @return status of current traveler.
     */
    public String getCurrentTravelerSelectedStatus() {
        return getCurrentTravelerStatus();
    }

    public void setCurrentTravelerSelectedStatus(String currentTravelerSelectedStatus) {
        this.currentTravelerSelectedStatus = currentTravelerSelectedStatus;
    }

    public String getCurrentTravelerEditTitle() {
        return getCurrentTravelerInstanceTitle();
    }

    public void setCurrentTravelerEditTitle(String currentTravelerEditTitle) {
        this.currentTravelerEditTitle = currentTravelerEditTitle;
    }

    public String getCurrentTravelerEditDescription() {
        return getCurrentTravelerInstanceDescription();
    }

    public void setCurrentTravelerEditDescription(String currentTravelerEditDescription) {
        this.currentTravelerEditDescription = currentTravelerEditDescription;
    }
}