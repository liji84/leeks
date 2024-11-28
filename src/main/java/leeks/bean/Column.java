package leeks.bean;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 标签中文名称
     */
    String value();

    /**
     * 序号，值越小越靠前
     */
    int sequence() default Integer.MAX_VALUE;

    /**
     * 是否可以低敏感显示（显示拼音）
     */
    boolean maybeGraying() default false;

    /**
     * 是否可以标记涨跌
     */
    boolean mayBeChangeMarked() default false;
}
