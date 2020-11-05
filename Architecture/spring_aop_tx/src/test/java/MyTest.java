import com.alibaba.druid.pool.DruidDataSource;
import com.hhr.dao.EmpDao;
import com.hhr.entity.Emp;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: hhr
 * @Date: 2020/11/5 - 11 - 05 - 11:30
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class MyTest {
    ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("applicationContext.xml");
    @Test
    public void test01()throws SQLException{
        DruidDataSource dataSource = context.getBean("dataSource", DruidDataSource.class);
        System.out.println(dataSource.getConnection());
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        System.out.println(jdbcTemplate);
    }

    @Test
    public void test02(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql="insert into emp(empno,ename) values(?,?)";
        int zhangsan= jdbcTemplate.update(sql,111,"zhangsan");
        System.out.println(zhangsan);
    }

    @Test
    public void test03(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql="insert into emp(empno,ename) values(?,?)";
        List<Object[]> list=new ArrayList<Object[]>();
        list.add(new Object[]{2222,"lisi"});
        list.add(new Object[]{3333,"wangwu"});
        list.add(new Object[]{4444,"maliu"});
        int[] batchUpdate = jdbcTemplate.batchUpdate(sql, list);
        System.out.println(batchUpdate);
    }

    @Test
    public void test04(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql="delete from emp where empno=?";
        int zhangsan= jdbcTemplate.update(sql,111);
        System.out.println(zhangsan);
    }
    @Test
    public void test05(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql="update emp set ename=? where empno=?";
        int zhangsan= jdbcTemplate.update(sql,"mashibing",2222);
        System.out.println(zhangsan);
    }

    @Test
    public void test06(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql="select * from emp where empno=?";
        Emp result = jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(Emp.class),3333);
        System.out.println(result);
    }

    @Test
    public void test07(){
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        String sql = "select * from emp where sal >?";
        List<Emp> result = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(Emp.class),1500);
        for (Emp emp : result) {
            System.out.println(emp);
        }
    }

    @Test
    public void test08(){
        EmpDao empDao=context.getBean("empDao",EmpDao.class);
        empDao.save(new Emp(1111,"zhangsan"));
    }
}
