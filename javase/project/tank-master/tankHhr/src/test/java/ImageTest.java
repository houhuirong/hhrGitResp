import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * @Auther: hhr
 * @Date: 2020/8/30 - 08 - 30 - 10:20
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class ImageTest {
    @Test
    public void test(){
        try {
            BufferedImage image = ImageIO.read(new File("C:\\Users\\Administrator.PC-20200319WBHM\\Pictures\\QQ图片20200416105700.jpg"));
            assertNotNull(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
