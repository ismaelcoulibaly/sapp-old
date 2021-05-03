package ca.ghost_team.sapp.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SappAPI {
    public static Retrofit api = null;

    public static Retrofit getApi(){
        if(api == null){
            Gson gson = new GsonBuilder().create();

            api =  new Retrofit.Builder()
                    .baseUrl("http://172.16.201.9/sappserver/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
                    //.create(UtilisateurAPI.class);
        }

        return api;
    }

}
