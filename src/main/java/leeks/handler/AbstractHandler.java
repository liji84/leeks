package leeks.handler;

import com.intellij.ide.util.PropertiesComponent;
import leeks.bean.AbstractRowDataBean;
import leeks.constant.Constants;
import leeks.ui.AbstractTab;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 抽象的数据查询handler类
 */
public abstract class AbstractHandler<T extends AbstractRowDataBean> {

    private final ReentrantLock lock = new ReentrantLock();

    protected AbstractTab<T>.TableContext tableContext;

    /**
     * 从网络更新数据, 增加了锁防止多cron表达式重复同步执行.
     *
     * @param code 股票代码/基金代码/虚拟币代码
     */
    public void handle(List<String> code) {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                handleInternal(code);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    protected abstract void handleInternal(List<String> code);

    /**
     * 按照编码顺序初始化，for 每次刷新都乱序，没办法控制显示顺序
     *
     * @param code
     */
    public void setupTable(List<String> code) {
        for (String s : code) {
            try {
                updateData(getParameterizedType().getConstructor(String.class).newInstance(s) );
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Class<T> getParameterizedType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 参考源码{@link DefaultTableModel#setValueAt}，此为直接更新行，提高点效率
     *
     * @param rowIndex
     * @param rowData
     */
    protected void updateRow(int rowIndex, Vector<Object> rowData) {
        tableContext.getTableModel().getDataVector().set(rowIndex, rowData);
        // 通知listeners刷新ui
        tableContext.getTableModel().fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * 参考源码{@link DefaultTableModel#removeRow(int)}，此为直接清除全部行，提高点效率
     */
    public void clearRow() {
        int size = tableContext.getTableModel().getDataVector().size();
        if (0 < size) {
            tableContext.getTableModel().getDataVector().clear();
            // 通知listeners刷新ui
            tableContext.getTableModel().fireTableRowsDeleted(0, size - 1);
        }
    }


    /**
     * 查找列项中的valueName所在的行
     *
     * @param columnIndex 列号
     * @param value       值
     * @return 如果不存在返回-1
     */
    protected int findRowIndex(int columnIndex, String value) {
        int rowCount = tableContext.getTableModel().getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object valueAt = tableContext.getTableModel().getValueAt(rowIndex, columnIndex);
            if (StringUtils.equalsIgnoreCase(value, valueAt.toString())) {
                return rowIndex;
            }
        }
        return -1;
    }

    protected void updateUI() {
        SwingUtilities.invokeLater(() -> {
            tableContext.getRefreshTimeLabel().setText(LocalDateTime.now().format(Constants.TIME_FORMATTER));
            tableContext.getRefreshTimeLabel().setToolTipText("最后刷新时间");
        });

        PropertiesComponent instance = PropertiesComponent.getInstance();
        tableContext.refreshCellColor(instance.getBoolean(Constants.Keys.COLORFUL));
    }

    protected void updateData(AbstractRowDataBean bean) {
        if (bean.getCode() == null) {
            return;
        }
        Vector<Object> convertData = convertData(bean);
        if (convertData == null) {
            return;
        }
        // 获取行
        int index = findRowIndex(tableContext.getCodeColumnIndex(), bean.getCode());
        if (index >= 0) {
            updateRow(index, convertData);
        } else {
            tableContext.getTableModel().addRow(convertData);
        }
    }

    private Vector<Object> convertData(AbstractRowDataBean bean) {
        if (bean == null) {
            return null;
        }

        PropertiesComponent instance = PropertiesComponent.getInstance();
        boolean colorful = instance.getBoolean(Constants.Keys.COLORFUL);

        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<>(tableContext.getTableHeaders().size());
        for (String tableHeader : tableContext.getTableHeaders()) {
            v.addElement(bean.getValueByColumn(tableHeader, colorful));
        }
        return v;
    }
}
