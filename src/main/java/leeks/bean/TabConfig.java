package leeks.bean;

import lombok.Getter;

import java.util.List;

@Getter
public class TabConfig {

    private final String tableHeaderKey;
    private final List<String> tableHeaders;
    private final String cronExpressionKey;
    private final String codesKey;

    public TabConfig(String tableHeaderKey, List<String> tableHeaders, String cronExpressionKey, String codesKey) {
        this.tableHeaderKey = tableHeaderKey;
        this.tableHeaders = tableHeaders;
        this.cronExpressionKey = cronExpressionKey;
        this.codesKey = codesKey;
    }

}
