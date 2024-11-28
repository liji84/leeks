package leeks.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class StockBean extends AbstractRowDataBean {
    //"编码,股票名称,涨跌,涨跌幅,最高价,最低价,当前价,成本价,持仓,收益率,收益,更新时间"
    @Column(value = "编码", sequence = 0)
    private String code;
    @Column(value = "股票名称", sequence = 1, maybeGraying = true)
    private String name;
    @Column(value = "涨跌", sequence = 2, mayBeChangeMarked = true)
    private String change;//涨跌
    @Column(value = "涨跌幅", sequence = 3, mayBeChangeMarked = true)
    private String changePercent;
    @Column(value = "最高价", sequence = 4)
    private String max;
    @Column(value = "最低价", sequence = 5)
    private String min;
    @Column(value = "当前价", sequence = 6)
    private String now;
    @Column(value = "成本价", sequence = 7)
    private String costPrise;//成本价
    @Column(value = "持仓", sequence = 8)
    private String bonds;//持仓
    @Column(value = "收益率", sequence = 9, mayBeChangeMarked = true)
    private String incomePercent;//收益率
    @Column(value = "收益", sequence = 10, mayBeChangeMarked = true)
    private String income;//收益
    @Column(value = "更新时间", sequence = 11)
    private String time;

    //配置code同时配置成本价和成本值
    public StockBean(String code) {
        if (StringUtils.isNotBlank(code)) {
            String[] codeStr = code.split(",");
            if (codeStr.length > 2) {
                this.code = codeStr[0];
                this.costPrise = codeStr[1];
                this.bonds = codeStr[2];
            } else {
                this.code = codeStr[0];
                this.costPrise = "--";
                this.bonds = "--";
            }
        } else {
            this.code = code;
        }
        this.name = "--";
    }

    public StockBean(String code, Map<String, String[]> codeMap){
        this.code = code;
        if(codeMap.containsKey(code)){
            String[] codeStr = codeMap.get(code);
            if (codeStr.length > 2) {
                this.code = codeStr[0];
                this.costPrise = codeStr[1];
                this.bonds = codeStr[2];
            }
        }
    }


    @Column("涨跌")
    public String getChangeStr() {
        String changeStr = "--";
        if (this.getChange() != null) {
            changeStr = this.getChange().startsWith("-") ? this.getChange() : "+" + this.getChange();
        }
        return changeStr;
    }

    @Column("涨跌幅")
    public String getChangePercentStr() {
        String changePercentStr = "--";
        if (this.getChangePercent() != null) {
            changePercentStr = this.getChangePercent().startsWith("-") ? this.getChangePercent() : "+" + this.getChangePercent();
        }
        return changePercentStr + "%";
    }

    @Column("收益率")
    public String getIncomePercentStr() {
        return this.getCostPrise() != null ? this.getIncomePercent() + "%" : this.getIncomePercent();
    }

    @Column("更新时间")
    public String getUpdateTimeStr() {
        String timeStr = "--";
        if (this.getTime() != null) {
            timeStr = this.getTime().substring(8);
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
        StockBean bean = (StockBean) o;
        return Objects.equals(code, bean.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
