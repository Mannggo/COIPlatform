package zhongd.coiplatform.service.user;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhongd.coiplatform.dao.user.IgUserMapper;
import zhongd.coiplatform.dao.user.UserJoinMapper;
import zhongd.coiplatform.entity.DO.user.IgPermission;
import zhongd.coiplatform.entity.ReturnObj;
import zhongd.coiplatform.entity.DO.user.IgRole;
import zhongd.coiplatform.entity.DO.user.IgUser;
import zhongd.coiplatform.entity.DTO.user.IgUserDTO;
import zhongd.coiplatform.entity.DTO.user.IgUserLoginDTO;
import zhongd.coiplatform.utils.constant.Constant;
import zhongd.coiplatform.utils.PasswordHandler;
import zhongd.coiplatform.utils.constant.ReturnCode;

@Service
public class IgUserServiceImpl implements IgUserService {
	@Autowired
	IgUserMapper igUserMapper;
	@Autowired
	UserJoinMapper joinMapper;

	Logger logger = LoggerFactory.getLogger(getClass());
	public IgUser getQueryUserDO() {
		return new IgUser();
	}
	@Override
	public IgUser getIgUserByUsername(String username) {
		IgUser queryUser = getQueryUserDO();
		queryUser.setUsername(username);
		return (IgUser) igUserMapper.selectOne(queryUser);
	}

	@Override
	public ReturnObj login(IgUser user) {
		ReturnObj obj = new ReturnObj();
		try {
			IgUser check = getIgUserByUsername(user.getUsername());
			if (check == null) {
				obj.setMsg("用户不存在");
				obj.setReturnCode(ReturnCode.LOGIN_ERROR_USER_NOT_EXIST);
				return obj;
			}
			user.setPassword(PasswordHandler.encodePassword(user.getPassword(), check.getUsername(), Constant.MD5_STR));
			if (!user.getPassword().equals(check.getPassword())) {
				obj.setMsg("密码错误");
				obj.setReturnCode(ReturnCode.LOGIN_ERROR_PASSWORD_INCORRECT);
				return obj;
			}
			UsernamePasswordToken token = new UsernamePasswordToken(check.getUsername(), check.getPassword());
			Subject subject = SecurityUtils.getSubject();
			subject.login(token);
			// 将登录信息放进Session中
			IgUserLoginDTO login = new IgUserLoginDTO();
			login.setIgUser(check);
			login.setLoginTime(new Date());
			subject.getSession().setAttribute("currentUser", login);

			obj.setMsg("登录成功");
			obj.setReturnCode(ReturnCode.LOGIN_SUCCESS);
			logger.info("===========成功===========");
		}catch (Exception e){
			logger.error(e.getMessage(), e);
			obj.setMsg("登录失败，请重新登录");
			obj.setReturnCode(ReturnCode.FAIL);
		}
		return obj;
	}
	
	public Map<String, Object> getUserRoleSet(Integer igUserId){
		Set<IgRole> set = joinMapper.getRoleSet(igUserId);
		Map<String, Object> data = new HashMap<>();
		data.put("set", set);
		data.put("count", set.size());
		return data;
	}

	public Map<String, Object> getUserUserPermissions(Integer igUserId){
		Set<IgPermission> set = joinMapper.getUserPermissionSet(igUserId);
		Map<String, Object> data = new HashMap<>();
		data.put("set", set);
		data.put("count", set.size());
		return data;
	}
	@Override
	public int insert(IgUser user) {
		user.setPassword(PasswordHandler.encodePassword(user.getPassword(), user.getUsername(), Constant.MD5_STR));
		return igUserMapper.insertSelective(user);
	}
	@Override
	public int deleteById(IgUser user) {
		return igUserMapper.deleteByPrimaryKey(user.getIgUserId());
	}
	@Override
	public int update(IgUser user) {
		return igUserMapper.updateByPrimaryKeySelective(user);
	}
	@Override
	public List<IgUserDTO> list(int pageSize, int pageIndex, IgUserDTO queryUser) {
		Map<String, Object> param = new HashMap<String, Object>();
		pageIndex -= 1;
		param.put("pageIndex", pageIndex);
		param.put("pageSize", pageSize);
		param.put("condition", queryUser);
		return joinMapper.getUserList(param);
	}

	@Override
	public int setRole(Integer igUserId, Integer igRoleId, Integer currentUserId) {
		Map<String, Object> param = new HashMap<>();
		param.put("igUserId", igUserId);
		param.put("igRoleId", igRoleId);
		param.put("updateBy", currentUserId);
		param.put("createBy", currentUserId);
		param.put("updateTime", new Date());
		param.put("createTime", new Date());
		return joinMapper.setRole(param);
	}

	@Override
	public int rmRole(Integer igUserId, Integer igRoleId) {
		Map<String, Object> param = new HashMap<>();
		param.put("igUserId", igUserId);
		param.put("igRoleId", igRoleId);
		return joinMapper.rmRole(param);
	}

	@Override
	public Map<String, Object> getUserRoleSelectSet(Integer igUserId) {
		Set<IgRole> set = joinMapper.getRoleSelectSet(igUserId);
		Map<String, Object> data = new HashMap<>();
		data.put("set", set);
		data.put("count", set.size());
		return data;
	}
}
