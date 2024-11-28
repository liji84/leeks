package leeks.handler;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import leeks.bean.CoinBean;
import leeks.ui.AbstractTab;
import leeks.utils.HttpClientPool;
import leeks.utils.LogUtil;
import lombok.Data;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class YahooCoinHandler extends AbstractHandler<CoinBean> {
    private static final String URL = "https://query1.finance.yahoo.com/v7/finance/quote?&symbols=";
    private static final String KEYS = "&fields=regularMarketChange,regularMarketChangePercent,regularMarketPrice,regularMarketTime,regularMarketDayHigh,regularMarketDayLow";

    private final Gson gson = new Gson();

    public YahooCoinHandler(AbstractTab<CoinBean>.TableContext tableContext) {
        this.tableContext = tableContext;
    }

    @Override
    public void handleInternal(List<String> code) {
        if (code.isEmpty()) {
            return;
        }

        pollStock(code);
    }

    private void pollStock(List<String> code) {
        if (code.isEmpty()){
            return;
        }
        String params = Joiner.on(",").join(code);
        try {
            String res = HttpClientPool.getHttpClient().get(URL + params + KEYS);
            handleResponse(res);
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response) {
        List<String> refreshTimeList = new ArrayList<>();
        try{
            YahooResponse yahooResponse = gson.fromJson(response, YahooResponse.class);
            for (CoinBean coinBean : yahooResponse.getQuoteResponse().getResult()) {
                updateData(coinBean);
                refreshTimeList.add(coinBean.getValueByColumn("更新时间",false));
            }
        }catch (Exception e){
            e.printStackTrace(System.out);
        }

        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
        SwingUtilities.invokeLater(() -> tableContext.getRefreshTimeLabel().setText(text));
    }

    @Data
    public static class YahooResponse {
        Result quoteResponse;

        public YahooResponse() {
        }

        @Data
        public static class Result{
            public Result() {
            }

            private List<CoinBean> result;

        }
    }
}
