package leeks.bean;

import leeks.utils.PinYinUtils;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractRowDataBean {

    public abstract String getCode();

    protected static DecimalFormat decimalFormat = new DecimalFormat("#.00");

    /**
     * 返回列名的VALUE 用作展示
     *
     * @param column   字段名
     * @param colorful 隐蔽模式
     * @return 对应列名的VALUE值 无法匹配返回""
     */
    public String getValueByColumn(String column, boolean colorful) {
        AtomicReference<String> value = new AtomicReference<>("");
        //先匹配字段
        Arrays.stream(this.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).value().equals(column))
                .findFirst().ifPresent(field -> {
                    try {
                        field.setAccessible(true);
                        Object o = field.get(this);
                        if (o != null) {
                            if (o instanceof Number) {
                                value.set(decimalFormat.format(o));
                            } else {
                                value.set(String.valueOf(o));
                            }
                            if (field.getAnnotation(Column.class).maybeGraying() && !colorful) {
                                value.set(PinYinUtils.toPinYin(String.valueOf(o)));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        //再匹配方法，方法结果覆盖字段结果
        Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Column.class) &&method.getAnnotation(Column.class).value().equals(column))
                .filter(method -> method.getParameterCount() == 0)
                .findFirst().ifPresent(method -> {
                    try {
                        Object o = method.invoke(this);
                        if (o != null) {
                            value.set(String.valueOf(o));
                            if (method.getAnnotation(Column.class).maybeGraying() && !colorful) {
                                value.set(PinYinUtils.toPinYin(String.valueOf(o)));
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

        return value.get();
    }
}
