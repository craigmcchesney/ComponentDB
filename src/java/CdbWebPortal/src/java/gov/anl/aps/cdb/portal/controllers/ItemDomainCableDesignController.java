/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.constants.ItemCoreMetadataFieldType;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.extensions.BundleWizard;
import gov.anl.aps.cdb.portal.controllers.extensions.CableWizard;
import gov.anl.aps.cdb.portal.controllers.extensions.CircuitWizard;
import gov.anl.aps.cdb.portal.import_export.import_.helpers.ImportHelperCableDesign;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainCableDesignSettings;
import gov.anl.aps.cdb.portal.controllers.utilities.ItemDomainCableDesignControllerUtility;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainCableDesignFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Connector;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemCategory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemConnector;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCableCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCableDesign;
import static gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCableDesign.CABLE_DESIGN_INTERNAL_PROPERTY_TYPE;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.ItemProject;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import gov.anl.aps.cdb.portal.view.objects.CableDesignConnectionListObject;
import gov.anl.aps.cdb.portal.view.objects.DomainImportExportInfo;
import gov.anl.aps.cdb.portal.view.objects.ImportExportFormatInfo;
import gov.anl.aps.cdb.portal.view.objects.ItemCoreMetadataPropertyInfo;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author cmcchesney
 */
@Named(ItemDomainCableDesignController.CONTROLLER_NAMED)
@SessionScoped
public class ItemDomainCableDesignController extends ItemController<ItemDomainCableDesignControllerUtility, ItemDomainCableDesign, ItemDomainCableDesignFacade, ItemDomainCableDesignSettings> {

    @Override
    protected ItemDomainCableDesignControllerUtility createControllerUtilityInstance() {
        return new ItemDomainCableDesignControllerUtility(); 
    }

    public class ConnectionDialog {

        private Boolean disableButtonSave = true;
        private Item origMdItem = null;
        private TreeNode mdTree = null;
        private TreeNode selectedMdItem = null;
        private ItemConnector origMdConnector;
        private ItemConnector origCableConnector;
        private ItemConnector selectedCableConnector;
        private List<ItemConnector> availableCableConnectors;
        private Integer relationshipId;
        
        public Boolean getDisableButtonSave() {
            return disableButtonSave;
        }

        public void setDisableButtonSave(Boolean disableButtonSave) {
            this.disableButtonSave = disableButtonSave;
        }

        public Item getOrigMdItem() {
            return origMdItem;
        }

        public void setOrigMdItem(Item origMdItem) {
            this.origMdItem = origMdItem;
        }

        public String getItemEndpointString() {
            if (origMdItem == null) {
                return "";
            } else {
                return origMdItem.getName();
            }
        }

        public Integer getRelationshipId() {
            return relationshipId;
        }

        public void setRelationshipId(Integer relationshipId) {
            this.relationshipId = relationshipId;
        }

        public TreeNode getMdTree() {
            if (mdTree == null) {
                ItemDomainMachineDesignController controller = ItemDomainMachineDesignController.getInstance();
                mdTree = controller.loadMachineDesignRootTreeNode(false, false, true);
            }
            return mdTree;
        }

        public void setMdTree(TreeNode mdTree) {
            this.mdTree = mdTree;
        }

        public TreeNode getSelectedMdItem() {
            return selectedMdItem;
        }

        public void setSelectedMdItem(TreeNode selectedMdItem) {
            this.selectedMdItem = selectedMdItem;
        }

        public ItemConnector getOrigMdConnector() {
            return origMdConnector;
        }

        public void setOrigMdConnector(ItemConnector origMdConnector) {
            this.origMdConnector = origMdConnector;
        }

        public ItemConnector getOrigCableConnector() {
            return origCableConnector;
        }

        public void setOrigCableConnector(ItemConnector origCableConnector) {
            this.origCableConnector = origCableConnector;
        }

        public ItemConnector getSelectedCableConnector() {
            return selectedCableConnector;
        }

        public void setSelectedCableConnector(ItemConnector selectedCableConnector) {
            this.selectedCableConnector = selectedCableConnector;
        }

        public List<ItemConnector> getAvailableCableConnectors() {
            return availableCableConnectors;
        }

        public void setAvailableCableConnectors(List<ItemConnector> availableCableConnectors) {
            this.availableCableConnectors = availableCableConnectors;
        }

