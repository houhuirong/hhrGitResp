import com.hhr.proxy.CaculatorProxy;
import com.hhr.service.Calculator;
import com.hhr.service.MyCalculator;
import org.junit.Test;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:09
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class MyTest {
    @Test
    public void test01() throws NoSuchMethodException {
        /*MyCalculator myCalculator=new MyCalculator();
        System.out.println(myCalculator.add(1,2));
        System.out.println(myCalculator.div(1,2));*/

        Calculator calculator= CaculatorProxy.getCalculator(new MyCalculator());
        calculator.add(1,1);
    }
}
