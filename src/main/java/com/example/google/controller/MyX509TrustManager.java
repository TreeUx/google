package com.example.google.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.google.common.MapConstant;
import com.example.google.utils.ReadTextUtils;
import com.example.google.utils.RequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
@RestController
@RequestMapping("/google")
public class MyX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    /*
     * 处理https GET/POST请求
     * 请求地址、请求方法、参数
     * */
    public static String httpsRequest(String requestUrl,String requestMethod,String outputStr){
        StringBuffer buffer=null;
        try{
            //创建SSLContext
            SSLContext sslContext=SSLContext.getInstance("SSL");
            TrustManager[] tm={new MyX509TrustManager()};
            //初始化
            sslContext.init(null, tm, new java.security.SecureRandom());
            //获取SSLSocketFactory对象
            SSLSocketFactory ssf=sslContext.getSocketFactory();
            URL url=new URL(requestUrl);
            HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            //设置当前实例使用的SSLSoctetFactory
            conn.setSSLSocketFactory(ssf);
            conn.connect();
            //往服务器端写内容
            if(null!=outputStr){
                OutputStream os=conn.getOutputStream();
                os.write(outputStr.getBytes("utf-8"));
                os.close();
            }

            //读取服务器端返回的内容
            InputStream is=conn.getInputStream();
            InputStreamReader isr=new InputStreamReader(is,"utf-8");
            BufferedReader br=new BufferedReader(isr);
            buffer=new StringBuffer();
            String line=null;
            while((line=br.readLine())!=null){
                buffer.append(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    @RequestMapping("/location")
    public void getGoogleLocation() {
//        JSONArray ja = ReadTextUtils.readText("F:\\district\\next\\products.txt");
        JSONArray ja = ReadTextUtils.readText("C:\\app\\products.txt");
        for (int i = 0; i < ja.size(); i++) {
            String query = ja.getString(i);
            String googleUrl = MapConstant.GOOGLE_PREFIX_URL + MapConstant.JSON_URL + query //google请求地址
                    + MapConstant.GOOGLE_SUFFIX_URL + MapConstant.API_KEY;
            JSONObject jo = RequestUtils.sendPost(googleUrl);
            JSONArray jas = JSONArray.parseArray((jo.get("results").toString()));
            if(jas.size() != 0) {
                JSONObject geometryJo = JSONObject.parseObject(JSONObject.parseObject(jas.get(0).toString()).get("geometry").toString());
                JSONObject locaJo = JSONObject.parseObject(geometryJo.get("location").toString());
                BigDecimal lng = new BigDecimal(locaJo.get("lng").toString()).setScale(6, BigDecimal.ROUND_HALF_UP);
                BigDecimal lat = new BigDecimal(locaJo.get("lat").toString()).setScale(6, BigDecimal.ROUND_HALF_UP);
                System.out.println(lat + "," + lng);
            } else {
                System.out.println(" ");
            }
        }
    }

}

