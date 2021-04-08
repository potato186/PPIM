package zonglian.ilesson.com.ppim;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

;
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void main1(){
        String address = "广东省深圳市";
        String p = address.substring(0,address.indexOf("省"));
        System.out.printf(p);
    }
    @Test
    public void main12(){
        long time = 1612231208588l;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(time);
        System.out.println(date);
    }

}