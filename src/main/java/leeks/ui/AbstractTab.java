package leeks.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import leeks.bean.AbstractRowDataBean;
import leeks.bean.Column;
import leeks.bean.TabConfig;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.quartz.HandlerJob;
import leeks.quartz.QuartzManager;
import leeks.utils.PinYinUtils;
import leeks.utils.PropertiesUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * 抽象内容页,3种投资类型内容页高度相似,作抽象处理.
 */
public abstract class AbstractTab<T extends AbstractRowDataBean> {

    @Getter
    protected JPanel panel;

    protected TableContext tableContext;


    protected JPanel toolPanel;
    protected AnAction refreshAction;
    protected AnAction pauseAction;
    protected AnAction resumeAction;

    /**
     * @return 选项卡的名字
     */
    public abstract String getName();

    public AbstractTab() {
        panel = new JPanel();
        panel.setBorder(JBUI.Borders.empty());
        panel.setLayout(new BorderLayout());

        tableContext = createTable(getConfig());

        refreshAction = new AnAction("立刻刷新当前表格数据", null, AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(false);
                panel.updateUI();
                refresh();
                e.getPresentation().setEnabled(true);
                panel.updateUI();
            }
        };
        pauseAction = new AnAction("停止刷新当前表格数据", null, AllIcons.Actions.Pause) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                stop();
                this.getTemplatePresentation().setVisible(false);
                resumeAction.getTemplatePresentation().setVisible(true);
                panel.updateUI();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setVisible(this.getTemplatePresentation().isVisible());
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        resumeAction = new AnAction("恢复刷新当前表格数据", null, AllIcons.Actions.Resume) {
            {
                this.getTemplatePresentation().setVisible(false);
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                start();
                this.getTemplatePresentation().setVisible(false);
                pauseAction.getTemplatePresentation().setVisible(true);
                panel.updateUI();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setVisible(this.getTemplatePresentation().isVisible());
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(tableContext.getTable())
                .addExtraActions(refreshAction, pauseAction, resumeAction)
                .setToolbarPosition(ActionToolbarPosition.TOP);
        toolPanel = toolbarDecorator.createPanel();
        CommonActionsPanel actionsPanel = toolbarDecorator.getActionsPanel();
        // 在工具栏的按钮条外面再套一层panel，并加入文本框
//        Component actionToolbar = actionsPanel.getComponent(0);
//        actionsPanel.remove(0);
//        JPanel newActionToolbar = new JPanel();
//        newActionToolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
//        newActionToolbar.add(actionToolbar);
//        newActionToolbar.add(new JTextField(20));
//        actionsPanel.add(newActionToolbar, BorderLayout.CENTER);
        // 添加刷新时间
        actionsPanel.add(tableContext.refreshTimeLabel, BorderLayout.EAST);
        toolPanel.setBorder(JBUI.Borders.empty());
        panel.add(toolPanel, BorderLayout.CENTER);
    }

    private TableContext createTable(TabConfig config) {
        PropertiesComponent instance = PropertiesComponent.getInstance();

        JLabel refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(JBUI.Borders.emptyRight(5));

        JBTable table = new JBTable();

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(tableModel);

        List<String> tableHeaders = PropertiesUtils.getSortedTableHeaders(config.getCodesKey());
        int codeColumnIndex = tableHeaders.indexOf("编码");

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));

