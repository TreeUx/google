package com.example.google.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.google.common.MapConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author Breach
 * @Date 2018/11/14
 * @Version V1.0
 **/
@RestController
@Log4j2
@RequestMapping("/bxly")
public class GoogMapController implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /**
      * @Author Breach
      * @Description 请求google地图api
      * @Date 2018/11/14
      * @Param
      * @return void
      */
    @RequestMapping("/googleInfo")
    public JSONObject getGoogMapInfo(@RequestBody JSONObject object) {
        Map<String, Object> para = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray results = null;
        JSONObject tempJo = new JSONObject();
        JSONObject jo = null;

        JSONObject geometry = null;
        StringBuffer buffer = null;
        String address = object.get("address").toString();//地址
        String location = object.get("location").toString();//地址
        InputStream in = null;
        String requestUrl = MapConstant.GOOGLE_PLACE_URL + address + MapConstant.PIEFIX_LOCATION + location
                + MapConstant.GOOGLE_LANGUAGE + MapConstant.GOOGLE_SUFFIX_URL + MapConstant.API_KEY;//请求地址
        try {
            //创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new MyX509TrustManager()};
            //初始化
            sslContext.init(null, tm, new SecureRandom());
            //获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            System.out.println(url);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            //设置当前实例使用SSLSocketFactory
            conn.setSSLSocketFactory(ssf);
            conn.connect();

            in = conn.getInputStream();
            //读取服务端返回的内容
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf8"));
            buffer = new StringBuffer();
            String line = "";
            while((line = br.readLine()) != null) {
                buffer.append(line);
            }
            jo = JSONObject.parseObject(buffer.toString());
            String status = jo.get("status").toString();//状态码
            results = JSONArray.parseArray(jo.get("results").toString());
            System.out.println("开始");
            for(int i = 0; i < results.size(); i++) {
                JSONObject jot = JSONObject.parseObject(results.get(i).toString());
                if(jot.containsKey("geometry")) {
                    geometry = JSONObject.parseObject(jot.get("geometry").toString());
                } else {
                    geometry = JSONObject.parseObject("");
                }
                String formatted_address = jot.get("formatted_address").toString();//地点位置
                String name = jot.get("name").toString();//地点名称
                String loca = geometry.get("location").toString();//坐标
                para.put("name", name);
                para.put("location", loca);
                para.put("formatted_address", formatted_address);
                list.add(para);
                para = new HashMap<>();
            }
            tempJo.put("status", status);
            tempJo.put("results", list);
            System.out.println(tempJo);
            System.out.println("结束");
//            System.out.println(buffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempJo;
    }

    public static void main(String[] args) {

        Map<String, Object> para = new HashMap<>();
        Map<String, Object> para1 = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> list1 = new ArrayList<>();
        JSONObject jo1 = new JSONObject();
        for(int i = 0; i < 4; i++) {
            para.put("name", i + "rose");
            para.put("age", i + "16");
            para.put("sex", i + "male");
            list.add(para);
//            list1.addAll(list);
            para = new HashMap<>();
            System.out.println(list);
        }
        jo1.put("results", list.toString());

    }
}
