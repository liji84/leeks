package leeks.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import leeks.constant.Constants;
import leeks.quartz.QuartzManager;
import leeks.utils.HttpClientPool;
import leeks.utils.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SettingsWindow implements Configurable {
    private JPanel panel1;
    private JTextArea textAreaFund;
    private JTextArea textAreaStock;
    private JCheckBox checkbox;
    /**
     * 使用tab界面，方便不同的设置分开进行控制
     */
    private JTabbedPane tabbedPane1;
    private JCheckBox checkBoxTableStriped;
    private JTextField cronExpressionFund;
    private JTextField cronExpressionStock;
    private JTextField cronExpressionCoin;
    private JCheckBox checkboxSina;
    private JCheckBox checkboxLog;
    private JTextArea textAreaCoin;
    private JLabel proxyLabel;
    private JTextField inputProxy;
    private JButton proxyTestButton;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Leeks";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        textAreaFund.setText(instance.getValue(Constants.Keys.FUNDS));
        textAreaStock.setText(instance.getValue(Constants.Keys.STOCKS));
        textAreaCoin.setText(instance.getValue(Constants.Keys.COINS));
        checkbox.setSelected(!instance.getBoolean(Constants.Keys.COLORFUL));
        checkBoxTableStriped.setSelected(instance.getBoolean(Constants.Keys.TABLE_STRIPED));
        checkboxSina.setSelected(instance.getBoolean(Constants.Keys.STOCKS_SINA));
        checkboxLog.setSelected(instance.getBoolean(Constants.Keys.CLOSE_LOG));
        //开市时间每分钟
        cronExpressionFund.setText(instance.getValue(Constants.Keys.CRON_EXPRESSION_FUND,
                String.join(";", Arrays.asList(
                        "0 30-59 9 ? * 2-6",
                        "0 * 10 ? * 2-6",
                        "0 0-31 11 ? * 2-6",
                        "0 * 13-14 ? * 2-6",
                        "0 0-1 15 ? * 2-6"))
        ));
        //开市时间每10秒
        cronExpressionStock.setText(instance.getValue(Constants.Keys.CRON_EXPRESSION_STOCK,
                String.join(";", Arrays.asList(
                        "*/10 30-59 9 ? * 2-6",
                        "*/10 * 10 ? * 2-6",
                        "*/10 0-31 11 ? * 2-6",
                        "*/10 * 13-14 ? * 2-6",
                        "*/10 0-1 15 ? * 2-6"))
        ));
        //默认每60秒执行
        cronExpressionCoin.setText(instance.getValue(Constants.Keys.CRON_EXPRESSION_COIN, "0 * * * * ?"));
        //代理设置
        inputProxy.setText(instance.getValue(Constants.Keys.PROXY));
        proxyTestButton.addActionListener(actionEvent -> {
            String proxy = inputProxy.getText().trim();
            testProxy(proxy);
        });
        return panel1;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        String errorMsg = checkConfig();
        if (StringUtils.isNotEmpty(errorMsg)) {
            LogUtil.notify(errorMsg, false);
            throw new ConfigurationException(errorMsg);
        }

        PropertiesComponent instance = PropertiesComponent.getInstance();
        instance.setValue(Constants.Keys.FUNDS, textAreaFund.getText());
        instance.setValue(Constants.Keys.STOCKS, textAreaStock.getText());
        instance.setValue(Constants.Keys.COINS, textAreaCoin.getText());
        instance.setValue(Constants.Keys.COLORFUL, !checkbox.isSelected());
        instance.setValue(Constants.Keys.CRON_EXPRESSION_FUND, cronExpressionFund.getText());
        instance.setValue(Constants.Keys.CRON_EXPRESSION_STOCK, cronExpressionStock.getText());
        instance.setValue(Constants.Keys.CRON_EXPRESSION_COIN, cronExpressionCoin.getText());
        instance.setValue(Constants.Keys.TABLE_STRIPED, checkBoxTableStriped.isSelected());
        instance.setValue(Constants.Keys.STOCKS_SINA, checkboxSina.isSelected());
        instance.setValue(Constants.Keys.CLOSE_LOG, checkboxLog.isSelected());
        String proxy = inputProxy.getText().trim();
        instance.setValue(Constants.Keys.PROXY, proxy);
        HttpClientPool.getHttpClient().buildHttpClient(proxy);

        MainWindow.TABS.forEach(AbstractTab::apply);
    }


    private void testProxy(String proxy) {
        if (proxy.indexOf('：') > 0) {
            LogUtil.notify("别用中文分割符啊!", false);
            return;
        }
        HttpClientPool httpClientPool = HttpClientPool.getHttpClient();
        httpClientPool.buildHttpClient(proxy);
        try {
            httpClientPool.get("https://www.baidu.com");
            LogUtil.notify("代理测试成功!请保存", true);
        } catch (Exception e) {
            Logger.getLogger(SettingsWindow.class.getCanonicalName()).severe(e.getMessage());
            LogUtil.notify("测试代理异常!", false);
        }
    }

    public static List<String> getConfigList(String key, String split) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = value.split(split);
        for (String code : codes) {
            if (!code.isEmpty()) {
                set.add(code.trim());
            }
        }
        return new ArrayList<>(set);
    }

    public static List<String> getConfigList(String key) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = null;
        if (value.contains(";")) {//包含分号
            codes = value.split("[;]");
        } else {
            codes = value.split("[,，]");
        }
        for (String code : codes) {
            if (!code.isEmpty()) {
                set.add(code.trim());
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 检查配置项
     *
     * @return 返回提示的错误信息
     */
    private String checkConfig() {
        List<String> errors = Stream.of(Stream.of(cronExpressionFund.getText().split(";")).map(s -> QuartzManager.checkCronExpression(s) ? null : "Fund请配置正确的cron表达式[" + s + "]"),
                Stream.of(cronExpressionCoin.getText().split(";")).map(s -> QuartzManager.checkCronExpression(s) ? null : "Coin请配置正确的cron表达式[" + s + "]"),
                        Stream.of(cronExpressionStock.getText().split(";")).map(s -> QuartzManager.checkCronExpression(s) ? null : "Stock请配置正确的cron表达式[" + s + "]")
                ).flatMap(e -> e)
                .filter(StringUtils::isNotEmpty).toList();
        return errors.isEmpty() ? "" : String.join("、", errors) + "。";
    }
}
