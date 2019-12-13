package wenjin.subtable.core;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wenjin.subtable.utils.ParameterMappingUtil;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author 玟瑾
 * @Create 2019-11-05 17:10
 * @Email 1924567147@qq.com
 * @Description
 */
@Intercepts( {
        @Signature( type = ParameterHandler.class,method = "setParameters",args = { PreparedStatement.class })
} )
public class SubTabelPlugin implements Interceptor{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String BOUND_SQL = "boundSql.sql";
    private static final String PARAMETER_MAPPERINGS = "boundSql.parameterMappings";
    private static final String TABLE_PREFIX = "tablePrefix";
    private static final String MAPPED_STATEMENT_ID = "mappedStatement.id";

    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable{
        return invocation.proceed();
    }

    /**
     * 如果 SQL 语句中没有 tablePrefix 说明该 SQL 不需要进行分表操作，则直接返回即可
     * 否则，根据 tablePrefix 配置的前缀对 SQL 语句进行分表操作
     * @param o
     * @return
     */
    @Override
    public Object plugin(Object o){
        if ( o.getClass().equals( DefaultParameterHandler.class )){
            MetaObject metaObject = SystemMetaObject.forObject( o );
            String sql = ( String ) metaObject.getValue( BOUND_SQL );
            String tablePrefix = this.properties.getProperty( TABLE_PREFIX );
            if ( !sql.contains( tablePrefix ) )
                return o;
            log.info( "拆分表名前缀：【"+tablePrefix+"】" );
            String mappedStatementId = ( String ) metaObject.getValue( MAPPED_STATEMENT_ID );
            log.info( "对【"+mappedStatementId+"】进行分表操作" );
            ArrayList< ParameterMapping > paramterMapperings ;
            Object parameters = metaObject.getValue( PARAMETER_MAPPERINGS );
            if ( parameters instanceof ArrayList )
                paramterMapperings = ( ArrayList< ParameterMapping > ) parameters;
            else
                paramterMapperings = new ArrayList<>();
            String finalSQl;
            List< ParameterMapping > finalParameterMappings;
            if ( SubTableCache.exit( mappedStatementId ) ){
                finalSQl = SubTableCache.getSQL( mappedStatementId );
            } else {
                ParameterMappingUtil.configuration = ( Configuration ) metaObject.getValue( "configuration" );
                String processSQL = SubTable.processSQL( sql );
                StringBuilder tempSQL = SubTable.subTable( processSQL , tablePrefix );
                SubTable.cacheSQl( tempSQL.toString(),mappedStatementId );
                finalSQl = SubTableCache.getSQL( mappedStatementId );
            }
            finalParameterMappings = ParameterMappingUtil.copyByParamList( paramterMapperings , mappedStatementId );
            metaObject.setValue( BOUND_SQL,finalSQl );
            metaObject.setValue( PARAMETER_MAPPERINGS,finalParameterMappings );
            log.info( "分表后 SQL【"+finalSQl+"】" );
        }
        return Plugin.wrap( o,this );
    }

    @Override
    public void setProperties(Properties properties){
        this.properties = properties;
    }
}
