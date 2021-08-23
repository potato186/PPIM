package zonglian.ilesson.com.ppim;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        System.out.println(hour);
        System.out.println(rightNow.get(Calendar.MINUTE));
        System.out.println(rightNow.get(Calendar.DAY_OF_MONTH));
        System.out.println(rightNow.get(Calendar.MONTH)+1);
    }

    private static final String TAG = "ExampleUnitTest";
    @Test
    public void loadData() throws Exception {
        //https://pp.fangnaokeji.com:9443/pp/express?action=query&no=4607490744122
        //https://pp.fangnaokeji.com:9443/pp/produce?action=selection&id=6
//        URL localURL = new URL(Constants.BASE_URL + Constants.EXPRESS+"?action=query&no=YT9535522647381");
        URL localURL = new URL("https://pp.fangnaokeji.com:9443/pp/order?action=info&oid=1309");
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;

        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String token = "A01479421152D14FF6393863449A40969C2EE3C722BEEE1BA060CD521A6C8B27";
        httpURLConnection.setRequestProperty("produce","pp");
        httpURLConnection.setRequestProperty("channel","1001");
        httpURLConnection.setRequestProperty("token",token);
        httpURLConnection.setRequestProperty("authorization",token);
        httpURLConnection.setRequestProperty("version","2");
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
        int i = 0x0A;
        System.out.println(i);
//      String s = "Cang Yuan Hong Rui Ji Dong Che An Quan Jian Ce Fu Wu You Xian Gong Si";
//      System.out.println(s.length());
//        setVoiceCommand("151000123","A00202");
    }
    public void setVoiceCommand(String deviceUid, String content) {
        try {
            byte[] contents = hexStringToBytes(content);
            byte[] allgoByte = new byte[6 + contents.length];

            allgoByte[0] = (byte) (0xFE);  //省电模式

            //转为16进制
            deviceUid = Integer.toHexString(Integer.parseInt(deviceUid));
            int length = deviceUid.length();

            //前面补0
            for (int i = 0; i < 8 - length; i++) {
                deviceUid = "0" + deviceUid;
            }

            //4个字节的deviceUid
            for (int i = 1, j = 6; i <= 4; i++, j = j - 2) {
                short a = Short.valueOf(deviceUid.substring(j, j + 2), 16);       //然后转为字节
                byte p0 = (byte) a;
                allgoByte[i] = p0;
            }

            int allLength = 6 + contents.length;
            allgoByte[5] = (byte) allLength;

            //带上透传的协议
            System.arraycopy(contents, 0, allgoByte, 6, contents.length);
            System.out.println();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


    }

    /**
     *
     * @param deviceUid
     * @return 151000123 -> 3B140009
     */
    private static String convertId(String deviceUid) {
        int value = Integer.parseInt(deviceUid);
        String hex = Integer.toHexString(value).toUpperCase();

        while(hex.length() < 8) {
            hex = "0" + hex;
        }

        char[] chs = new char[8];
        chs[0] = hex.charAt(6);
        chs[1] = hex.charAt(7);
        chs[2] = hex.charAt(4);
        chs[3] = hex.charAt(5);
        chs[4] = hex.charAt(2);
        chs[5] = hex.charAt(3);
        chs[6] = hex.charAt(0);
        chs[7] = hex.charAt(1);

        return new String(chs);
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    @Test
    public void main2(){
        int a = 5;
        List<Integer> arr = new ArrayList<>();
        List<Integer> arr1 = new ArrayList<>();
        arr.add(1);
        arr.add(2);
        arr.add(5);
        arr.add(6);
        arr1 = arr.subList(0,arr.size()-1);
        for (Integer integer : arr1) {
            System.out.println(integer);
        }
    }
}