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
public class ImportHelperManagedNameValidateSignals extends ImportHelperBase {

    public class SignalNameInfo extends ImportHelperBase.RowModel {

        private String name = "";

        public SignalNameInfo(String n, boolean v, String vs) {
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

    protected static String nameHeader = "Signal Name";
    protected static String nameProperty = "name";

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
            NameParseInfo info = ItemDomainManagedNameController.getInstance().parseSignalName(name);
            isValid = info.valid;
            validString = info.message;
        }

        SignalNameInfo info = new SignalNameInfo(name, isValid, validString);
        rows.add(info);
    }

}
