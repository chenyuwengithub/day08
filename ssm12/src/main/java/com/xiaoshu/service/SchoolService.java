package com.xiaoshu.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.xiaoshu.dao.AreaMapper;
import com.xiaoshu.dao.SchoolMapper;
import com.xiaoshu.dao.UserMapper;
import com.xiaoshu.entity.Area;
import com.xiaoshu.entity.School;
import com.xiaoshu.entity.SchoolVo;
import com.xiaoshu.entity.User;
import com.xiaoshu.entity.UserExample;
import com.xiaoshu.entity.UserExample.Criteria;

@Service
public class SchoolService {

	
	@Autowired
	SchoolMapper sm;
	
	@Autowired
	AreaMapper am;
/*	@Autowired
	UserMapper userMapper;

	// 查询所有
	public List<User> findUser(User t) throws Exception {
		return userMapper.select(t);
	};

	// 数量
	public int countUser(User t) throws Exception {
		return userMapper.selectCount(t);
	};

	// 通过ID查询
	public User findOneUser(Integer id) throws Exception {
		return userMapper.selectByPrimaryKey(id);
	};

	// 新增
	public void addUser(User t) throws Exception {
		userMapper.insert(t);
	};

	// 修改
	public void updateUser(User t) throws Exception {
		userMapper.updateByPrimaryKeySelective(t);
	};

	// 删除
	public void deleteUser(Integer id) throws Exception {
		userMapper.deleteByPrimaryKey(id);
	};

	// 登录
	public User loginUser(User user) throws Exception {
		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		criteria.andPasswordEqualTo(user.getPassword()).andUsernameEqualTo(user.getUsername());
		List<User> userList = userMapper.selectByExample(example);
		return userList.isEmpty()?null:userList.get(0);
	};

	// 通过用户名判断是否存在，（新增时不能重名）
	public User existUserWithUserName(String userName) throws Exception {
		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(userName);
		List<User> userList = userMapper.selectByExample(example);
		return userList.isEmpty()?null:userList.get(0);
	};

	// 通过角色判断是否存在
	public User existUserWithRoleId(Integer roleId) throws Exception {
		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		criteria.andRoleidEqualTo(roleId);
		List<User> userList = userMapper.selectByExample(example);
		return userList.isEmpty()?null:userList.get(0);
	}

	public PageInfo<User> findUserPage(User user, int pageNum, int pageSize, String ordername, String order) {
		PageHelper.startPage(pageNum, pageSize);
		ordername = StringUtil.isNotEmpty(ordername)?ordername:"userid";
		order = StringUtil.isNotEmpty(order)?order:"desc";
		UserExample example = new UserExample();
		example.setOrderByClause(ordername+" "+order);
		Criteria criteria = example.createCriteria();
		if(StringUtil.isNotEmpty(user.getUsername())){
			criteria.andUsernameLike("%"+user.getUsername()+"%");
		}
		if(user.getUsertype() != null){
			criteria.andUsertypeEqualTo(user.getUsertype());
		}
		if(user.getRoleid() != null){
			criteria.andRoleidEqualTo(user.getRoleid());
		}
		List<User> userList = userMapper.selectUserAndRoleByExample(example);
		PageInfo<User> pageInfo = new PageInfo<User>(userList);
		return pageInfo;
	}*/

	public List<SchoolVo> findSchool(SchoolVo sv) {
		return sm.findList(sv);
		
	}
	public PageInfo<SchoolVo> findPage(SchoolVo sv, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<SchoolVo> userList = sm.findList(sv);
		PageInfo<SchoolVo> pageInfo = new PageInfo<SchoolVo>(userList);
		return pageInfo;
	}

	public School existName(String schoolname) {
		// TODO Auto-generated method stub
		School s = new School();
		s.setSchoolname(schoolname);
		
		return sm.selectOne(s);
	}

	public void addSchool(School s) {
		// TODO Auto-generated method stub
		s.setCreatetime(new Date());
		sm.insert(s);
	}
	public static boolean jiao(String phone) {

		return phone.matches("^[0-9]{11}");
	}


	public List<Area> findAera() {
		// TODO Auto-generated method stub
		return am.selectAll();
	}
	// 删除
		public void deleteUser(Integer id) throws Exception {
			sm.deleteByPrimaryKey(id);
		}

		public void updateSchool(School s) {
			// TODO Auto-generated method stub
			sm.updateByPrimaryKey(s);
		}
		public Area findByAname(String aname) {
			// TODO Auto-generated method stub
			Area a = new Area();
			a.setAreaname(aname);
			return am.selectOne(a);
		}
		public void insrnt(School s) {
			// TODO Auto-generated method stub
			s.setCreatetime(new Date());
			sm.insert(s);
		};

}