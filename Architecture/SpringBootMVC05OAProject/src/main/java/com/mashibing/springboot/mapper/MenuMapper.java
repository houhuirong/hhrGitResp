package com.mashibing.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashibing.springboot.entity.Menu;
import com.mashibing.springboot.mapper.MenuExample;
import org.springframework.stereotype.Repository;

/**
 * MenuMapper继承基类
 */
@Repository
public interface MenuMapper extends BaseMapper<Menu> {
}