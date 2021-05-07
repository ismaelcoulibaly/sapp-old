package ca.ghost_team.sapp.service.API;


import java.util.List;

import ca.ghost_team.sapp.model.Annonce;
import ca.ghost_team.sapp.model.Utilisateur;
import ca.ghost_team.sapp.service.dto.UtilisateurDTO;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UtilisateurAPI {

    @FormUrlEncoded
    @POST("user.php")
    Call<Utilisateur> getUtilisateurViaAPI(
            @Field("username") String nom,
            @Field("password") String motpasse
    );

//    @GET("user.php")
//    Call<Utilisateur> getUtilisateurViaAPI(
//            @Query("username") String nom,
//            @Query("password") String motpasse
//    );

    @FormUrlEncoded
    @POST("signup.php")
    Call<String> createUtilisateurViaAPI(@Field("checksum") String checksum);

//    @GET("signup.php")
//    Call<Utilisateur> createUtilisateurViaAPI(
//            @Query("fullname") String fullname,
//            @Query("username") String username,
//            @Query("password") String password,
//            @Query("email") String email
//    );

    @FormUrlEncoded
    @POST("userbyannonce.php")
    Call<Utilisateur> getUtilisateurViaAnnonceAPI(
            @Field("titre") String titre,
            @Field("prix") int prix
    );

    @GET("userbyannonce.php")
    Call<Utilisateur> getUtilisateurViaAnnonceGetAPI(
            @Query("titre") String titre,
            @Query("prix") int prix
    );

}