        /**
         * Determines whether endpoint changed, and call update if it did to
         * save the change.
         */
        public String save(String remoteCommandSuccess) {

            Item selectedMdItem = null;
            ItemConnector selectedMdConnector = null;
            if (this.selectedMdItem.getType().equals("Connector")) {
                selectedMdConnector = ((ItemElement) (this.selectedMdItem.getData())).getMdConnector();
                selectedMdItem = selectedMdConnector.getItem();
            } else {
                selectedMdItem = ((ItemElement) (this.selectedMdItem.getData())).getContainedItem();
            }

            if (getCurrent().updateConnection(getRelationshipId(),
                    selectedMdItem,
                    selectedMdConnector,
                    getSelectedCableConnector())) {

                String updateResult = update();

                // An error occured, reload the page with correct information. 
                if (updateResult == null) {
                    reloadCurrent();
                    return view();
                }

                refreshConnectionListForCurrent();

                SessionUtility.executeRemoteCommand(remoteCommandSuccess);
            }
            return null;
        }

        public void cancel() {
        }
        
        public void selectListenerConnector(SelectEvent event){
            setEnablement();
        }

        /**
         * Handles select events generated by the machine design tree table
         * component. Must call client side remoteCommand to update button state
         * from oncomplete attribute for this event tag.
         */
        public void selectListenerEndpoint(NodeSelectEvent event) {
            setEnablement();
        }

        public void actionListenerSaveSuccess() {
        }

        /**
         * Resets the dialog components when closing.
         */
        public void reset() {            
            setOrigMdItem(null);
            setMdTree(null);
            setSelectedMdItem(null);
            setOrigMdConnector(null);
            setOrigCableConnector(null);
            setSelectedCableConnector(null);
            setAvailableCableConnectors(null);
            setRelationshipId(null);
            setEnablement();            
        }

        /**
         * Enables save button when a valid selection, different from the
         * original endpoint, is made.
         */
        private void setEnablement() {

            if (selectedMdItem == null) {

                setDisableButtonSave((Boolean) true);

            } else {
                
                Item selectedMdItem = null;
                ItemConnector selectedMdConnector = null;
                if (this.selectedMdItem.getType().equals("Connector")) {
                    selectedMdConnector = ((ItemElement) (this.selectedMdItem.getData())).getMdConnector();
                    selectedMdItem = selectedMdConnector.getItem();
                } else {
                    selectedMdItem = ((ItemElement) (this.selectedMdItem.getData())).getContainedItem();
                }
                
                ItemConnector selectedCableConnector = getSelectedCableConnector();

                if ((selectedMdItem.equals(getOrigMdItem())) 
                        && (selectedMdConnector == getOrigMdConnector())
                        && (selectedCableConnector == getOrigCableConnector())) {
                    
                    setDisableButtonSave((Boolean) true);
                    
                } else {
                    setDisableButtonSave((Boolean) false);
                }
            }
        }

