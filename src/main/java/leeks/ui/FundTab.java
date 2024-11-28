package leeks.ui;

import leeks.bean.FundBean;
import leeks.bean.TabConfig;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import leeks.handler.TianTianFundHandler;

public class FundTab extends AbstractTab<FundBean> {
    public static final String NAME = "Fund";

    private static final TabConfig CONFIG = new TabConfig(Constants.Keys.TABLE_HEADER_KEY_FUND,
            getColumnHeaders(FundBean.class),
            //"编码,基金名称,估算涨跌,当日净值,估算净值,持仓成本价,持有份额,收益率,收益,更新时间",
            Constants.Keys.CRON_EXPRESSION_FUND,
            Constants.Keys.FUNDS);

    private final TianTianFundHandler handler;

    public FundTab() {
        super();
        handler = new TianTianFundHandler(tableContext);
        apply();
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
    protected AbstractHandler<FundBean> getHandler() {
        return this.handler;
    }
}
