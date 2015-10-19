package cn.instree.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
 
import org.apache.commons.lang.StringUtils;


 
/**
 * Description: 生成查询数量的SQL
 */
public class SQLGenerator<T> {
     
	private static final DateFormat df = DateFormat.getTimeInstance(DateFormat.FULL,Locale.CHINA);
	private static final int DELETE_CRITICAL_VAL = 5;
	
    private Set<String>    columns;
    private String        tableName;
    private String        columnsStr;
    private String        pkName;
    private String idName;
     
    public SQLGenerator(Set<String> columns, String tableName, String pkName,String idName) {
        
        this.columns = columns;
        this.tableName = tableName;
        this.pkName = pkName;
        this.idName = idName;
        this.columnsStr = StringUtils.join(this.columns, ",");
    }
     
    /**
     * 生成新增的SQL
     * 
     * @param t
     * @param currentColumnFieldNames
     * @return
     */
    public String sql_create(T t, Map<String, String> currentColumnFieldNames) {
        List<Object> values = obtainFieldValues(t, currentColumnFieldNames);
         
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("INSERT INTO ").append(tableName).append("(")
                .append(columnsStr).append(")values(")
                .append(StringUtils.join(values, ",")).append(")");
        String sql = sql_build.toString();
         
         
        return sql;
    }
     
    /**
     * 生成批量新增的SQL
     * 
     * @param tList
     * @param currentColumnFieldNames
     * @return
     */
    public String sql_createOfBatch(List<T> tList,
            Map<String, String> currentColumnFieldNames) {
        StringBuilder sql_build = new StringBuilder();
        int len = tList.size(),i=0;

        sql_build.append("INSERT INTO ").append(tableName).append("(")
                .append(columnsStr).append(") VALUES");
        for (; i < len; i++) {
            T t = tList.get(i);
            List<Object> values = obtainFieldValues(t, currentColumnFieldNames);
                       
            sql_build.append("(");
            
            sql_build.append(StringUtils.join(values, ",")).append("),");
        }
         
        String sql = sql_build.toString();
        
        return sql;
    }
     
    /**
     * 提供给生成新增SQL 使用
     * 
     * @param t
     * @param currentColumnFieldNames
     * @return
     */
    private List<Object> obtainFieldValues(T t,
            Map<String, String> currentColumnFieldNames) {
        List<Object> values = new LinkedList<Object>();
        for (String column : columns) {
            Object value = ReflectionUtils.obtainFieldValue(t,
                    currentColumnFieldNames.get(column));
            
            value = handleValue(value);
            
            values.add(value);
        }
        return values;
    }
     
    /**
     * 处理value
     * 
     * @param value
     * @return
     */
    private Object handleValue(Object value) {
        if (value instanceof String) {
        	value = ((String) value).replaceAll("\'", "\\\\'");
            value = "\'" + value + "\'";
        } else if (value instanceof Date) {
            Date date = (Date) value;
            
            value = df.format(date);
           
        } else if (value instanceof Boolean) {
            Boolean v = (Boolean) value;
            value = v ? 1 : 0;
        }else if(null == value || StringUtils.isBlank(value.toString())){
            value = "''";
        }
        return value;
    }
     
    /**
     * 生成根据ID删除的SQL
     * 
     * @param id
     * @return
     */
    public <PK> String sql_removeById(PK id) {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("DELETE FROM ").append(this.tableName)
                .append(" WHERE ").append(pkName).append(" = ").append(id);
         
        String sql = sql_build.toString();
         
        return sql;
    }
     
    /**
     * 生成根据IDs批量删除的SQL
     * 
     * @param ids
     * @return
     */
    public <PK> String sql_removeOfBatch(List<PK> ids) {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("DELETE FROM ").append(this.tableName)
                .append(" WHERE ").append(pkName).append(" IN ( 0 ");
        int len = ids.size(), i = 0;
        for (; i < len; i++) {
            PK id = ids.get(i);
            sql_build.append(",").append(id);
            if (i > 0 && i % (DELETE_CRITICAL_VAL - 1) == 0) {
                sql_build.append(")").append(" OR ").append(pkName)
                        .append(" IN ( 0 ");
            }
        }
        sql_build.append(")");
         
        String sql = sql_build.toString();
         
        return sql;
    }
     
     
    public String sql_removeAll() {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("DELETE FROM ").append(this.tableName);
        String sql = sql_build.toString();
        return sql;
    }
     
    /**
     * 生成更新的SQL
     * 
     * @param t
     * @param currentColumnFieldNames
     * @return
     */
    public String sql_modify(T t, Map<String, String> currentColumnFieldNames) {
        List<String> values = obtainColumnVals(t, currentColumnFieldNames);
        Object id = ReflectionUtils.obtainFieldValue(t,idName);
        id = handleValue(id);
         
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("UPDATE ").append(tableName).append(" SET ")
                .append(StringUtils.join(values, ",")).append(" WHERE ")
                .append(pkName).append(" = ").append(id);
         
        String sql = sql_build.toString();
        
         
        return sql;
    }
     
    /**
     * 提供给生成更新SQL使用
     * 
     * @param t
     * @param currentColumnFieldNames
     * @return
     */
    private List<String> obtainColumnVals(T t,
            Map<String, String> currentColumnFieldNames) {
        List<String> colVals = new LinkedList<String>();
        for (String column : columns) {
            Object value = ReflectionUtils.obtainFieldValue(t,
                    currentColumnFieldNames.get(column));
            if (value != null && !StringUtils.equalsIgnoreCase(column, pkName)) {
                colVals.add(column + "=" + handleValue(value));
            }
        }
        return colVals;
    }
     
    /**
     * 生成根据ID查询的SQL
     * 
     * @param id
     * @return
     */
    public <PK> String sql_findOneById(PK id) {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("SELECT ").append(columnsStr).append(" FROM ")
                .append(this.tableName)
                .append(" WHERE ROWNUM = 1 AND " + pkName + " = " + id);
         
        String sql = sql_build.toString();
        
         
        return sql;
         
    }
     
    /**
     * 生成查询所有的SQL
     * 
     * @return
     */
    public String sql_findAll() {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("SELECT ").append(columnsStr).append(" FROM ")
                .append(this.tableName);
        String sql = sql_build.toString();
        
         
        return sql;
    }
     
    /**
     * 生成查询数量的SQL
     * 
     * @return
     */
    public String sql_findAllCount() {
        StringBuilder sql_build = new StringBuilder();
        sql_build.append("SELECT COUNT(1) ").append(" FROM ")
                .append(this.tableName);
        String sql = sql_build.toString();
         
         
        return sql;
    }
    
     
}
