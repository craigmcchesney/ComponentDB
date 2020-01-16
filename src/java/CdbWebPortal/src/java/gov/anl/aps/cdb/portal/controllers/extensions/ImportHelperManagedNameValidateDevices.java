/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.extensions;

import gov.anl.aps.cdb.portal.controllers.ImportHelperBase;
import gov.anl.aps.cdb.portal.controllers.ItemDomainManagedNameController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainManagedNameController.NameParseInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author craig
 */
public class ImportHelperManagedNameValidateDevices extends ImportHelperBase {

    public class DeviceNameInfo extends ImportHelperBase.RowModel {

        private String name = "";

        public DeviceNameInfo(String n, boolean v, String vs) {
            super(v, vs);
            name = n;
        }

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }

    }

    protected static String nameHeader = "Device Name";
    protected static String nameProperty = "name";

    protected static String completionUrlValue = "/views/itemDomainManagedName/list?faces-redirect=true";
    
    @Override
    protected String getCompletionUrlValue() {
        return completionUrlValue;
    }
    
    @Override
    protected void createColumnModels_() {
        columns.add(new ImportHelperBase.ColumnModel(nameHeader, nameProperty));
    }

    @Override
    public int getDataStartRow() {
        return 1;
    }

    @Override
    public void parseRow(Row row) {

        String name = "";
        
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
            NameParseInfo info = ItemDomainManagedNameController.getInstance().parseDeviceName(name);
            isValid = info.valid;
            validString = info.message;
        }

        DeviceNameInfo info = new DeviceNameInfo(name, isValid, validString);
        rows.add(info);
    }

}
