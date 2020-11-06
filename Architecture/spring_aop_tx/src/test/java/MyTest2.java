import com.hhr.service.BookService;
import org.aspectj.weaver.ast.Var;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Auther: hhr
 * @Date: 2020/11/5 - 11 - 05 - 15:50
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class MyTest2 {
    ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("applicationContext.xml");
    @Test
    public void test01(){
        BookService contextBean = context.getBean(BookService.class);
        contextBean.buyBook();
    }
}
