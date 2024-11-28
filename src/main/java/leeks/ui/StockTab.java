package leeks.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.awt.RelativePoint;
import leeks.bean.StockBean;
import leeks.bean.TabConfig;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.handler.SinaStockHandler;
import leeks.handler.TencentStockHandler;
import leeks.utils.LogUtil;
import leeks.utils.PopupsUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;

public class StockTab extends AbstractTab<StockBean> {

    private static final String NAME = "Stock";

    static AbstractHandler<StockBean> handler;

    private static final TabConfig CONFIG = new TabConfig(Constants.Keys.TABLE_HEADER_KEY_STOCK,
            getColumnHeaders(StockBean.class),
            //"编码,股票名称,涨跌,涨跌幅,最高价,最低价,当前价,成本价,持仓,收益率,收益,更新时间",
            Constants.Keys.CRON_EXPRESSION_STOCK,
            Constants.Keys.STOCKS);

    public StockTab() {
        super();
        handler = factoryHandler();

        // 只有股票才支持表格事件
        tableContext.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (tableContext.getTable().getSelectedRow() < 0) {
                    return;
                }
                String code = String.valueOf(tableContext.getTable().getModel().getValueAt(tableContext.getTable().convertRowIndexToModel(tableContext.getTable().getSelectedRow()), tableContext.getCodeColumnIndex()));
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
                    // 鼠标左键双击
                    try {
                        PopupsUiUtil.showImageByStockCode(code, PopupsUiUtil.StockShowType.min, new Point(e.getXOnScreen(), e.getYOnScreen()));
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        LogUtil.info(ex.getMessage());
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    //鼠标右键
                    JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<PopupsUiUtil.StockShowType>("",
                            PopupsUiUtil.StockShowType.values()) {
                        @Override
                        public @NotNull String getTextFor(PopupsUiUtil.StockShowType value) {
                            return value.getDesc();
                        }

                        @Override
                        public @Nullable PopupStep<?> onChosen(PopupsUiUtil.StockShowType selectedValue, boolean finalChoice) {
                            try {
                                PopupsUiUtil.showImageByStockCode(code, selectedValue, new Point(e.getXOnScreen(), e.getYOnScreen()));
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                                LogUtil.info(ex.getMessage());
                            }
                            return super.onChosen(selectedValue, finalChoice);
                        }
                    }).show(RelativePoint.fromScreen(new Point(e.getXOnScreen(), e.getYOnScreen())));
                }
            }
        });

        apply();
    }

    private AbstractHandler<StockBean> factoryHandler() {
        boolean useSinaApi = PropertiesComponent.getInstance().getBoolean(Constants.Keys.STOCKS_SINA);
        if (useSinaApi) {
            if (handler instanceof SinaStockHandler) {
                return handler;
            }
            return new SinaStockHandler(tableContext);
        }
        if (handler instanceof TencentStockHandler) {
            return handler;
        }
        return new TencentStockHandler(tableContext);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected TabConfig getConfig() {
        return CONFIG;
    }

    @Override
    protected AbstractHandler getHandler() {
        return handler;
    }
}
