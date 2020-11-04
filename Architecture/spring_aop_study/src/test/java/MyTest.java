import com.hhr.myiniter.MyInterface;
import com.hhr.myiniter.MySubClass;
import com.hhr.proxy.CaculatorProxy;
import com.hhr.service.Calculator;
import com.hhr.service.MyCalculator;
import com.hhr.service.impl.MyCalculator2;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:09
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class MyTest {
    ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("applicationContext.xml");

    @Test
    public void test01() throws NoSuchMethodException {
        /*MyCalculator myCalculator=new MyCalculator();
        System.out.println(myCalculator.add(1,2));
        System.out.println(myCalculator.div(1,2));*/

        Calculator calculator= (Calculator) CaculatorProxy.getProxy(new MyCalculator());
        calculator.add(1,1);
        calculator.sub(1,1);
        calculator.mul(1,1);
        calculator.div(1,0);

     /*   MyInterface proxy = (MyInterface) CaculatorProxy.getProxy(new MySubClass());
        proxy.show(100);*/
    }
    @Test
    public void test02() throws NoSuchMethodException {
        Calculator calculator = context.getBean("myCalculator",Calculator.class);
//        calculator.div(1,0);
        calculator.add(1, 1);
    }

    @Test
    public void test03() throws NoSuchMethodException {
        MyCalculator2 myCalculator2 = context.getBean("myCalculator2", MyCalculator2.class);
//        calculator.div(1,0);
        myCalculator2.add(1, 1);
    }
}
