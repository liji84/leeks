package leeks.utils;

import com.intellij.ide.util.PropertiesComponent;
import leeks.bean.*;
import leeks.constant.Constants;

import java.util.*;
import java.util.stream.Stream;

public final class PropertiesUtils {

    private PropertiesUtils() {
    }

    /**
     * 获得已排序的表头
     * @param key 业务分类 {@link Constants.Keys}
     * @return 表头列表
     */
    public static List<String> getSortedTableHeaders(String key) {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        List<String> rawHeaders;
        String property;
        switch (key) {
            case Constants.Keys.FUNDS:
                rawHeaders = getColumnHeaders(FundBean.class);
                property = instance.getValue(Constants.Keys.TABLE_HEADER_KEY_FUND);
                break;
            case Constants.Keys.STOCKS:
                rawHeaders = getColumnHeaders(StockBean.class);
                property = instance.getValue(Constants.Keys.TABLE_HEADER_KEY_STOCK);
                break;
            case Constants.Keys.COINS:
                rawHeaders = getColumnHeaders(CoinBean.class);
                property = instance.getValue(Constants.Keys.TABLE_HEADER_KEY_COIN);
                break;
            default:
                return Collections.emptyList();
        }

        if (property == null) {
            return rawHeaders;
        }

        String[] sortedData = property.split(",");
        List<String> sortedHeaders = new ArrayList<>(rawHeaders.size());
        rawHeaders = new ArrayList<>(rawHeaders);
        for (String header : sortedData) {
            if (rawHeaders.contains(header)) {
                sortedHeaders.add(header);
                rawHeaders.remove(header);
            }
        }
        sortedHeaders.addAll(rawHeaders);
        return sortedHeaders;
    }

    private static List<String> getColumnHeaders(Class<? extends AbstractRowDataBean> clazz) {
        return Stream.concat(
                        Arrays.stream(clazz.getDeclaredFields()).map(f -> f.getAnnotationsByType(Column.class)),
                        Arrays.stream(clazz.getDeclaredFields()).map(m -> m.getAnnotationsByType(Column.class)))
                .flatMap(Stream::of)
                .sorted(Comparator.comparingInt(Column::sequence))
                .map(Column::value).distinct().toList();
    }

}
