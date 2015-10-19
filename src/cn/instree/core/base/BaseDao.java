package cn.instree.core.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
import javax.annotation.Resource;
 
import org.apache.commons.lang.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
 
import cn.instree.util.GenerateSequenceUtil;
import cn.instree.util.GenericsUtils;
import cn.instree.util.ReflectionUtils;
import cn.instree.util.SQLGenerator;
import cn.instree.core.anno.base.Ignore;
import cn.instree.core.anno.base.PrimaryKey;
import cn.instree.core.anno.base.Table;
import cn.instree.core.anno.base.TableColumn;

@Repository("BaseDao")
public abstract class BaseDao<T, PK extends Serializable> implements IBaseDao<T, PK> {
	
	private static final int FLUSH_CRITICAL_VAL = 100;
     
    @Resource(name = "sessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate;
     
    private Class<T> entityClass;
    //实体类主键名称
    private String pkName;
    //实体类ID字段名称
    private String idName;
    //主键的生成策略
    private String generator;
    private String tableName;
    /**
     * 作cache 结构{T类的镜像,{数据库列名,实体字段名}}
     */
    private static final Map<Class<?>, Map<String, String>> classFieldMap = new HashMap<Class<?>, Map<String, String>>();
    private Map<String, String> currentColumnFieldNames;
     
    private SQLGenerator<T> sqlGenerator;
     
    
    @SuppressWarnings("unchecked")
    public BaseDao() {
    	
		this.entityClass = (Class<T>) GenericsUtils.getSuperClassGenricType(this.getClass());
	    
	    currentColumnFieldNames = classFieldMap.get(entityClass);
	    if (null == currentColumnFieldNames) {
	        currentColumnFieldNames = new LinkedHashMap<String, String>();
	        classFieldMap.put(entityClass, currentColumnFieldNames);
	    }
	     
	    // 作cache
	    Field[] fields = this.entityClass.getDeclaredFields();
	     
	    String fieldName = null;
	    String columnName = null;
	    for (Field field : fields) {
	        if (field.isAnnotationPresent(Ignore.class)) {
	            continue;
	        }
	        fieldName = field.getName();
	        TableColumn tableColumn = field.getAnnotation(TableColumn.class);
	        if (null != tableColumn) {
	            columnName = tableColumn.value();
	        } else {
	            columnName = null;
	        }
	        // 如果未标识特殊的列名，默认取字段名
	        columnName = (StringUtils.isEmpty(columnName) ? fieldName : columnName);
	        currentColumnFieldNames.put(columnName, fieldName);
	        if (field.isAnnotationPresent(PrimaryKey.class)) {
	            // 取得ID的列名
	            idName = fieldName;
	            pkName = columnName;
	            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
	            generator = primaryKey.generator();
	            if("native".equals(generator)) {
	            	
	            	currentColumnFieldNames.remove(pkName);
	            }
	        }
	    }
	     
	    Table table = this.entityClass.getAnnotation(Table.class);
	    if (null == table) { throw new RuntimeException("类-"
	            + this.entityClass + ",未用@Table注解标识!!"); }
	    tableName = table.value();
	     
	    sqlGenerator = new SQLGenerator<T>(currentColumnFieldNames.keySet(),
	            tableName,pkName,idName);
        
    }
     
     
    @Override
    public void save(T t) {
    	//没有使用自增序列
        if(StringUtils.isEmpty(generator)){
        	Object obj = ReflectionUtils.invokeGetterMethod(t, idName);
        	if(obj == null) {
        		String seq = GenerateSequenceUtil.generateSequenceNo();
                ReflectionUtils.invokeSetterMethod(t, idName, seq);
        	}
        }
        this.create(t);
    }
 
    @Override
    public void saveOfBatch(List<T> tList) {
        if(null == tList || tList.isEmpty()){
            return;
        }
        if(StringUtils.isEmpty(generator)){
        	for(T t : tList){
        		Object obj = ReflectionUtils.invokeGetterMethod(t, idName);
            	if(obj == null) {
            		String seq = GenerateSequenceUtil.generateSequenceNo();
                    ReflectionUtils.invokeSetterMethod(t, idName, seq);
            	}
            }
        }
        
         
        this.createOfBatch(tList);
    }
 
    @Override
    public void removeById(PK id) {
        sqlSessionTemplate.delete("removeById",sqlGenerator.sql_removeById(id));
    }
     
    @Override
    public void removeOfBatch(List<PK> ids) {
        if(null == ids || ids.isEmpty()){
            return;
        }
        int len = ids.size(), i = 0;
        List<PK> temp = new ArrayList<PK>();
        for (; i < len; i++) {
            temp.add(ids.get(i));
            if (i > 0 && i % FLUSH_CRITICAL_VAL == 0) {
                sqlSessionTemplate.delete("removeOfBatch",
                        sqlGenerator.sql_removeOfBatch(temp));
                sqlSessionTemplate.flushStatements();
                temp = new ArrayList<PK>();
            }
        }
        sqlSessionTemplate.delete("removeOfBatch",
                sqlGenerator.sql_removeOfBatch(temp));
    }
     
    @Override
    public void removeAll(){
        sqlSessionTemplate.delete("removeAll",
                sqlGenerator.sql_removeAll());
    }
     
     
    @Override
    public void modify(T t) {
        sqlSessionTemplate.update("modify",
                sqlGenerator.sql_modify(t, currentColumnFieldNames));
         
    }
     
    @Override
    public void modifyOfBatch(List<T> tList) {
        if(null == tList || tList.isEmpty()){
            return;
        }
        int len = tList.size(),i=0;
        for(;i<len;i++){
            this.modify(tList.get(i));
            if (i > 0 && i % FLUSH_CRITICAL_VAL == 0) {
                sqlSessionTemplate.flushStatements();
            }
        }
    }
     
    @Override
    public T findOneById(PK id) {
        Map<String, Object> resultMap = sqlSessionTemplate.selectOne(
                "findOneById", sqlGenerator.sql_findOneById(id));
         
        return handleResult(resultMap, this.entityClass);
    }
     
    @Override
    public List<T> findAll() {
        List<Map<String, Object>> resultMapList = sqlSessionTemplate
                .selectList("findAll", sqlGenerator.sql_findAll());
        List<T> tList = new ArrayList<T>(resultMapList.size());
        for (Map<String, Object> resultMap : resultMapList) {
            T t = handleResult(resultMap, this.entityClass);
            tList.add(t);
        }
        return tList;
    }
     
    @Override
    public Long findAllCount() {
        Long count = sqlSessionTemplate.selectOne("findAllCount", sqlGenerator.sql_findAllCount());
        return count;
    }


	private T handleResult(Map<String, Object> resultMap, Class<T> tClazz) {
        T t = null;
        try {
            t = tClazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            String key = entry.getKey();
            key = currentColumnFieldNames.get(key);
            Object val = entry.getValue();
            ReflectionUtils.invokeSetterMethod(t, key, val);
        }
        return t;
    }
    
    private void create(T t) {
        sqlSessionTemplate.insert("create",sqlGenerator.sql_create(t, currentColumnFieldNames));
    }
    
    private void createOfBatch(List<T> tList) {
        if(null == tList || tList.isEmpty()){
            return;
        }
        int len = tList.size(), i = 0;
        List<T> temp = new ArrayList<T>();
        
        for (; i < len; i++) {
            T t = tList.get(i);
            temp.add(t);
            if (i > 0 && i % FLUSH_CRITICAL_VAL == 0) {
                sqlSessionTemplate.insert("createOfBatch", sqlGenerator
                        .sql_createOfBatch(temp, currentColumnFieldNames));
                sqlSessionTemplate.flushStatements();
                temp = new ArrayList<T>();
            }
        }
        sqlSessionTemplate.insert("createOfBatch", sqlGenerator.sql_createOfBatch(temp, currentColumnFieldNames));
    }
     
}