package leeks.ui;

import leeks.bean.CoinBean;
import leeks.bean.TabConfig;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.handler.YahooCoinHandler;

import java.util.List;

public class CoinTab extends AbstractTab<CoinBean> {
    public static final String NAME = "Coin";

    private static final TabConfig CONFIG = new TabConfig(Constants.Keys.TABLE_HEADER_KEY_COIN,
             getColumnHeaders(CoinBean.class),
            //"编码,当前价,涨跌,涨跌幅,最高价,最低价,更新时间",
            Constants.Keys.CRON_EXPRESSION_COIN,
            Constants.Keys.COINS);

    static AbstractHandler<CoinBean> handler;

    public CoinTab() {
        super();
        handler = new YahooCoinHandler(tableContext);
        apply();
    }

    @Override
    protected TabConfig getConfig() {
        return CONFIG;
    }

    @Override
    protected AbstractHandler getHandler() {
        return null;
    }

    @Override
    protected List<String> getCodes() {
        return SettingsWindow.getConfigList(CONFIG.getCodesKey(), "[,，]");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
