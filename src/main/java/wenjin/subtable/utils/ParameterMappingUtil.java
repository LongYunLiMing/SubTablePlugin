package wenjin.subtable.utils;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import wenjin.subtable.core.SubTableCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 玟瑾
 * @Create 2019-11-06 10:13
 * @Email 1924567147@qq.com
 * @Description
 */
public class ParameterMappingUtil{
    public static Configuration configuration;

    public static ArrayList< ParameterMapping > copy(ArrayList<ParameterMapping> parameterMappings
                                                ,int start,int end){
        ArrayList<ParameterMapping> mappings = new ArrayList<>();
        for ( int i = start ; i < end ; i++ ){
            mappings.add( copy( parameterMappings.get( i ) ) );
        }
        parameterMappings.addAll( end,mappings );
        return parameterMappings;
    }

    public static ParameterMapping copy(ParameterMapping mapping){
        ParameterMapping parameterMapping = new ParameterMapping.Builder(configuration,mapping.getProperty(),mapping.getJavaType())
                .expression( mapping.getExpression() ).jdbcType( mapping.getJdbcType() ).jdbcTypeName( mapping.getJdbcTypeName() )
                .mode( mapping.getMode() ).numericScale( mapping.getNumericScale() ).resultMapId( mapping.getResultMapId() )
                .typeHandler( mapping.getTypeHandler() ).build();
        return parameterMapping;
    }

    public static List<ParameterMapping> copyByParamList(List<ParameterMapping> sourceParamterMapping,String id){
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        List< Integer > paramList = SubTableCache.getParamList( id );
        paramList.forEach( s-> parameterMappings.add( copy( sourceParamterMapping.get( s ) ) ));
        return parameterMappings;
    }
}
