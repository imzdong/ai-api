package org.imzdong.ai.openai.builder;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author DongZhou
 * @since 2023/3/31 10:31
 */
public class FeignClientBuilder {

    public static <T> T build(String baseUrl, Class<T> clazz, String token, Proxy proxy) {
        Feign.Builder builder = initFeignBuilder(token, proxy);
        return builder.target(clazz, baseUrl);
    }

    private static Feign.Builder initFeignBuilder(String token, Proxy proxy) {
        return Feign.builder()
                .client(new OkHttpClient(initOkhttp3Client(token, proxy)))
                .logger(new Logger.ErrorLogger())
                .logLevel(Logger.Level.FULL)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder());
    }

    private static okhttp3.OkHttpClient initOkhttp3Client(String token, Proxy proxy) {
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(new TokenHeaderInterceptor(token))
                .addInterceptor(new RequestInterceptor())
                .retryOnConnectionFailure(false)//是否开启缓存
                .connectionPool(pool())//连接池
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(10L, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory(), x509TrustManager());
        if(proxy != null){
            builder.proxy(proxy);
        }
        return builder.build();
    }

    private static class TokenHeaderInterceptor implements Interceptor {
        private String authToken;
        public TokenHeaderInterceptor(String authToken){
            this.authToken = "Bearer " + authToken;
        }
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request updateRequest = originalRequest.newBuilder().header("Authorization", authToken).build();
            return chain.proceed(updateRequest);
        }
    }
    /**
     * 请求拦截器，修改请求header
     */
    private static class RequestInterceptor implements Interceptor{

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Accept", "*/*")
                    .addHeader("Access-Control-Allow-Origin", "*")
                    .addHeader("Access-Control-Allow-Headers", "X-Requested-With")
                    .addHeader("Vary", "Accept-Encoding")
                    .build();

            return chain.proceed(request);
        }
    }
    private static ConnectionPool pool() {
        return new ConnectionPool(100, Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS);
    }
    private static SSLSocketFactory sslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("sslSocketFactory error");
    }
    private static X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

}
