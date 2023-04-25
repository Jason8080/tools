package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.define.Format;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出Excel文档工具类
 *
 * @author Jason
 */
public class ExcelUtil {
    /**
     * The interface Style.
     */
    @FunctionalInterface
    public interface Style {
        /**
         * 样式设置.
         *
         * @param workbook 工作簿
         * @param keyStyle 表头样式
         * @param valStyle 内容样式
         */
        void style(Workbook workbook, CellStyle keyStyle, CellStyle valStyle);
    }

    private static Style style = (workbook, keyStyle, valStyle) -> style(workbook, keyStyle, valStyle);

    private static ThreadLocal<Map<Class, Format>> threadLocal = new ThreadLocal();

    /**
     * 默认的表格名称.
     * sheet name.
     */
    private static String DEFAULT_SHEET_NAME = "Sheet1";

    /**
     * 样式.
     *
     * <p>
     * 注意: 配置后全局有效.
     * </p>
     *
     * @param style the style
     */
    public static void configure(Style style) {
        ExcelUtil.style = style;
    }

    /**
     * 数据格式化.
     *
     * <p>
     * 注意: 仅当前线程有效.
     * </p>
     *
     * @param <I> the type parameter
     * @param key the key
     * @param val the val
     */
    public static <I> void install(Class<I> key, Format<I, String> val) {
        Map<Class, Format> formatMap = ExcelUtil.threadLocal.get();
        if (formatMap == null) {
            formatMap = new HashMap();
            ExcelUtil.threadLocal.set(formatMap);
        }
        formatMap.put(key, val);
    }

    /**
     * 数据格式化(卸载).
     *
     * <p>
     * 注意: 需要手动清理 (无需传参: 当参数是空时全部卸载).
     * </p>
     *
     * @param <T>  the type parameter
     * @param keys the keys
     */
    public static <T> void uninstall(Class<T>... keys) {
        Map<Class, Format> formatMap = ExcelUtil.threadLocal.get();
        if (formatMap == null) {
            return;
        }
        if (BoolUtil.isEmpty(keys)) {
            threadLocal.remove();
            return;
        }
        for (Class<T> key : keys) {
            if (key != null) {
                formatMap.remove(key);
            }
        }
    }

    /**
     * 创建工作表 (纯数据表).
     *
     * @param <T>  the type parameter
     * @param rows 整表 (单列数据)
     * @return the workbook 工作表
     */
    public static <T> Workbook generate(Collection<T> rows) {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        Sheet sheet = wb.createSheet(DEFAULT_SHEET_NAME);
        // 创建表
        CellStyle nameStyle = wb.createCellStyle();
        CellStyle valStyle = wb.createCellStyle();
        // 设置列名和列值样式
        style.style(wb, nameStyle, valStyle);
        if (BoolUtil.notEmpty(rows)) {
            int i = 0;
            Iterator<T> iterator = rows.iterator();
            while (iterator.hasNext()) {
                // 获取每行数据
                T column = iterator.next();
                Row row = sheet.createRow(i);
                int count = 0;
                sheet.setColumnWidth(0, (short) (35.7 * 150));
                Cell cell = row.createCell(count);
                cell.setCellValue(column != null ? format(column) : null);
                cell.setCellStyle(nameStyle);
            }
        }
        return wb;
    }

    /**
     * 创建工作表 (动态列).
     *
     * @param <T>     the type parameter
     * @param rows    the rows
     * @param columns the columns
     * @return the workbook
     */
    public static <T> Workbook news(Collection<T> rows, String... columns) {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        if (BoolUtil.notEmpty(rows)) {
            List<Map<String, Object>> maps = rows.stream().map(x -> ClassUtil.generateMap(x)).collect(Collectors.toList());
            createSheet(wb, DEFAULT_SHEET_NAME, maps, columns);
        }
        return wb;
    }