        private void expandTreeAndSelectNode() {

            TreeNode machineDesignTreeRootTreeNode = getMdTree();

            if (selectedMdItem != null) {
                selectedMdItem.setSelected(false);
                selectedMdItem = null;
            }

            TreeNode selectedNode = ItemDomainMachineDesignController.expandToSpecificMachineDesignItem(
                    machineDesignTreeRootTreeNode, 
                    (ItemDomainMachineDesign)getOrigMdItem());
            
            selectedMdItem = selectedNode;
            
            if ((selectedNode != null) && (getOrigMdConnector() != null)) {
                selectedNode.setSelected(false);
                selectedNode.setExpanded(true);
                List<TreeNode> children = selectedNode.getChildren();
                for (TreeNode child : children) {
                    if (child.getType().equals("Connector")) {
                        ItemConnector connectorChild = 
                                ((ItemElement) (child.getData())).getMdConnector();
                        if (connectorChild.equals(getOrigMdConnector())) {
                            child.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }

    }

    public class CatalogDialog {

        private Boolean disableButtonSave = true;
        private Item itemCatalog = null;
        private Item selectionModelCatalog = null;
        
        public Boolean getDisableButtonSave() {
            return disableButtonSave;
        }

        public void setDisableButtonSave(Boolean disableButtonSave) {
            this.disableButtonSave = disableButtonSave;
        }

        public Item getItemCatalog() {
            return itemCatalog;
        }

        public void setItemCatalog(Item itemCatalog) {
            this.itemCatalog = itemCatalog;
            selectCatalogItem((ItemDomainCableCatalog) itemCatalog);
        }

        public String getItemCatalogString() {
            if (itemCatalog == null) {
                return "";
            } else {
                return itemCatalog.getName();
            }
        }

        public Item getSelectionModelCatalog() {
            return selectionModelCatalog;
        }

        public void setSelectionModelCatalog(Item selectionModelCatalog) {
            this.selectionModelCatalog = selectionModelCatalog;
        }

        /**
         * Determines whether catalog item changed, and calls update if it did.
         */
        public String save(String remoteCommandSuccess) {

            // catalog item changed, update cable and save
            if (!selectionModelCatalog.equals(getItemCatalog())) {
                
                getCurrent().setCatalogItem(selectionModelCatalog);

                String updateResult = update();

                // An error occured, reload the page with correct information. 
                if (updateResult == null) {
                    reloadCurrent();
                    return view();
                }

                SessionUtility.executeRemoteCommand(remoteCommandSuccess);
            }
            return null;
        }

        public void cancel() {
        }

        /**
         * Handles select events generated by the data table
         * component. Must call client side remoteCommand to update button state
         * from oncomplete attribute for this event tag.
         */
        public void selectListenerCatalog() {
            setEnablement();
        }

        public void actionListenerSaveSuccess() {
        }

        /**
         * Resets the dialog components when closing.
         */
        public void reset() {
            setSelectionModelCatalog(null);
            setEnablement();
        }

        /**
         * Enables save button when a valid selection, different from the
         * original catalog item, is made.
         */
        private void setEnablement() {

            if (selectionModelCatalog == null) {

                setDisableButtonSave((Boolean) true);

            } else {

                if (selectionModelCatalog.equals(getItemCatalog())) {
                    setDisableButtonSave((Boolean) true);
                } else {
                    setDisableButtonSave((Boolean) false);
                }
            }
        }

        private void selectCatalogItem(ItemDomainCableCatalog item) {
            selectionModelCatalog = item;
        }

    }

    public static final String CONTROLLER_NAMED = "itemDomainCableDesignController";

    @EJB
    ItemDomainCableDesignFacade itemDomainCableDesignFacade;

    private CatalogDialog dialogCatalog = new CatalogDialog();
    private ConnectionDialog dialogConnection = new ConnectionDialog();
    
    private List<CableDesignConnectionListObject> connectionListForCurrent;

    protected ImportHelperCableDesign importHelper = new ImportHelperCableDesign();
    
    public CatalogDialog getDialogCatalog() {
        return dialogCatalog;
    }

    public void setDialogCatalog(CatalogDialog endpoint2Dialog) {
        this.dialogCatalog = dialogCatalog;
    }

    public ConnectionDialog getDialogConnection() {
        return dialogConnection;
    }

    public void setDialogConnection(ConnectionDialog endpoint2Dialog) {
        this.dialogConnection = dialogConnection;
    }

    public static ItemDomainCableDesignController getInstance() {
        return (ItemDomainCableDesignController) SessionUtility.findBean(ItemDomainCableDesignController.CONTROLLER_NAMED);
    }
    
    @Override
    protected ItemCoreMetadataPropertyInfo initializeCoreMetadataPropertyInfo() {
        ItemCoreMetadataPropertyInfo info = new ItemCoreMetadataPropertyInfo("Cable Design Metadata", CABLE_DESIGN_INTERNAL_PROPERTY_TYPE);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY, "Ext Cable Name", "External cable name (e.g., from CAD or routing tool).", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY, "Import Cable ID", "Import cable identifier.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY, "Alt Cable ID", "Alternate (e.g., group-specific) cable identifier.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY, "Legacy QR ID", "Legacy QR identifier, e.g., for cables that have already been assigned a QR code.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_LAYING_KEY, "Laying", "Laying style e.g., S=single-layer, M=multi-layer, T=triangular, B=bundle", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_VOLTAGE_KEY, "Voltage", "Voltage aplication e.g., COM=communication, CTRL=control, IW=instrumentation, LV=low voltage, MV=medium voltage", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_ENDPOINT1_DESC_KEY, "Endpoint1 Desc", "Endpoint details useful for external editing.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_ENDPOINT2_DESC_KEY, "Endpoint2 Desc", "Endpoint details useful for external editing.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_ENDPOINT1_ROUTE_KEY, "Endpoint1 Route", "Routing waypoint for first endpoint.", ItemCoreMetadataFieldType.STRING, "", null);
        info.addField(ItemDomainCableDesign.CABLE_DESIGN_PROPERTY_ENDPOINT2_ROUTE_KEY, "Endpoint2 Route", "Routing waypoint for second endpoint.", ItemCoreMetadataFieldType.STRING, "", null);
        return info;
    }

    /**
     * Creates a cable design object and sets the core variables.
     *
     * @param itemEndpoint1
     * @param itemEndpoint2
     * @param cableName
     * @return
     */
    private ItemDomainCableDesign createCableCommon(Item itemEndpoint1,
            Item itemEndpoint2,
            String cableName,
            List<ItemProject> projectList,
            List<ItemCategory> technicalSystemList) {

        ItemDomainCableDesign newCable = this.createEntityInstance();
        newCable.setName(cableName);
        newCable.setItemProjectList(projectList);
        newCable.setTechnicalSystemList(technicalSystemList);

        // set endpoints
        newCable.setEndpoint1(itemEndpoint1);
        newCable.setEndpoint2(itemEndpoint2);

        return newCable;
    }

    /**
     * Creates cable design connecting the specified endpoints, with
     * unspecified cable catalog type.
     *
     * @param itemEndpoint1
     * @param itemEndpoint2
     * @param cableName
     * @return
     */
    public boolean createCableUnspecified(Item itemEndpoint1,
            Item itemEndpoint2,
            String cableName,
            List<ItemProject> projectList,
            List<ItemCategory> technicalSystemList) {

        ItemDomainCableDesign newCable = this.createCableCommon(itemEndpoint1,
                itemEndpoint2,
                cableName,
                projectList,
                technicalSystemList);

        if (this.create() == null) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Creates cable design of specified cable catalog type
     * connecting the specified endpoints.
     *
     * @param itemEndpoint1
     * @param itemEndpoint2
     * @param cableName
     * @return
     */
    public boolean createCableCatalog(Item itemEndpoint1,
            Item itemEndpoint2,
            String cableName,
            List<ItemProject> projectList,
            List<ItemCategory> technicalSystemList,
            Item itemCableCatalog) {

        ItemDomainCableDesign newCable = this.createCableCommon(itemEndpoint1,
                itemEndpoint2,
                cableName,
                projectList,
                technicalSystemList);

        newCable.setCatalogItem(itemCableCatalog);

        if (this.create() == null) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Prepares endpoint dialog for editing endpoint1.
     */
    public void prepareDialogCatalog() {
        dialogCatalog.reset();
        dialogCatalog.setItemCatalog(getCurrent().getCatalogItem());
    }

    public void prepareEditEndpoint(CableDesignConnectionListObject connection) {
        
        dialogConnection.reset();
        
        dialogConnection.setOrigMdItem(connection.getMdItem());
        dialogConnection.setOrigMdConnector(connection.getMdConnector());
        dialogConnection.setRelationshipId(connection.getCableRelationship().getId());
        dialogConnection.setOrigCableConnector(connection.getItemConnector());
        
        // get list of unmapped connectors, plus connector for this connection if any
        List<ItemConnector> unmappedConnectors = getUnmappedConnectorsForCurrent();
        if (connection.getItemConnector() != null) {
            unmappedConnectors.add(connection.getItemConnector());
            dialogConnection.setSelectedCableConnector(connection.getItemConnector());
        }
        dialogConnection.setAvailableCableConnectors(unmappedConnectors);
        
        // expand MD tree to specified md item and connector
        dialogConnection.expandTreeAndSelectNode();
    }
    
    public Boolean renderEditLinkForConnection(CableDesignConnectionListObject connection) {
        return (connection.getMdItem() != null);
    }
    
    /**
     * Prepares cable wizard.
     */
    public String prepareWizardCable() { 
        CableWizard.getInstance().reset();
        return "/views/itemDomainCableDesign/create?faces-redirect=true";
    }

    /**
     * Prepares import wizard.
     */
    public String prepareWizardCircuit() {
        CircuitWizard.getInstance().reset();
        return "/views/itemDomainCableDesign/createCircuit?faces-redirect=true";
    }

    /**
     * Prepares import wizard.
     */
    public String prepareWizardBundle() {        
        BundleWizard.getInstance().reset();
        return "/views/itemDomainCableDesign/createBundle?faces-redirect=true";
    }

    @Override
    public ItemDomainCableDesign createEntityInstance() {
        ItemDomainCableDesign item = super.createEntityInstance();
        setCurrent(item);
        return item;
    }

    @Override
    protected ItemDomainCableDesign instenciateNewItemDomainEntity() {
        return new ItemDomainCableDesign();
    }

    @Override
    protected ItemDomainCableDesignSettings createNewSettingObject() {
        return new ItemDomainCableDesignSettings(this);
    }

    @Override
    protected ItemDomainCableDesignFacade getEntityDbFacade() {
        return itemDomainCableDesignFacade;
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.cableDesign.getValue();
    }   

    @Override
    public boolean getEntityDisplayItemConnectors() {
        return false;
    }

    @Override
    public boolean getEntityDisplayDerivedFromItem() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemGallery() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemLogs() {
        return true;
    }

    @Override
    public boolean getEntityDisplayItemSources() {
        return false;
    }

    @Override
    public boolean getEntityDisplayItemProperties() {
        return true;
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
        return true;
    }

    @Override
    public boolean getEntityDisplayItemEntityTypes() {
        return false;
    }

    @Override
    public String getItemsDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getStyleName() {
        return "machineDesign";
    }

    @Override
    public String getDefaultDomainDerivedFromDomainName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDefaultDomainDerivedToDomainName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean getEntityDisplayImportButton() {
        return true;
    }

    @Override
    protected DomainImportExportInfo initializeDomainImportInfo() {
        
        List<ImportExportFormatInfo> formatInfo = new ArrayList<>();
        formatInfo.add(new ImportExportFormatInfo("Basic Cable Design Create/Update Format", ImportHelperCableDesign.class));
        
        String completionUrl = "/views/itemDomainCableDesign/list?faces-redirect=true";
        
        return new DomainImportExportInfo(formatInfo, completionUrl);
    }

    @Override
    public boolean getEntityDisplayExportButton() {
        return true;
    }
    
    @Override
    protected DomainImportExportInfo initializeDomainExportInfo() {
        
        List<ImportExportFormatInfo> formatInfo = new ArrayList<>();
        
        formatInfo.add(new ImportExportFormatInfo("Basic Cable Design Create/Update Format", ImportHelperCableDesign.class));
        
        String completionUrl = "/views/itemDomainCableDesign/list?faces-redirect=true";
        
        return new DomainImportExportInfo(formatInfo, completionUrl);
    }
    
    public List<CableDesignConnectionListObject> getConnectionListForCurrent() {
        if (connectionListForCurrent == null) {
            ItemDomainCableDesign item = getCurrent();
            connectionListForCurrent = getConnectionListForItem(item);
        }
        return connectionListForCurrent;
    }
    
    private List<ItemConnector> getUnmappedConnectorsForCurrent() {
        List<ItemConnector> unmappedConnectors = new ArrayList<>();
        for (CableDesignConnectionListObject connection : getConnectionListForCurrent()) {
            if ((connection.getItemConnector() != null) && (connection.getMdItem() == null)) {
                unmappedConnectors.add(connection.getItemConnector());
            }
        }
        return unmappedConnectors;
    }

    public List<CableDesignConnectionListObject> getConnectionListForItem(ItemDomainCableDesign item) {
        this.getControllerUtility().syncConnectors(item);
        return CableDesignConnectionListObject.getConnectionList(item);
    }
    
    private void refreshConnectionListForCurrent() {
        ItemDomainCableDesign item = getCurrent();
        connectionListForCurrent = getConnectionListForItem(item);
    }

    public boolean getDisplayConnectionsList() {
        return getConnectionListForCurrent().size() > 0;
    }
    
    /**
     * Handles add button on cable design view connections list.
     */
    public void prepareAddConnection() {
        System.out.println("TODO: implement prepareAddConnection");
    }

}
