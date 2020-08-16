package com.xiaoshu.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xiaoshu.entity.Log;
import com.xiaoshu.entity.Menu;
import com.xiaoshu.entity.Role;
import com.xiaoshu.entity.Token;
import com.xiaoshu.entity.User;
import com.xiaoshu.service.LogService;
import com.xiaoshu.service.MenuService;
import com.xiaoshu.service.RoleService;
import com.xiaoshu.service.TokenService;
import com.xiaoshu.service.UserService;
import com.xiaoshu.util.CodeUtil;
import com.xiaoshu.util.IpUtil;
import com.xiaoshu.util.StochasticUtil;
import com.xiaoshu.util.StringUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Controller
@SuppressWarnings("unchecked")
public class LoginController {

	@Autowired
	private UserService userService;
	@Autowired
	private MenuService menuService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private LogService logService;
	@Autowired
	private TokenService tokenService;
	
	static Logger logger = Logger.getLogger(LoginController.class);
	
	
	@SuppressWarnings("static-access")
	@RequestMapping("login")
	public void login(HttpServletRequest request,HttpServletResponse response) throws Exception{
		try {
			HttpSession session = request.getSession();
			String userName=request.getParameter("userName");
			String password=request.getParameter("password");
			String imageCode=request.getParameter("imageCode");
			String auto = request.getParameter("auto");
			request.setAttribute("userName", userName);
			request.setAttribute("password", password);
			request.setAttribute("imageCode", imageCode);
			if(StringUtil.isEmpty(userName)||StringUtil.isEmpty(password)){
				request.setAttribute("error", "璐︽埛鎴栧瘑鐮佷负绌�");
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
/*			if(StringUtil.isEmpty(imageCode)){
				request.setAttribute("error", "楠岃瘉鐮佷负绌�");
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
			if(!imageCode.toUpperCase().equals(session.getAttribute("sRand").toString().toUpperCase())){
				request.setAttribute("error", "楠岃瘉鐮侀敊璇�");
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}*/
			User user = new User();
			user.setUsername(userName);
			user.setPassword(password);
			User currentUser = userService.loginUser(user);
			if(currentUser==null){
				request.setAttribute("error", "鐢ㄦ埛鍚嶆垨瀵嗙爜閿欒");
				request.getRequestDispatcher("login.jsp").forward(request, response);
			}else{
				// 鍔犲叆鐧婚檰鏃ュ織
				Log log = new Log();
				log.setUsername(userName);
				log.setCreatetime(new Date());
				log.setIp(IpUtil.getIpAddr(request));
				log.setOperation("鐧诲綍");
				logService.insertLog(log);
				
				// 鐧诲綍淇℃伅瀛樺叆session
				Role role = roleService.findOneRole(currentUser.getRoleid());
				String roleName = role.getRolename();
				currentUser.setRoleName(roleName);
				session.setAttribute("currentUser", currentUser);  // 褰撳墠鐢ㄦ埛淇℃伅
				session.setAttribute("currentOperationIds", role.getOperationids());  // 褰撳墠鐢ㄦ埛鎵�鎷ユ湁鐨勬寜閽潈闄�
				
				// 鍕鹃�変簡涓ゅ懆鍐呰嚜鍔ㄧ櫥褰曘��
				if ("on".equals(auto)) {
					// 璁颁綇鐧诲綍淇℃伅
					Token token = new Token();
					token.setUserid(currentUser.getUserid());
					String userAgent = StochasticUtil.getUUID();
					token.setUseragent(CodeUtil.getMd5(userAgent, 32));
					token.setCreatetime(new Date());
					Calendar cal = Calendar.getInstance();
					cal.add(cal.WEEK_OF_YEAR, 2);
					token.setExpiretime(cal.getTime());
					String t = CodeUtil.getMd5(currentUser.getUsername()+CodeUtil.getMd5(userAgent, 32), 32);
					token.setToken(t);
					tokenService.insertToken(token);
					
					// 璁剧疆cookie
					Cookie cookie = new Cookie("autoLogin",t);
					cookie.setMaxAge(3600*24*15);  // cookie鏃舵晥15澶�
					response.addCookie(cookie);
				}
				
				// 璺宠浆鍒颁富鐣岄潰
				response.sendRedirect("main.htm");
			}
		} catch (Exception e) {
			logger.error("鐢ㄦ埛鐧诲綍閿欒",e);
			throw e;
		}
	}
	
	
	// 杩涘叆绯荤粺涓荤晫闈�
	@RequestMapping("main")
	public String toMain(HttpServletRequest request,HttpServletResponse response) throws Exception{
		User currentUser = (User) request.getSession().getAttribute("currentUser");
		if(currentUser == null ){
			return null;
		}
		getMenuTree("-1",currentUser,request,response);
		return "main";
	}
	
	// 杩涘叆绯荤粺涓荤晫闈�
	@RequestMapping("index")
	public String toIndex(HttpServletRequest request,HttpServletResponse response){
		return "index";
	}
	
	
	// 鍔犺浇鏈�涓婄骇宸﹁彍鍗曟爲
	public void getMenuTree(String parentId,User currentUser,HttpServletRequest request,HttpServletResponse response) throws Exception{
		try {
			
			Role role = roleService.findOneRole(currentUser.getRoleid());
			if(role != null && StringUtil.isNotEmpty(role.getMenuids())){
				String[] menuIds = role.getMenuids().split(",");
				Map map = new HashMap();
				map.put("parentId",parentId);
				map.put("menuIds", menuIds);
				JSONArray jsonArray = getMenusByParentId(parentId, menuIds);
				request.setAttribute("menuTree", jsonArray.get(0));
			}
		} catch (Exception e) {
			logger.error("鍔犺浇宸﹁彍鍗曢敊璇�",e);
			throw e;
		}
	}
	
	
	// 閫掑綊鍔犺浇鎵�鎵�鏈夋爲鑿滃崟
	public JSONArray getMenusByParentId(String parentId,String[] menuIds)throws Exception{
		JSONArray jsonArray = this.getMenuByParentId(parentId,menuIds);
		for(int i=0;i<jsonArray.size();i++){
			JSONObject jsonObject=jsonArray.getJSONObject(i);
			if(!"isParent".equals(jsonObject.getString("state"))){
				continue;
			}else{
				jsonObject.put("children", getMenusByParentId(jsonObject.getString("id"),menuIds));
			}
		}
		return jsonArray;
	}
	
	
	// 灏嗘墍鏈夌殑鏍戣彍鍗曟斁鍏son鏁版嵁涓�
	public JSONArray getMenuByParentId(String parentId,String[] menuIds)throws Exception{
		JSONArray jsonArray=new JSONArray();
		Map map= new HashMap();
		map.put("parentId",Integer.parseInt(parentId));
		map.put("menuIds", menuIds);
		List<Menu> list = menuService.menuTree(map);
		for(Menu menu : list){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", menu.getMenuid());
			jsonObject.put("text", menu.getMenuname());
			jsonObject.put("iconCls", menu.getIconcls());
			JSONObject attributeObject = new JSONObject();
			attributeObject.put("menuUrl", menu.getMenuurl());
			jsonObject.put("state", menu.getState());				
			jsonObject.put("attributes", attributeObject);
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}
	
	
	// 鍒ゆ柇鏄笉鏄湁瀛愬瀛愶紝浜哄伐缁撴潫閫掑綊鏍�
	public boolean hasChildren(Integer parentId,String[] menuIds) throws Exception{
		boolean flag = false;
		try {
			Map map= new HashMap();
			map.put("parentId",parentId);
			map.put("menuIds", menuIds);
			List<Menu> list = menuService.menuTree(map);
			if (list == null || list.size()==0) {
				flag = false;
			}else {
				flag = true;
			}
		} catch (Exception e) {
			logger.error("鍔犺浇宸﹁彍鍗曟椂鍒ゆ柇鏄笉鏄湁瀛愬瀛愰敊璇�",e);
			throw e;
		}
		return flag;
	}
	
	//瀹夊叏閫�鍑�
	@RequestMapping("logout")
	public void logout(HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		// 璁板綍鏃ュ織
		User currentUser = (User) request.getSession().getAttribute("currentUser");
		Log log = new Log();
		log.setUsername(currentUser.getUsername());
		log.setCreatetime(new Date());
		log.setOperation("閫�鍑�");
		logService.insertLog(log);
		
		// 娓呯┖session
		request.getSession().invalidate();
		
		// 娓呯┖cookie
		Cookie[] cookies = request.getCookies();
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = new Cookie(cookies[i].getName(), null);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}

		response.sendRedirect("login.jsp");
	}
	
	
	
	/**
	 * 鑷姩鐧诲綍
	 * @param request
	 * @param response
	 */
	@RequestMapping("auto")
	public void autoLogin(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	Cookie[] cookies = request.getCookies();
    	if(cookies != null) {
        	for(int i=0; i<cookies.length; i++) {
           		Cookie cookie = cookies[i];
           		if("autoLogin".equals(cookie.getName())){
					  Map map = new HashMap();
					  map.put("token", cookie.getValue());
					  map.put("expireTime", new Date());
					  Token token = tokenService.findOneToken(map);
					  if (token == null) {
						  request.getRequestDispatcher("login.jsp").forward(request, response);
						  return;
					  } else {
						  	int userId = token.getUserid();
						  	User currentUser = userService.findOneUser(userId);
						  	Log log = new Log();
							log.setUsername(currentUser.getUsername());
							log.setCreatetime(new Date());
							log.setIp(IpUtil.getIpAddr(request));
							log.setOperation("鐧诲綍");
							logService.insertLog(log);
							
							// 鐧诲綍淇℃伅瀛樺叆session
							Role role = roleService.findOneRole(currentUser.getRoleid());
							String roleName = role.getRolename();
							currentUser.setRoleName(roleName);
							HttpSession session = request.getSession();
							session.setAttribute("currentUser", currentUser);  // 褰撳墠鐢ㄦ埛淇℃伅
							session.setAttribute("currentOperationIds", role.getOperationids());  // 褰撳墠鐢ㄦ埛鎵�鎷ユ湁鐨勬寜閽潈闄�
							// 璺宠浆鍒颁富鐣岄潰
							response.sendRedirect("main.htm");
							return;
					  }
           		}
        	}
    	}
    	request.getRequestDispatcher("login.jsp").forward(request, response);
	}
	
}
