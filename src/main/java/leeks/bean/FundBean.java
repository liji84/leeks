package leeks.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class FundBean extends AbstractRowDataBean {
    //"编码,基金名称,估算涨跌,当日净值,估算净值,持仓成本价,持有份额,收益率,收益,更新时间"

    @SerializedName("fundcode")
    @Column(value = "编码", sequence = 0)
    private String fundCode;
    @SerializedName("name")
    @Column(value = "基金名称", sequence = 1, maybeGraying = true)
    private String fundName;
    @Column(value = "估算涨跌", sequence = 2, mayBeChangeMarked = true)
    private String gszzl;//估算涨跌百分比 即-0.42%
    @Column(value = "当日净值", sequence = 2)
    private String dwjz;//当日净值
    private String jzrq;//净值日期
    @Column(value = "估算净值", sequence = 3)
    private String gsz; //估算净值
    @Column(value = "持仓成本价", sequence = 4)
    private String costPrise;//持仓成本价
    @Column(value = "持有份额", sequence = 5)
    private String bonds;//持有份额
    @Column(value = "收益率", sequence = 6, mayBeChangeMarked = true)
    private String incomePercent;//收益率
    @Column(value = "收益", sequence = 7, mayBeChangeMarked = true)
    private String income;//收益
    @Column(value = "更新时间", sequence = 8)
    private String gztime;//gztime估值时间

    public FundBean(String fundCode) {
        if (StringUtils.isNotBlank(fundCode)) {
            String[] codeStr = fundCode.split(",");
            if (codeStr.length > 2) {
                this.fundCode = codeStr[0];
                this.costPrise = codeStr[1];
                this.bonds = codeStr[2];
            } else {
                this.fundCode = codeStr[0];
                this.costPrise = "--";
                this.bonds = "--";
            }
        } else {
            this.fundCode = fundCode;
        }
        this.fundName = "--";
    }

    public static void loadFund(FundBean fund, Map<String, String[]> codeMap) {
        String code = fund.getFundCode();
        if (codeMap.containsKey(code)) {
            String[] codeStr = codeMap.get(code);
            if (codeStr.length > 2) {
                fund.setCostPrise(codeStr[1]);
                fund.setBonds(codeStr[2]);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FundBean fundBean = (FundBean) o;
        return Objects.equals(fundCode, fundBean.fundCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundCode);
    }

    @Override
    public String getCode() {
        return fundCode;
    }

    @Column("估值涨跌")
    private String getGzzdlStr() {
        String gszzlStr = "--";
        String gszzl = this.getGszzl();
        if (gszzl != null) {
            gszzlStr = gszzl.startsWith("-") ? gszzl : "+" + gszzl;
        }
        return gszzlStr + "%";
    }

    @Column("更新时间")
    private String getUpdateTimeStr() {
        String timeStr = this.getGztime();
        if (timeStr == null) {
            timeStr = "--";
        }
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (timeStr.startsWith(today)) {
            timeStr = timeStr.substring(timeStr.indexOf(" "));
        }
        return timeStr;
    }

    @Column("收益率")
    private String getIncomePercentStr() {
        return this.getCostPrise() != null ? this.getIncomePercent() + "%" : this.getIncomePercent();
    }

    @Column("当前净值")
    private String getDwjzStr() {
        return this.getDwjz() + "[" + this.getJzrq() + "]";
    }
}
