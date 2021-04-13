package zonglian.ilesson.com.ppim;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

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
        String chinese = "测试的字符串";
        char[] chars = chinese.toCharArray();

        StringBuffer buffer = new StringBuffer(); //储存结果

        //转换函数用到的一些配置
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  //转小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); //不带音标

        for(int i = 0; i < chars.length; ++i){
            if(chars[i] > 128){
                try{
                    buffer.append(PinyinHelper.toHanyuPinyinStringArray(chars[i],format)[0]);  //转换出的结果包含了多音字，这里简单粗暴的取了第一个拼音。
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{ //非汉字
                buffer.append(chars[i]);
            }
        }

        buffer.toString(); //最终的结果"ceshidezifuchuan"
        System.out.println(buffer.toString());
    }
    @Test
    public void main12(){
        long time = 1612231208588l;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(time);
        System.out.println(date);
    }

}