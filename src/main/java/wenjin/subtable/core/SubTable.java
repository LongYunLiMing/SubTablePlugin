package wenjin.subtable.core;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import java.util.*;

/**
 * @Author 玟瑾
 * @Create 2019-11-05 17:16
 * @Email 1924567147@qq.com
 * @Description SQL 处理工具类
 */
public class SubTable{

    public static StringBuilder subTable(String oldSql,  String tablePrefix){
        SQLStatement sqlStatement = new MySqlStatementParser( oldSql ).parseStatement();
        StringBuilder sql = new StringBuilder();
        if ( sqlStatement instanceof SQLSelectStatement ){
            SQLWithSubqueryClause withSubQuery = ( ( SQLSelectStatement ) sqlStatement ).getSelect().getWithSubQuery();
            if ( withSubQuery != null ){
                List< SQLWithSubqueryClause.Entry > entries = withSubQuery.getEntries();
                sql.append( "\nWITH\n" );
                for ( int i = 0 ; i < entries.size() ; i++ ){
                    SQLWithSubqueryClause.Entry entry = entries.get( i );
                    if ( i != 0 )
                        sql.append( ",\n" );
                    fromKeyWord( entry,sql,tablePrefix );
                }
            }
            select( ((SQLSelectStatement)sqlStatement).getSelect().getQuery(), sql,tablePrefix);
        }
        return sql;
    }

    /**
     * 查询 SQL 的处理入口
     * @param query
     * @param sql
     * @param tablePrefix
     * @return
     */
    public static void select(SQLSelectQuery query, StringBuilder sql,String tablePrefix){
        if ( SQLSelectQueryBlock.class.equals( query.getClass() ) || MySqlSelectQueryBlock.class.equals( query.getClass() ) ){
            sqlSelectQueryBlocak((SQLSelectQueryBlock)query,sql,tablePrefix);
        } else if ( SQLUnionQuery.class.equals( query.getClass() ) ){
            sqlUnionQuery((SQLUnionQuery)query,sql,tablePrefix);
        }
    }

    /**
     * 处理 UNION ALL 的查询语句
     * @param query
     * @param sql
     * @param tablePrefix
     */
    private static void sqlUnionQuery(SQLUnionQuery query , StringBuilder sql , String tablePrefix){
        SQLSelectQuery left = query.getLeft();
        SQLSelectQuery right = query.getRight();
        SQLLimit limit = query.getLimit();
        SQLUnionOperator operator = query.getOperator();
        SQLOrderBy orderBy = query.getOrderBy();
        StringBuilder leftSQL = new StringBuilder();
        select( left, leftSQL,tablePrefix);
        sql.append( leftSQL );
        sql.append( "\n"+operator.name+"\n" );
        StringBuilder rightSQL = new StringBuilder();
        select( right, rightSQL,tablePrefix);
        sql.append( rightSQL );
        if ( orderBy != null ){
            sql.append( "\nORDER BY " );
            for ( int i = 0 ; i < orderBy.getItems().size() ; i++ ){
                SQLSelectOrderByItem orderByItem = orderBy.getItems().get( i );
                if ( i != 0 ){
                    sql.append( "," );
                }
                whereKeyWord( orderByItem.getExpr(),sql,tablePrefix,false );
            }
        }
        if ( limit != null ){
            SQLExpr offset = limit.getOffset();
            SQLExpr rowCount = limit.getRowCount();
            sql.append( "\nLIMIT " );
            whereKeyWord( offset,sql,tablePrefix ,false);
            sql.append( "," );
            whereKeyWord( rowCount,sql,tablePrefix ,false);
        }
    }

