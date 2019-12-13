# SubTablePlugin
  一个用于 Mybatis 的分表插件，使其能够让多张【相同表结构不同表名】的表之间实现灵活的联合查询
  当在同一个数据库中存在表结构相同但表名不同的情况下，就可以尝试使用分表插件。例如在一些项目中在进行数据归档时同样的数据可能会因为数据量过大而根据月份来进行分表存储时。
  
# 使用步骤
## 1、Maven 依赖

  上传中 。。。。。。。。。。。。。。。。

## 2、mybatis 配置文件中配置插件

<plugins>
    <plugin interceptor="wenjin.subtable.core.SubTabelPlugin">
        <property name="tablePrefix" value="@"/>
    </plugin>
</plugins>
tablePrefix 表前缀，该配置项需要指定特殊字符串，否则不需要分表的 SQL 语句也会被作用。

## 3、编写 SQL 语句
  在编写 SQL 语句中 from 后面就不需要指定特定的表名了，而是提供一个 key ，该 key 由 tablePrefix 表前缀和一个字符串组成【例如：@table】
 
 select * from @table
 
## 4、添加表策略
  sg.core.subtable.utils.SubTableHelper.addStrategy(String key,ICalculateStrategy strategy)
  key ：是 SQL 语句中使用的 key；
  strategy ：是一个函数式接口，用于提供当前 key 需要 UNION ALL 查询的表名集合，这里的表名集合就需要是数据库中真是存在的。
  
  例如：
  SubTableHelper.addStrategy( "@TABLE",()->Arrays.asList( "table1,table2" ) );
  
## 5、分表后的最终执行 SQL
  sub_table_plugin 分表插件会在任何使用到 key 的查询中将所有的表进行 UNION ALL 连接。sub_table_plugin 支持在任何嵌套子查询的地方使用，无论子查询是在 select 中还是 from 中还是 where 中都会根据 key 进行解析处理
  
  select * from table1 
  UNION ALL 
  select * from table2
