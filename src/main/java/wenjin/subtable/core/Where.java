package wenjin.subtable.core;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLSelect;

import java.util.List;

import static wenjin.subtable.core.SubTable.select;
import static wenjin.subtable.core.SubTable.whereKeyWord;

/**
 * @Author 玟瑾
 * @Create 2019-12-11 10:45
 * @Email 1924567147@qq.com
 * @Description
 */
public class Where{

    public static void identifierExpr(SQLIdentifierExpr expr,StringBuilder sql,boolean isSelect){
        String name = expr.getName();
        if ( isSelect ){
            if ( isExitDistinct(expr,name) ){
                sql.append( "DISTINCT " );
            }
        }
        sql.append( name );
    }

    public static void propertyExpr(SQLPropertyExpr expr, StringBuilder sql, String tablePrefix){
        String name = expr.getName();//例子中的 id
        SQLExpr owner = expr.getOwner();//例子中的 a
        whereKeyWord( owner,sql,tablePrefix,false );
        sql.append( "." );
        sql.append( name );
    }

    public static void binaryOpExpr(SQLBinaryOpExpr expr, StringBuilder sql, String tablePrefix){
        SQLExpr left = expr.getLeft();//例子中的 id
        SQLBinaryOperator operator = expr.getOperator();//例子中的 =
        SQLExpr right = expr.getRight();//例子中的 1
        whereKeyWord( left,sql,tablePrefix ,false);
        sql.append( " "+operator.name+" " );
        whereKeyWord( right,sql,tablePrefix ,false);
    }

    public static void variantRefExpr(SQLVariantRefExpr expr, StringBuilder sql){
        String name = expr.getName();//例子中的 ？
        sql.append( name );
    }

    public static void integerExpr(SQLIntegerExpr  expr, StringBuilder sql){
        Object value = expr.getValue();//例子中的 1
        sql.append( value.toString() );
    }

    public static void charExpr(SQLCharExpr expr, StringBuilder sql){
        String text = expr.getText();
        sql.append( "'" );
        sql.append( text );
        sql.append( "'" );
    }

    public static void methodInvokeExpr(SQLMethodInvokeExpr expr,StringBuilder sql,String tablePrefix){
        String methodName = expr.getMethodName();
        List< SQLExpr > arguments = expr.getArguments();
        sql.append( methodName );
        sql.append( "(" );
        for ( int i = 0 ; i < arguments.size() ; i++ ){
            SQLExpr sqlExpr = arguments.get( i );
            if ( i != 0 )
                sql.append( "," );
            whereKeyWord( sqlExpr,sql,tablePrefix ,false);
        }
        sql.append( ")" );
    }

    public static void aggregateExpr(SQLAggregateExpr expr,StringBuilder sql,String tablePrefix){
        String methodName = expr.getMethodName();
        List< SQLExpr > arguments = expr.getArguments();
        sql.append( methodName );
        sql.append( "(" );
        for ( int i = 0 ; i < arguments.size() ; i++ ){
            SQLExpr sqlExpr = arguments.get( i );
            if ( i != 0 )
                sql.append( "," );
            whereKeyWord( sqlExpr,sql,tablePrefix ,false);
        }
        sql.append( ")" );
    }

    public static void caseExpr(SQLCaseExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr elseExpr = expr.getElseExpr();
        List< SQLCaseExpr.Item > items = expr.getItems();
        SQLExpr valueExpr = expr.getValueExpr();
        sql.append( "CASE " );
        whereKeyWord( valueExpr,sql,tablePrefix ,false);
        for ( int i = 0 ; i < items.size() ; i++ ){
            sql.append( "\n" );
            SQLCaseExpr.Item item = items.get( i );
            SQLExpr conditionExpr = item.getConditionExpr();
            SQLExpr itemValueExpr = item.getValueExpr();
            sql.append( "WHEN " );
            whereKeyWord( conditionExpr,sql,tablePrefix ,false);
            sql.append( " THEN " );
            whereKeyWord( itemValueExpr,sql,tablePrefix ,false);
        }
        sql.append( "\n" );
        sql.append( "ELSE " );
        whereKeyWord( elseExpr,sql,tablePrefix ,false);
        sql.append( " END " );
    }

    public static void betweenExpr(SQLBetweenExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr beginExpr = expr.getBeginExpr();
        SQLExpr endExpr = expr.getEndExpr();
        sql.append( "BEGIN " );
        whereKeyWord( beginExpr,sql,tablePrefix, false );
        sql.append( "AND " );
        whereKeyWord( endExpr,sql,tablePrefix,false );
    }

    public static void inListExpr(SQLInListExpr expr,StringBuilder sql,String tablePrefix){
        List< SQLExpr > targetList = expr.getTargetList();
        SQLExpr expr1 = expr.getExpr();
        whereKeyWord( expr1,sql,tablePrefix,false );
        sql.append( " IN (" );
        for ( int i = 0 ; i < targetList.size() ; i++ ){
            SQLExpr expr2 = targetList.get( i );
            if ( i != 0 )
                sql.append( "," );
            whereKeyWord( expr2,sql,tablePrefix,false );
        }
        sql.append( ")" );
    }

