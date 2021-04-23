package zonglian.ilesson.com.ppim;

import com.google.gson.Gson;
import com.ilesson.ppim.entity.LogistBase;
import com.ilesson.ppim.entity.LogistReg;
import com.ilesson.ppim.utils.Constants;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ilesson.ppim.activity.ModifyLogisticActivity.json;
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
        LogistBase logistBase = new Gson().fromJson(json, LogistBase.class);
//        String no = "552040947081913";
//        String no = "777039877137211";
        String no = "SF1322209583703";
        List<LogistReg> companyReturnList = logistBase.getCompanyReturnList();
        for (int i =0;i<companyReturnList.size();i++) {
            LogistReg logistReg = companyReturnList.get(i);
            Pattern p = Pattern.compile(logistReg.getReg_mail_no());  //正则表达式
            Matcher m = p.matcher(no);
            if(m.matches()){
                System.out.println(logistReg.getName());
            }
        }
    }

    private static final String TAG = "ExampleUnitTest";
    @Test
    public void loadData() throws Exception {
        //https://pp.fangnaokeji.com:9443/pp/express?action=query&no=4607490744122
        URL localURL = new URL(Constants.BASE_URL + Constants.EXPRESS+"?action=query&no=YT9535522647381");
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;

        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String token = "A01479421152D14FF6393863449A40969C2EE3C722BEEE1BA060CD521A6C8B27";
        httpURLConnection.setRequestProperty("produce","pp");
        httpURLConnection.setRequestProperty("channel","1001");
        httpURLConnection.setRequestProperty("token",token);
        httpURLConnection.setRequestProperty("authorization",token);
        httpURLConnection.setRequestProperty("version","1");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
        }

        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            System.out.println(resultBuffer.toString());
        } finally {

            if (reader != null) {
                reader.close();
            }

            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        }


    }
    @Test
    public void test4() {
        // 无返回值lambda函数体中用法
        Runnable r1 = () -> {
            System.out.println("hello lambda1");
            System.out.println("hello lambda2");
            System.out.println("hello lambda3");
        };
        r1.run();

        // 有返回值lambda函数体中用法
        BinaryOperator<Integer> binary = (x, y) -> {
            int a = x * 2;
            int b = y + 2;
            return a + b;
        };
        System.out.println(binary.apply(1, 2));// 3

    }
}