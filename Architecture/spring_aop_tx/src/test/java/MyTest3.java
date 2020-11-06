import com.hhr.service.BookService;
import com.hhr.service.Multservice;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Auther: hhr
 * @Date: 2020/11/5 - 11 - 05 - 15:50
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class MyTest3 {
    ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("applicationContext.xml");
    @Test
    public void test01(){
        Multservice multservice = context.getBean(Multservice.class);
        multservice.mult();
    }
}
