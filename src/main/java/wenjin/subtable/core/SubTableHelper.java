package wenjin.subtable.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 玟瑾
 * @Create 2019-11-06 16:21
 * @Email 1924567147@qq.com
 * @Description
 */
public class SubTableHelper{

    private static Map< String, ICalculateStrategy > map = new HashMap<>();

    /**
     * 添加一个策略
     * @param key 对应 SQL 中的表名
     * @param strategy 表名计算策略
     */
    public static void addCalculate(String key,ICalculateStrategy strategy){
        if ( map.containsKey( key ) )
            map.replace( key,strategy );
        else
            map.put( key,strategy );
    }

    /**
     * 根据 key 获取表名策略的计算结果
     * @param key 对应 SQL 中的表名
     * @return 需要 union all 的表名集合
     */
    public static List<String> get(String key){
        if ( !map.containsKey( key ) )
            new Exception( "表策略【"+key+"】不存在" );
        return map.get( key ).calculate();
    }

    /**
     * 删除一个策略
     * @param key
     * @return
     */
    public static boolean delete(String key){
        return map.remove( key ) == null;
    }
}
