package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */

    Integer countByCategoryId(Long id);

    void update(Setmeal setmeal);


    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);
}