        //记录列名的变化
        table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                StringBuilder tableHeadChange = new StringBuilder();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    tableHeadChange.append(table.getColumnName(i)).append(",");
                }
                PropertiesComponent instance = PropertiesComponent.getInstance();
                //将列名的修改放入环境中 key:coin_table_header_key
                instance.setValue(getConfig().getTableHeaderKey(), tableHeadChange
                        .substring(0, !tableHeadChange.isEmpty() ? tableHeadChange.length() - 1 : 0));

                //LogUtil.info(instance.getValue(WindowUtils.COIN_TABLE_HEADER_KEY));
            }
        });
        TableContext tableContext1 = new TableContext(table, tableModel, codeColumnIndex, tableHeaders, refreshTimeLabel);
        tableContext1.refreshColorful(instance.getBoolean(Constants.Keys.COLORFUL));
        return tableContext1;
    }

    /**
     * 获取选项卡配置
     *
     * @return 选项卡配置
     */
    protected abstract TabConfig getConfig();

    /**
     * 获取当前选项卡的handler
     *
     * @return 数据处理器
     */
    protected abstract AbstractHandler<T> getHandler();

    protected List<String> getCodes() {
        return SettingsWindow.getConfigList(getConfig().getCodesKey());
    }

    public void apply() {
        if (getHandler() != null) {
            PropertiesComponent instance = PropertiesComponent.getInstance();
            tableContext.getTable().setStriped(instance.getBoolean(Constants.Keys.TABLE_STRIPED));
            getHandler().clearRow();
            getHandler().setupTable(getCodes());
            refresh();
            start();
        }
    }

    public void start() {
        if (getHandler() != null) {
            PropertiesComponent instance = PropertiesComponent.getInstance();

            QuartzManager quartzManager = QuartzManager.getInstance(getName());
            HashMap<String, Object> dataMap = new HashMap<>(2);
            dataMap.put(HandlerJob.KEY_HANDLER, getHandler());
            dataMap.put(HandlerJob.KEY_CODES, getCodes());
            String cronExpression = instance.getValue(getConfig().getCronExpressionKey());
            if (StringUtils.isEmpty(cronExpression)) {
                cronExpression = "0 * * * * ?";
            }
            quartzManager.runJob(HandlerJob.class, cronExpression, dataMap);
        }
    }

    public void stop() {
        QuartzManager.getInstance(getName()).stopJob();
    }

    public void refresh() {
        if (getHandler() != null) {
            List<String> codes = getCodes();
            if (CollectionUtils.isEmpty(codes)) {
                stop(); //如果没有数据则不需要启动时钟任务浪费资源
            } else {
                getHandler().handle(codes);
            }
        }
    }

    protected static List<String> getColumnHeaders(Class<? extends AbstractRowDataBean> clazz) {
        return Stream.concat(
                        Arrays.stream(clazz.getDeclaredFields()).map(f -> f.getAnnotationsByType(Column.class)),
                        Arrays.stream(clazz.getDeclaredFields()).map(m -> m.getAnnotationsByType(Column.class)))
                .flatMap(Stream::of)
                .sorted(Comparator.comparingInt(Column::sequence))
                .map(Column::value).distinct().toList();
    }

    private Class<T> getParameterizedType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @AllArgsConstructor
    @Getter
    public class TableContext {
        private JBTable table;
        private DefaultTableModel tableModel;
        private int codeColumnIndex;
        private List<String> tableHeaders;
        protected JLabel refreshTimeLabel;

        public void refreshColorful(boolean colorful) {
            String[] columnNames = tableHeaders.toArray(new String[0]);
            // 刷新表头
            if (colorful) {
                tableModel.setColumnIdentifiers(columnNames);
            } else {
                tableModel.setColumnIdentifiers(PinYinUtils.toPinYin(columnNames));
            }
            refreshCellColor(colorful);
        }

        public void refreshCellColor(boolean colorful) {
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    double profit = NumberUtils.toDouble(StringUtils.remove(Objects.toString(value), "%"));
                    if (profit > 0) {
                        setForeground(colorful ? JBColor.RED : JBColor.DARK_GRAY);
                    } else if (profit < 0) {
                        setForeground(colorful ? JBColor.GREEN : JBColor.GRAY);
                    } else {
                        setForeground(getForeground());
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            };

            Stream.of(getParameterizedType().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Column.class))
                    .map(f -> f.getAnnotation(Column.class))
                    .filter(Column::mayBeChangeMarked)
                    .map(Column::value)
                    .forEach(
                            e -> {
                                int index;
                                if ((index = tableHeaders.indexOf(e)) > -1) {
                                    table.getColumn(tableModel.getColumnName(index)).setCellRenderer(cellRenderer);
                                }
                            }
                    );
        }
    }
}
