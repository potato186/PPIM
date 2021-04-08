package com.ilesson.ppim.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import Decoder.BASE64Encoder;

public class FileTool {
    private static String getSdcardPath(Context cx) {
        ArrayList<String> devices = getAllStorageDevices(cx);
        String device = devices.get(0);
        double max = 0;
        for (int i = 0; i < devices.size(); i++) {
            StatFs sf = new StatFs(devices.get(i));
            double blockSize = sf.getBlockSize();
            double size = sf.getAvailableBlocks() / 1024.0 / 1024 * blockSize;
            if (size > max) {
                max = size;
                device = devices.get(i);
            }
        }
        return device;
    }

    private static ArrayList<String> getAllStorageDevices(Context cx) {
        ArrayList<String> devices = new ArrayList<String>();
        devices.add(Environment.getExternalStorageDirectory().getAbsolutePath());
//		try {
//			StorageManager sm = (StorageManager) cx
//					.getSystemService(Context.STORAGE_SERVICE);
//			String[] paths = (String[]) sm.getClass()
//					.getMethod("getVolumePaths", null).invoke(sm, null);
//			for (int i = 0; i < paths.length; i++) {
//				String status = (String) sm.getClass()
//						.getMethod("getVolumeState", String.class)
//						.invoke(sm, paths[i]);
//				if (status.equals(Environment.MEDIA_MOUNTED)) {
//					File file = new File(paths[i] + "/test.txt");
//					boolean canWrite = false;
//					if (file.exists()) {
//						canWrite = file.delete();
//					} else {
//						canWrite = file.createNewFile();
//					}
//					if (canWrite) {
//						devices.add(paths[i]);
//					}
//				}
//			}
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO: handle exception
//		}
        return devices;
    }

    /**
     * 获取目录,不存在就创建：/mnt/sdcard/.resourc_not_delete
     */
    /*public static String getDir(Context cx, String dir) {
        // File sdcard = Environment.getExternalStorageDirectory();
        // String sdcard_path = sdcard.getAbsolutePath();
        SharedPreferences shared = cx.getSharedPreferences(
                Const.CONFIG_NAME, Context.MODE_PRIVATE);
        String sdcard_dir = ConfigTool.getString(shared, "compose_base_dir", "");
        String sdcard_path = null;
        if (null == sdcard_dir || "".equals(sdcard_dir)
                || !new File(sdcard_dir).exists()) {
            sdcard_path = getSdcardPath(cx);
            ConfigTool.putString(shared, "compose_base_dir", sdcard_path);
        } else {
            sdcard_path = sdcard_dir;
        }
        if (null == sdcard_path || "".equals(sdcard_path)) {
            File sdcard = Environment.getExternalStorageDirectory();
            sdcard_path = sdcard.getAbsolutePath();
        }
        File base_dir = new File(sdcard_path + dir);
        if (!base_dir.exists()) {
            base_dir.mkdirs();
        }
        return sdcard_path + dir;
    }*/

    /**
     * 保存文件
     *
     * @throws IOException
     */
    public static void save(String path, String name, String content)
            throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path + "/" + name);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.flush();
        bw.close();
    }


    public static String getImageStr(String imgSrcPath) {
        InputStream in = null;
        byte[] data = null;
        try {
            in = new FileInputStream(imgSrcPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(data==null){
            return null;
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);//返回Base64编码过的字节数组字符串
    }

    /**
     * 保存文件
     *
     * @throws IOException
     */
    public static String read(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuffer sbf = new StringBuffer();
        String tmp = null;
        while ((tmp = br.readLine()) != null) {
            sbf.append(tmp);
        }
        br.close();
        return sbf.toString();
    }

    /**
     *
     */
    public static String getNameByUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 懒虫听书子项
     *
     * @return 懒虫听书子项
     * @throws IOException
     */
    public static ArrayList<String> getListenContent(final String path)
            throws IOException {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            final ArrayList<String> data = new ArrayList<String>();
            dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(".mp3")) {
                        data.add(path + "/" + filename);
                        return true;
                    }
                    return false;
                }
            });
            Collections.sort(data);
            return data;
        }
        throw new IOException();

    }

    /**
     * 获取目录下的所有文件
     *
     * @throws IOException
     */
    public static String[] getFilePath(final String path) throws IOException {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    if (!filename.endsWith(".png")) {
                        return true;
                    }
                    return false;
                }
            });
            String[] data = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    data[i] = files[i].getAbsolutePath();
                }
            }
            return data;
        }
        throw new IOException();

    }

    /**
     * 删除目录
     */
    private static boolean deleteDir(File file) {
        boolean returnFlag = true;
        if (file.isDirectory()) {
            File[] fs = file.listFiles();
            for (File f : fs) {
                returnFlag = deleteFile(f);
                if (returnFlag == false)
                    break;
            }
            returnFlag = file.delete();
        }
        return returnFlag;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(File file) {
        boolean returnFlag = true;
        if (file.isFile()) {
            returnFlag = file.delete();
        } else {
            returnFlag = deleteDir(file);
        }
        return returnFlag;
    }

    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "B";
        }


        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }


        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }


        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
}