    /**
     * 处理普通的查询语句
     * @param query
     * @param sql
     * @param tablePrefix
     * @return
     */
    private static void sqlSelectQueryBlocak(SQLSelectQueryBlock query , StringBuilder sql , String tablePrefix){
        List< SQLSelectItem > selectList = query.getSelectList();
        String currentTablePrefix = null;
        if ( selectList.size()>0 ){
            sql.append( "SELECT\n" );
            selectKeyWord( selectList,sql,tablePrefix );
        }

        SQLTableSource from = query.getFrom();
        if ( from != null ){
            sql.append( "\nFROM\n" );
            currentTablePrefix = fromKeyWord( from,sql,tablePrefix );
        }

        SQLExpr where = query.getWhere();
        if ( where != null ){
            sql.append( "\nWHERE\n" );
            whereKeyWord( where,sql,tablePrefix ,false);
        }

        SQLSelectGroupByClause groupBy = query.getGroupBy();
        if ( groupBy != null &&groupBy.getItems().size()>0 ){
            sql.append( "\nGROUP BY\n" );
            for ( int i = 0 ; i < groupBy.getItems().size() ; i++ ){
                SQLExpr sqlExpr = groupBy.getItems().get( i );
                if ( i!=0 )
                    sql.append( "," );
                whereKeyWord( sqlExpr,sql,tablePrefix ,false);
            }
            SQLExpr having = groupBy.getHaving();
            if ( having != null ){
                sql.append( "\nHAVING\n" );
                whereKeyWord( having,sql,tablePrefix ,false);
            }
        }

        SQLOrderBy orderBy = query.getOrderBy();
        if ( orderBy != null ){
            List< SQLSelectOrderByItem > orderByItems = orderBy.getItems();
            if ( orderByItems.size()>0 ){
                sql.append( "\nORDER BY\n" );
                for ( int i = 0 ; i < orderByItems.size() ; i++ ){
                    SQLSelectOrderByItem order = orderByItems.get( i );
                    whereKeyWord( order.getExpr(),sql,tablePrefix ,false);
                }
            }
        }

        SQLLimit limit = query.getLimit();
        if ( limit != null ){
            sql.append( "\nLIMIT " );
            whereKeyWord( limit.getOffset(),sql,tablePrefix ,false);
            sql.append( "," );
            whereKeyWord( limit.getRowCount(),sql,tablePrefix, false );
            sql.append( "\n" );
        }

        //说明当前的查询语句中包含表前缀需要对其进行分表操作
        if ( currentTablePrefix != null ){
            List< String > tables = SubTableHelper.get( currentTablePrefix );
            String sourceSql = sql.toString();
            sql.delete( 0,sql.length() );
            for ( int i = 0 ; i < tables.size() ; i++ ){
                if ( i != 0 ){
                    sql.append( "\nUNION ALL\n" );
                }
                String table = tables.get( i );
                sql.append( sourceSql.replace( currentTablePrefix,table ) );
            }
        }
    }

    /**
     * select 关键字部分的 SQL 处理
     * @param selectList
     * @param sql
     * @param tablePrefix
     * @return
     */
    private static void selectKeyWord(List< SQLSelectItem > selectList,StringBuilder sql,String tablePrefix){
        for ( int i = 0 ; i < selectList.size() ; i++ ){
            if ( i != 0 )
                sql.append( ",\n" );
            SQLSelectItem selectItem = selectList.get( i );
            SQLExpr expr = selectItem.getExpr();
            whereKeyWord( expr,sql,tablePrefix ,true);
            if ( selectItem.getAlias() != null ){
                sql.append( " AS " );
                sql.append( selectItem.getAlias() );
            }
        }
    }

    /**
     * from 关键字部分的 SQL 处理
     * @param from
     * @param sql
     * @param tablePrefix
     * @return
     */
    private static String fromKeyWord(SQLTableSource from ,StringBuilder sql, String tablePrefix){
        Class< ? extends SQLTableSource > fromClass = from.getClass();
        if ( SQLExprTableSource.class.equals( fromClass ) ){
            SQLExprTableSource tableSource = ( SQLExprTableSource ) from;
            String tableName = tableSource.getExpr().toString();
            String alias = tableSource.getAlias();
            sql.append( tableName );
            if ( alias != null ){
                sql.append( " " );
                sql.append( alias );
            }
            if ( tableName.contains( tablePrefix ) )
                return tableName;
            return null;
        } else if ( SQLJoinTableSource.class.equals( fromClass ) ){
            SQLJoinTableSource tableSource = ( SQLJoinTableSource ) from;
            SQLTableSource left = tableSource.getLeft();
            SQLJoinTableSource.JoinType joinType = tableSource.getJoinType();
            SQLTableSource right = tableSource.getRight();
            String alias = tableSource.getAlias();
            SQLExpr condition = tableSource.getCondition();
            fromKeyWord( left,sql,tablePrefix );
            sql.append( "\n" );
            sql.append( joinType.name );
            sql.append( "\n" );
            fromKeyWord( right,sql,tablePrefix );
            sql.append( "\n" );
            sql.append( "ON " );
            whereKeyWord( condition,sql,tablePrefix ,false);
            sql.append( alias==null?"":" "+alias );
        } else if ( SQLSubqueryTableSource.class.equals( fromClass ) ){
            SQLSubqueryTableSource tableSource = ( SQLSubqueryTableSource ) from;
            SQLSelect select = tableSource.getSelect();
            String alias = tableSource.getAlias();
            StringBuilder newSql = new StringBuilder();
            select( select.getQuery(), newSql,tablePrefix);
            sql.append( "(\n" );
            sql.append( newSql );
            sql.append( ")\n" );
            sql.append( alias==null?"":" "+alias );
        } else if ( SQLUnionQueryTableSource.class.equals( fromClass ) ){
            SQLUnionQueryTableSource tableSource = ( SQLUnionQueryTableSource ) from;
            SQLUnionQuery union = tableSource.getUnion();
            String alias = tableSource.getAlias();
            StringBuilder newSql = new StringBuilder();
            select( union,newSql,tablePrefix );
            sql.append( "(\n" );
            sql.append( newSql );
            sql.append( ")\n" );
            sql.append( alias==null?"":" "+alias );
        } else if ( SQLValuesTableSource.class.equals( fromClass ) ){
            SQLValuesTableSource tableSource = ( SQLValuesTableSource ) from;
            //TODO
        } else if ( SQLWithSubqueryClause.Entry.class.equals( fromClass ) ){
            SQLWithSubqueryClause.Entry entry = ( SQLWithSubqueryClause.Entry ) from;
            SQLSelect subQuery = entry.getSubQuery();
            String alias = entry.getAlias();
            sql.append( alias );
            sql.append( " AS" );
            sql.append( "\n(\n" );
            StringBuilder newSql = new StringBuilder();
            select( subQuery.getQuery(), newSql,tablePrefix);
            sql.append( newSql );
            sql.append( "\n)\n" );
        }
        return null;
    }

