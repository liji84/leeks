package leeks.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class CoinBean extends AbstractRowDataBean {
    //"编码,当前价,涨跌,涨跌幅,最高价,最低价,更新时间",

    @Column(value = "编码", sequence = 0)
    private String symbol;
    @Column(value = "当前价", sequence = 1)
    private double regularMarketPrice;
    @Column(value = "涨跌", sequence = 2, mayBeChangeMarked = true)
    private double regularMarketChange;
    @Column(value = "最高价", sequence = 3)
    private double regularMarketDayHigh;
    @Column(value = "最低价", sequence = 4)
    private double regularMarketDayLow;
    @Column(value = "涨跌幅", sequence = 5, mayBeChangeMarked = true)
    private double regularMarketChangePercent;
    @Column(value = "更新时间", sequence = 6)
    private long regularMarketTime;

    public CoinBean(String code) {
        this.symbol = code;
    }

    @Override
    public String getCode() {
        return symbol;
    }

    @Column(value = "更新时间")
    public String getUpdateTimeStr() {
        String timeStr = "--";
        if (this.regularMarketTime != 0) {
            timeStr = String.valueOf(this.regularMarketTime);
        }
        return timeStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoinBean bean = (CoinBean) o;
        return Objects.equals(symbol, bean.symbol);
    }

    @Override
    public int hashCode() {
        return this.symbol.hashCode();
    }
}
