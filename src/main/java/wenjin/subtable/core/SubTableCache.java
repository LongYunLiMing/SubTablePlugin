package wenjin.subtable.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 玟瑾
 * @Create 2019-12-10 14:16
 * @Email 1924567147@qq.com
 * @Description 缓存 SQL 语句，以及分表后的参数顺序
 */
public class SubTableCache{

    private static Map<String,String> sqlCache = new HashMap<>();
    private static Map<String, List<Integer> > paramListCache = new HashMap<>();

    public static String getSQL(String key){
        return sqlCache.get( key );
    }

    public static List<Integer> getParamList(String key){
        return paramListCache.get( key );
    }

    public static void putSQL(String key,String sql) {
        if ( sqlCache.containsKey( key ) )
            new Exception( "id 不能重复【"+key+"】" ).printStackTrace();
        sqlCache.put( key,sql );
    }

    public static void putParamList(String key,List<Integer> paramList){
        if ( paramListCache.containsKey( key ) )
            new Exception( "id 不能重复【"+key+"】" ).printStackTrace();
        paramListCache.put( key,paramList );
    }

    public static boolean exit(String id){
        return sqlCache.containsKey( id ) && paramListCache.containsKey( id );
    }
}

