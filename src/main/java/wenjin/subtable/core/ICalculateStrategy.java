package wenjin.subtable.core;

import java.util.List;

/**
 * @Author 玟瑾
 * @Create 2019-11-06 16:16
 * @Email 1924567147@qq.com
 * @Description 计算表名的函数值接口
 */
@FunctionalInterface
public interface ICalculateStrategy{
    /**
     * 该方法需要计算出或者直接提供需要 Union ALl 的表名
     * @return
     */
    List<String> calculate();
}
