package cn.instree.core.anno.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:标识该字段无对应的数据库，表列
 * @author shizy
 * @createDateTime 
 * @version  
 * @Company: MSD. 
 * @Copyright: Copyright (c) 2013
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {
}
