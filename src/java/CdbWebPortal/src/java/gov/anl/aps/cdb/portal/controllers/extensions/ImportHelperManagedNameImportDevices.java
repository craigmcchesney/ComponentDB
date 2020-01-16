/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.extensions;

import gov.anl.aps.cdb.portal.controllers.ImportHelperBase;
import gov.anl.aps.cdb.portal.controllers.ItemDomainManagedNameController;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author craig
 */
public class ImportHelperManagedNameImportDevices extends ImportHelperBase {

    public class DeviceNameInfo extends ImportHelperBase.RowModel {

        private String name = "";
        private String description = "";

        public DeviceNameInfo(String n, String d, boolean v, String vs) {
            super(v, vs);
            name = n;
            description = d;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    protected static String nameHeader = "Device Type";
    protected static String nameProperty = "name";

    protected static String descriptionHeader = "Description";
    protected static String descriptionProperty = "description";

    protected static String completionUrlValue = "/views/itemDomainManagedName/list?faces-redirect=true";
    
    @Override
    protected String getCompletionUrlValue() {
        return completionUrlValue;
    }
    
    @Override
    protected boolean isValidationOnly() {
        return false;
    }
    
    @Override
    protected void createColumnModels_() {
        columns.add(new ImportHelperBase.ColumnModel(nameHeader, nameProperty));
        columns.add(new ImportHelperBase.ColumnModel(descriptionHeader, descriptionProperty));
    }

    @Override
    public int getDataStartRow() {
        return 1;
    }

    @Override
    public void parseRow(Row row) {

        String name = "";
        String description = "";
        
        boolean isValid = true;
        String validString = "";

        Cell cell;

        cell = row.getCell(0);
        if (cell == null) {
            name = "";
            isValid = false;
            validString = "unspecified name";
        } else if (cell.getCellType() != CellType.STRING) {
            name = "";
            isValid = false;
            validString = "name is not a string";
        } else {
            name = cell.getStringCellValue();
            boolean exists = ItemDomainManagedNameController.getInstance().checkDeviceTypeExists(name);
            if (exists) {
                isValid = false;
                validString = "device type already exists";
            }
        }

        cell = row.getCell(1);
        if (cell == null) {
            description = "";
        } else if (cell.getCellType() != CellType.STRING) {
            description = "";
            isValid = false;
            validString = "description is not a string";
        } else {
            description = cell.getStringCellValue();
        }

        DeviceNameInfo info = new DeviceNameInfo(name, description, isValid, validString);
        rows.add(info);
    }

}
