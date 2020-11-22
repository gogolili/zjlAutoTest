package com.baiwang.moirai.utils;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.export.styler.ExcelExportStylerDefaultImpl;
import org.apache.poi.ss.usermodel.*;


public class ExcelExportStatisticSelfStyler extends ExcelExportStylerDefaultImpl {
    private CellStyle numberCellStyle;

    public ExcelExportStatisticSelfStyler(Workbook workbook) {
        super(workbook);
        createNumberCellStyler();
    }

    private void createNumberCellStyler() {
        numberCellStyle = workbook.createCellStyle();
        numberCellStyle.setAlignment(HorizontalAlignment.CENTER);
        numberCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        numberCellStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("0.00"));
        numberCellStyle.setWrapText(true);
    }

    @Override
    public CellStyle getStyles(boolean noneStyler, ExcelExportEntity entity) {
        if (entity != null
                && (entity.getName().contains("int") || entity.getName().contains("double"))) {
            return numberCellStyle;
        }
        return super.getStyles(noneStyler, entity);
    }

    @Override
    public CellStyle getTitleStyle(short color) {
        CellStyle titleStyle = this.workbook.createCellStyle();
        Font font = this.workbook.createFont();
        font.setFontHeightInPoints((short)10);
        titleStyle.setFont(font);
        titleStyle.setFont(font);
        titleStyle.setFillForegroundColor(color);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return titleStyle;
    }
}