    /**
     * 创建工作表 (动态列).
     *
     * @param book the book
     * @return the workbook
     */
    public static Workbook create(Map<String, Collection<Map<String, Object>>> book) {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        if (BoolUtil.notEmpty(book)) {
            Iterator<Map.Entry<String, Collection<Map<String, Object>>>> it = book.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Collection<Map<String, Object>>> next = it.next();
                String sheet = next.getKey();
                Collection<Map<String, Object>> value = next.getValue();
                createSheet(wb, sheet, value);
            }
        }
        return wb;
    }

    /**
     * 创建工作表 (动态列).
     *
     * @param rows the rows
     * @return the workbook
     */
    public static Workbook create(Collection<Map<String, Object>> rows) {
        return create(DEFAULT_SHEET_NAME, rows);
    }

    /**
     * 创建工作表 (动态列).
     *
     * @param sheet   the sheet
     * @param rows    the rows
     * @param columns the columns
     * @return the workbook
     */
    public static Workbook create(String sheet, Collection<Map<String, Object>> rows, String... columns) {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        createSheet(wb, sheet, rows, columns);
        return wb;
    }


    /**
     * 创建工作表 (数据分离).
     *
     * @param sheet   整表 (多列数据)
     * @param columns the columns 列名
     * @return the workbook 工作表
     */
    public static Workbook create(Collection<Collection<Object>> sheet, String... columns) {
        Map<String, Collection<Collection<Object>>> map = new HashMap(1);
        map.put(DEFAULT_SHEET_NAME, sheet);
        return create(map, columns);
    }

    /**
     * 创建工作簿 (数据分离).
     *
     * @param book    the book 整簿纯数据
     * @param columns the columns 列名
     * @return the workbook 工作簿
     */
    public static Workbook create(Map<String, Collection<Collection<Object>>> book, String... columns) {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        if (BoolUtil.notEmpty(book)) {
            Iterator<Map.Entry<String, Collection<Collection<Object>>>> it = book.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Collection<Collection<Object>>> next = it.next();
                // 创建表
                Sheet sheet = wb.createSheet(next.getKey());
                CellStyle nameStyle = wb.createCellStyle();
                CellStyle valStyle = wb.createCellStyle();
                // 设置列名和列值样式
                style.style(wb, nameStyle, valStyle);
                int def = 0;
                if (BoolUtil.notEmpty(columns)) {
                    Row top = sheet.createRow(def++);
                    for (int i = 0; i < columns.length; i++) {
                        sheet.setColumnWidth(i, (short) (35.7 * 150));
                        Cell cell = top.createCell(i);
                        cell.setCellValue(columns[i]);
                        cell.setCellStyle(nameStyle);
                    }
                }
                // 获取整页数据
                Collection<Collection<Object>> data = next.getValue();
                if (BoolUtil.notEmpty(data)) {
                    int i = 0;
                    Iterator<Collection<Object>> iterator = data.iterator();
                    while (iterator.hasNext()) {
                        // 获取每行数据
                        Collection<Object> list = iterator.next();
                        Row row = sheet.createRow(i + def);
                        int count = 0;
                        for (Object value : list) {
                            Cell cell = row.createCell(count);
                            cell.setCellValue(value != null ? format(value) : null);
                            cell.setCellStyle(valStyle);
                            ++count;
                        }
                        ++i;
                    }
                }
            }
        }
        return wb;
    }

    private static void createSheet(Workbook workbook, String name, Collection<Map<String, Object>> rows, String... columns) {
        Sheet sheet = workbook.createSheet(name);
        // 创建表
        CellStyle keyStyle = workbook.createCellStyle();
        CellStyle valStyle = workbook.createCellStyle();
        // 设置列名和列值样式
        style.style(workbook, keyStyle, valStyle);
        if (BoolUtil.notEmpty(rows)) {
            // 表头处理
            String[] cs = QuickUtil.is(BoolUtil.isEmpty(columns), () -> getSet(rows).toArray(new String[]{}), () -> columns);
            Row row0 = sheet.createRow(0);
            int count = 0;
            for (String column : cs) {
                sheet.setColumnWidth(count, (short) (35.7 * 150));
                Cell cell = row0.createCell(count);
                cell.setCellValue(column);
                cell.setCellStyle(keyStyle);
                count++;
            }
            // 数据处理
            int num = 0;
            for (Map<String, Object> map : rows) {
                Row row = sheet.createRow(++num);
                int counter = 0;
                for (String column : cs) {
                    Cell cell = row.createCell(counter);
                    Object value = map.get(column);
                    cell.setCellValue(value != null ? format(value) : null);
                    cell.setCellStyle(valStyle);
                    counter++;
                }
            }
        }
    }

    private static Set<String> getSet(Collection<Map<String, Object>> rows) {
        Set<String> set = new LinkedHashSet();
        for (Map<String, Object> map : rows) {
            map.forEach((key, val) -> set.add(key));
        }
        return set;
    }

    private static void style(Workbook wb, CellStyle keyStyle, CellStyle valStyle) {
        // 字体设置
        setFont(wb, keyStyle, valStyle);
        // 格式化设置
        setFormat(wb, keyStyle, valStyle);
    }

    private static void setFont(Workbook wb, CellStyle keyStyle, CellStyle valStyle) {
        // 创建两种字体
        Font f = wb.createFont();
        Font f2 = wb.createFont();

        // 创建第一种字体样式（用于列名）
        f.setFontHeightInPoints((short) 14);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);

        // 创建第二种字体样式（用于值）
        f2.setFontHeightInPoints((short) 10);
        f2.setColor(IndexedColors.BLACK.getIndex());
        f2.setBold(false);

        // 创建第三种字体样式（未使用）
        Font f3 = wb.createFont();
        f3.setFontHeightInPoints((short) 10);
        f3.setColor(IndexedColors.RED.getIndex());

        // 设置第一种单元格的样式（用于列名）
        keyStyle.setFont(f);
        keyStyle.setBorderLeft(BorderStyle.THIN);
        keyStyle.setBorderRight(BorderStyle.THIN);
        keyStyle.setBorderTop(BorderStyle.THIN);
        keyStyle.setBorderBottom(BorderStyle.THIN);
        keyStyle.setAlignment(HorizontalAlignment.CENTER);

        // 设置第二种单元格的样式（用于值）
        valStyle.setFont(f2);
        valStyle.setBorderLeft(BorderStyle.THIN);
        valStyle.setBorderRight(BorderStyle.THIN);
        valStyle.setBorderTop(BorderStyle.THIN);
        valStyle.setBorderBottom(BorderStyle.THIN);
        valStyle.setAlignment(HorizontalAlignment.CENTER);
    }

    private static void setFormat(Workbook wb, CellStyle keyStyle, CellStyle valStyle) {

    }


    private static String format(Object obj) {
        Map<Class, Format> formatMap = threadLocal.get();
        if (obj == null) {
            return null;
        }
        if (formatMap != null) {
            Format<Object, String> format = formatMap.get(obj.getClass());
            if (format != null) {
                return format.format(obj);
            }
        }
        return obj.toString();
    }
}