    /**
     * where 关键字部分的 SQL 处理
     * @param where
     * @param sql
     * @param tablePrefix
     * @return
     */
    public static void whereKeyWord(SQLExpr where , StringBuilder sql, String tablePrefix,boolean isSelect){
        if ( where == null )
            return;
        Class< ? extends SQLExpr > whereClass = where.getClass();
        if ( SQLIdentifierExpr.class.equals( where.getClass() ) ){
            //例如 id = 1 中的 id
            SQLIdentifierExpr expr = ( SQLIdentifierExpr ) where;
            Where.identifierExpr( expr,sql,isSelect );
        } else if ( SQLPropertyExpr.class.equals( whereClass ) ){
            //例如 a.id = 1 中的 a.id
            SQLPropertyExpr expr = ( SQLPropertyExpr ) where;
            Where.propertyExpr( expr,sql,tablePrefix );
        } else if ( SQLBinaryOpExpr.class.equals( whereClass ) ){
            //例如 id = 1
            SQLBinaryOpExpr expr = ( SQLBinaryOpExpr ) where;
            Where.binaryOpExpr( expr,sql,tablePrefix );
        } else if ( SQLVariantRefExpr.class.equals( whereClass ) ){
            //例如 id = ?
            SQLVariantRefExpr expr = ( SQLVariantRefExpr ) where;
            Where.variantRefExpr( expr,sql );
        } else if ( SQLIntegerExpr.class.equals( whereClass ) ){
            //例如 id = 1
            SQLIntegerExpr expr = ( SQLIntegerExpr ) where;
            Where.integerExpr( expr,sql );
        } else if ( SQLCharExpr.class.equals( whereClass ) ){
            SQLCharExpr expr = ( SQLCharExpr ) where;
            Where.charExpr( expr,sql );
        } else if ( SQLMethodInvokeExpr.class.equals( whereClass ) ){
            SQLMethodInvokeExpr expr = ( SQLMethodInvokeExpr ) where;
            Where.methodInvokeExpr( expr,sql,tablePrefix );
        } else if ( SQLAggregateExpr.class.equals( whereClass ) ){
            //聚合表达式
            SQLAggregateExpr expr = ( SQLAggregateExpr ) where;
            Where.aggregateExpr( expr,sql,tablePrefix );
        } else if ( SQLCaseExpr.class.equals( whereClass ) ){
            SQLCaseExpr expr = ( SQLCaseExpr ) where;
            Where.caseExpr( expr,sql,tablePrefix );
        } else if ( SQLBetweenExpr.class.equals( whereClass ) ){
            SQLBetweenExpr expr = ( SQLBetweenExpr ) where;
            Where.betweenExpr( expr,sql,tablePrefix );
        } else if ( SQLInListExpr.class.equals( whereClass ) ){
            SQLInListExpr expr = ( SQLInListExpr ) where;
            Where.inListExpr( expr,sql,tablePrefix );
        } else if ( SQLInSubQueryExpr.class.equals( whereClass ) ){
            SQLInSubQueryExpr expr = ( SQLInSubQueryExpr ) where;
            Where.inSubQueryExpr( expr,sql,tablePrefix );
        } else if ( SQLAllColumnExpr.class.equals( whereClass ) ){
            SQLAllColumnExpr expr = ( SQLAllColumnExpr ) where;//所有列 *
            Where.allColumnExpr( expr,sql );
        } else if ( SQLAllExpr.class.equals( whereClass ) ){
            SQLAllExpr expr = ( SQLAllExpr ) where;
            Where.allExpr( expr,sql,tablePrefix );
        } else if ( SQLListExpr.class.equals( whereClass ) ){
            SQLListExpr expr = ( SQLListExpr ) where;
            Where.listExpr( expr,sql,tablePrefix );
        } else if ( SQLAnyExpr.class.equals( whereClass ) ){
            SQLAnyExpr expr = ( SQLAnyExpr ) where;
            Where.anyExpr( expr,sql,tablePrefix );
        } else if ( SQLSomeExpr.class.equals( whereClass ) ){
            SQLSomeExpr expr = ( SQLSomeExpr ) where;
            Where.someExpr( expr,sql,tablePrefix );
        } else if ( SQLArrayExpr.class.equals( whereClass ) ){
            SQLArrayExpr expr = ( SQLArrayExpr ) where;
            Where.arrayExpr( expr,sql,tablePrefix );
        } else if ( SQLBooleanExpr.class.equals( whereClass ) ){
            SQLBooleanExpr expr = ( SQLBooleanExpr ) where;
            Where.booleanExpr( expr,sql,tablePrefix );
        } else if ( SQLNullExpr.class.equals( whereClass ) ){
            SQLNullExpr expr = ( SQLNullExpr ) where;
            Where.nullExpr( expr,sql,tablePrefix );
        } else if ( SQLNotExpr.class.equals( whereClass ) ){
            SQLNotExpr expr = ( SQLNotExpr ) where;
            Where.notExpr( expr,sql,tablePrefix );
        } else if ( SQLDateExpr.class.equals( whereClass ) ){
            SQLDateExpr expr = ( SQLDateExpr ) where;
            Where.dateExpr( expr,sql,tablePrefix );
        } else if ( SQLExistsExpr.class.equals( whereClass ) ){
            SQLExistsExpr expr = ( SQLExistsExpr ) where;
            Where.existsExpr( expr,sql,tablePrefix );
        } else if ( SQLCastExpr.class.equals( whereClass ) ){
            SQLCastExpr expr = ( SQLCastExpr ) where;
            Where.castExpr( expr,sql,tablePrefix );
        }
    }

