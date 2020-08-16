package com.xiaoshu.dao;

import com.xiaoshu.base.dao.BaseMapper;
import com.xiaoshu.entity.School;
import com.xiaoshu.entity.SchoolExample;
import com.xiaoshu.entity.SchoolVo;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SchoolMapper extends BaseMapper<School> {

	List<SchoolVo> findList(SchoolVo sv);
}