    public static void inSubQueryExpr(SQLInSubQueryExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr expr1 = expr.getExpr();
        SQLSelect subQuery = expr.getSubQuery();
        whereKeyWord( expr1,sql,tablePrefix ,false);
        sql.append( " IN(" );
        StringBuilder newSql = new StringBuilder();
        select( subQuery.getQuery(), newSql,tablePrefix);
        sql.append( newSql );
        sql.append( ")" );
    }

    public static void allColumnExpr(SQLAllColumnExpr expr,StringBuilder sql){
        sql.append( expr.toString() );
    }

    public static void allExpr(SQLAllExpr expr,StringBuilder sql,String tablePrefix ){
        SQLSelect subQuery = expr.getSubQuery();
        sql.append( "ALL" );
        sql.append( "(" );
        StringBuilder newSql = new StringBuilder();
        select( subQuery.getQuery(),newSql,tablePrefix );
        sql.append( newSql );
        sql.append( ")" );
    }

    public static void listExpr(SQLListExpr expr,StringBuilder sql,String tablePrefix){
        List< SQLExpr > items = expr.getItems();
        if ( items.size()>0 ){
            sql.append( "(" );
            for ( int i = 0 ; i < items.size() ; i++ ){
                if ( i != 0 )
                    sql.append( "," );
                whereKeyWord( items.get( i ),sql,tablePrefix,false );
            }
            sql.append( ")" );
        }
    }

    public static void anyExpr(SQLAnyExpr expr,StringBuilder sql,String tablePrefix){
        SQLSelect subQuery = expr.getSubQuery();
        sql.append( "ANY" );
        sql.append( "(" );
        StringBuilder newSql = new StringBuilder();
        select( subQuery.getQuery(),newSql,tablePrefix );
        sql.append( newSql );
        sql.append( ")" );
    }

    public static void someExpr(SQLSomeExpr expr,StringBuilder sql,String tablePrefix){
        SQLSelect subQuery = expr.getSubQuery();
        sql.append( "SOME" );
        sql.append( "(" );
        StringBuilder newSql = new StringBuilder();
        select( subQuery.getQuery(),newSql,tablePrefix );
        sql.append( newSql );
        sql.append( ")" );
    }

    public static void arrayExpr(SQLArrayExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr expr1 = expr.getExpr();
        List< SQLExpr > values = expr.getValues();
        //TODO 待实验
    }

    public static void booleanExpr(SQLBooleanExpr expr,StringBuilder sql,String tablePrefix){
        Boolean value = expr.getValue();
        sql.append( value );
    }

    public static void nullExpr(SQLNullExpr expr,StringBuilder sql,String tablePrefix){
        sql.append( "NULL" );
    }

    public static void notExpr(SQLNotExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr expr1 = expr.getExpr();
        sql.append( "NOT " );
        whereKeyWord( expr1,sql,tablePrefix ,false);
    }

    public static void dateExpr(SQLDateExpr expr,StringBuilder sql,String tablePrefix){
        String value = expr.getValue();
        sql.append( value );
        //TODO 待实验
    }

    public static void existsExpr(SQLExistsExpr expr,StringBuilder sql,String tablePrefix){
        SQLSelect subQuery = expr.getSubQuery();
        sql.append( "EXISTS" );
        sql.append( "(" );
        StringBuilder newSql = new StringBuilder();
        select( subQuery.getQuery(),newSql,tablePrefix );
        sql.append( newSql );
        sql.append( ")" );
    }

    public static void castExpr(SQLCastExpr expr,StringBuilder sql,String tablePrefix){
        SQLExpr expr1 = expr.getExpr();
        SQLDataType dataType = expr.getDataType();
        sql.append( "CAST(" );
        whereKeyWord( expr1,sql,tablePrefix,false );
        sql.append( " AS " );
        sql.append( dataType.getName() );
        sql.append( ")" );
    }

    /**
     * 判断目标列前是否存在 DISTINCT 关键字
     * @param expr
     * @param columnName
     * @return
     */
    private static boolean isExitDistinct(SQLExpr expr , String columnName){
        String sql = expr.getParent().getParent().toString();
        if ( !sql.contains( "DISTINCT" ) )
            return false;
        int distinct = sql.indexOf( "DISTINCT" )+8;
        int index = sql.indexOf( columnName );
        if ( distinct>index )
            return false;
        String substring = sql.substring( distinct , index );
        for ( int i = 0 ; i < substring.length() ; i++ ){
            char c = substring.charAt( i );
            if ( c != ' ' && c != '\n' )
                return false;
        }
        return true;
    }
}