    /**
     * 计算字符串中目标字符串的数量
     * @param origin
     * @param target
     * @return 目标字符串的数量
     */
    private static int calculateTargetNumber(String origin,String target){
        if ( !origin.contains( target ) )
            return 0;
        String temp = origin;
        int result = 0;
        while ( true ){
            int index = temp.indexOf( target );
            if ( index == -1 )
                return result;
            temp = temp.substring( index+target.length() );
            result ++;
        }
    }

    /**
     * 将 SQL 语句中的 ？占位符加上序号
     * @param sql 源 SQL
     * @return
     */
    public static String processSQL(String sql){
        StringBuilder newSql = new StringBuilder( sql.length() );
        int item = 0;
        for ( int i = 0 ; i < sql.length() ; i++ ){
            char c = sql.charAt( i );
            if ( '?' == c ){
                newSql.append( "'"+item+"?'" );
                item ++;
            } else {
                newSql.append( c );
            }
        }
        return newSql.toString();
    }

    /**
     * 解析 SQL 剔除 ？前面的序号作为最终 SQL 放入缓存中
     * 将 ？参数集合维护到缓存中，便于下次使用
     * @param newSQL
     * @param id MappedStatement 的 ID 标示唯一的一条 SQL
     * @return
     */
    public static void cacheSQl(String newSQL,String id){
        boolean flag = false;
        StringBuilder sql = new StringBuilder();
        List<Integer> paramList = new ArrayList<>();
        for ( int i = 0 ; i < newSQL.length() ; i++ ){
            char c = newSQL.charAt( i );
            if ( flag && '\'' == c ){
                flag = false;
                continue;
            }
            if ( '?' == c ){
                int j = i-1;
                if ( newSQL.substring( i-5,i ).contains( "'" ) )
                    flag = true;
                if ( flag ){
                    while ( true ){
                        if ( newSQL.charAt( j ) == '\'')
                            break;
                        j --;
                    }
                } else {
                    while ( true ){
                        if (  newSQL.charAt( j ) > '9' || '0' > newSQL.charAt( j ))
                            break;
                        j --;
                    }
                }
                String key = newSQL.substring( j+1,i+1 );
                paramList.add( Integer.valueOf( key.substring( 0,key.length()-1 ) ) );
                sql = sql.delete( sql.length()-(i-j),sql.length() );
            }
            sql.append( c );
        }
        SubTableCache.putSQL( id,sql.toString() );
        SubTableCache.putParamList( id,paramList );
    }
}
