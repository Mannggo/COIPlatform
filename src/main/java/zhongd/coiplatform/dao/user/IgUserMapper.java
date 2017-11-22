package zhongd.coiplatform.dao.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import zhongd.coiplatform.entity.User;
import zhongd.coiplatform.entity.DO.user.IgUserDO;

@Mapper
public interface IgUserMapper {
	@Select("SELECT * FROM ig_user where username = #{username}")
	public IgUserDO getObj(@Param(value = "username") String username);
	
	@Select(value = "SELECT * FROM ig_user where username = #{arg0} and password = #{arg1}")
	public IgUserDO getUserByUsernameAndPassword(String username, String password);
}