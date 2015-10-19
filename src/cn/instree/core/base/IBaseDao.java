package cn.instree.core.base;

import java.io.Serializable;
import java.util.List;

 
public interface IBaseDao<T, PK extends Serializable> {
     
     
    /**
     * 新增(会将序列生成的ID,注入)
     * @param t
     */
    void save(T t);
     
    /**
     * 批量新增(会将序列生成的ID,注入)
     * @param tList
     */
    void saveOfBatch(List<T> tList);
     
    /**
     * 根据ID进行删除
     * @param id
     */
    void removeById(PK id);
     
    /**
     * 根据ids进行批量删除
     * @param ids
     */
    void removeOfBatch(List<PK> ids);
     
     
    void removeAll();
     
    /**
     * 更新,字段为空，则不进行更新
     * @param t
     */
    void modify(T t);
     
    /**
     * 批量更新
     * @param tList
     */
    void modifyOfBatch(List<T> tList);
     
    /**
     * 根据ID获取对象
     * @param id
     * @return
     */
    T findOneById(PK id);
     
    /**
     * 获取所有的对象
     * @return
     */
    List<T> findAll();
     
    /**
     * 获取记录数
     * @return
     */
    Long findAllCount();
    
}
