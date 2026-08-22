package com.adarsh.ridebookingbookingservice.configuration;

import com.adarsh.ridebookingbookingservice.apis.AuthServiceApi;
import com.adarsh.ridebookingbookingservice.apis.LocationServiceApi;
import com.adarsh.ridebookingbookingservice.apis.SocketServiceApi;
import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {

    private final EurekaClient eurekaClient;
    public RetrofitConfig(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    private String getServiceUrl(String serviceName) {
        return eurekaClient.getNextServerFromEureka(serviceName,false).getHomePageUrl();
    }

    @Bean
    public LocationServiceApi locationServiceApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("RIDEBOOKING-LOCATIONSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(LocationServiceApi.class);
    }


    @Bean
    public AuthServiceApi authServiceApi() {

        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("RIDEBOOKING-AUTHSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(AuthServiceApi.class);
    }


    @Bean
    public SocketServiceApi socketServiceApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("RIDEBOOKING-SOCKETSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(SocketServiceApi.class);
    }


}
