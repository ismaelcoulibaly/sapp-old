package ca.ghost_team.sapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.ghost_team.sapp.BaseApplication;
import ca.ghost_team.sapp.MainActivity;
import ca.ghost_team.sapp.R;
import ca.ghost_team.sapp.activity.DetailAnnonce;
import ca.ghost_team.sapp.database.SappDatabase;
import ca.ghost_team.sapp.model.Annonce;
import ca.ghost_team.sapp.model.AnnonceFavoris;
import ca.ghost_team.sapp.repository.AnnonceFavorisRepo;
import ca.ghost_team.sapp.service.API.AnnonceFavorisAPI;
import ca.ghost_team.sapp.service.SappAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ca.ghost_team.sapp.BaseApplication.ID_USER_CURRENT;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceVH> {

    private static final String TAG = AnnonceAdapter.class.getSimpleName();
    private final MainActivity activity;
    Context context;
    List<Annonce> listeAnnonces;
    List<Annonce> listeAnnonceFavorite;
    private MainActivity app;
    private SappDatabase db;

    // Constantes
    public static final String ANNONCE_IMAGE_REQUEST = "Annonce_Image";
    public static final String ANNONCE_TITRE_REQUEST = "Annonce_Titre";
    public static final String ANNONCE_PRICE_REQUEST = "Annonce_Prix";
    public static final String ANNONCE_DESCRIPTION_REQUEST = "Annonce_Description";
    public static final String ANNONCE_ID_REQUEST = "Annonce_ID_current_Detail";
    public static final String ANNONCE_ZIP_REQUEST = "Annonce_ZIP_Code";

    public AnnonceAdapter(Context context) {
        this.context = context;
        this.listeAnnonces = new ArrayList<>();
        this.listeAnnonceFavorite = new ArrayList<>();
        this.db = Room.databaseBuilder(context, SappDatabase.class, BaseApplication.NAME_DB)
                .allowMainThreadQueries().build();
        this.activity = (MainActivity) getContext();
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public AnnonceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_annonce_item, parent, false);
        return new AnnonceVH(view);
    }

    //Va au final renvoyer jour resant
    public String formatDate(Date d) {
        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT);
        String x = "" + shortDateFormat.format(d);
        return x;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AnnonceVH holder, int position) {
        SappAPI.getApi().create(AnnonceFavorisAPI.class).getAllAnnonceFavorisViaAPI(ID_USER_CURRENT)
                .enqueue(new Callback<List<AnnonceFavoris>>() {
                    @Override
                    public void onResponse(Call<List<AnnonceFavoris>> call, Response<List<AnnonceFavoris>> response) {
                        // Si conncetion Failed
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "Connection Failed \nFailedCode : " + response.code());
                            return;
                        }

                        Log.i(TAG, "response : " + response);
                        List<AnnonceFavoris> annonceFavorisList = response.body();

                        for (AnnonceFavoris annonce : annonceFavorisList) {
                            int c = db.annonceFavorisDao().getAnnonceFavorisIfExist(annonce.getAnnonceId(), ID_USER_CURRENT);
                            if (c == 0) {
                                new AnnonceFavorisRepo(activity.getApplication()).insertLiked(annonce);
                                holder.likeBtn.setImageResource(R.drawable.ic_favoris_red);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AnnonceFavoris>> call, Throwable t) {
                        // Si erreur 404
                        Log.e(TAG, t.getMessage());
                    }
                });

        this.listeAnnonceFavorite = db.annonceFavorisDao().findListAnnonceFavoriteByUser(ID_USER_CURRENT);
        Annonce uneAnnonce = listeAnnonces.get(position);

        if (!uneAnnonce.getAnnonceImage().equals("null"))
            holder.imageAnnonce.setImageURI(Uri.parse(uneAnnonce.getAnnonceImage()));
        else
            holder.imageAnnonce.setImageResource(R.drawable.collection);

        // holder.imageAnnonce.setImageURI(Uri.parse(uneAnnonce.getAnnonceImage()));
        holder.titre.setText(uneAnnonce.getAnnonceTitre());
        holder.prix.setText("$" + uneAnnonce.getAnnoncePrix());
        //apelle de la methode de formatage
        holder.date.setText("" + uneAnnonce.getAnnonceDate());

        // Donner les états initials du Boutton
        if (!verifyContent(uneAnnonce))
            holder.likeBtn.setImageResource(R.drawable.ic_favoris);
        else
            holder.likeBtn.setImageResource(R.drawable.ic_favoris_red);

        holder.likeBtn.setOnClickListener(v -> {
            if (uneAnnonce.getUtilisateurId() == ID_USER_CURRENT) {
                Toast.makeText(context, "Tu ne peux pas aimer ton annonce !", Toast.LENGTH_LONG).show();
                return;
            }

            if (!verifyContent(uneAnnonce)) {
                SappAPI.getApi().create(AnnonceFavorisAPI.class).likeAnnonceViaAPI(uneAnnonce.getIdAnnonce(), ID_USER_CURRENT)
                        .enqueue(new Callback<AnnonceFavoris>() {
                            @Override
                            public void onResponse(Call<AnnonceFavoris> call, Response<AnnonceFavoris> response) {
                                // Si conncetion Failed
                                if (!response.isSuccessful()) {
                                    Log.i(TAG, "Connection Failed \nFailedCode : " + response.code());
                                    return;
                                }
                                Log.i(TAG, "response : " + response);
                                AnnonceFavoris annonceFavoris = response.body();
                                int c = db.annonceFavorisDao().getAnnonceFavorisIfExist(annonceFavoris.getAnnonceId(), ID_USER_CURRENT);
                                if (c == 0) {
                                    new AnnonceFavorisRepo(activity.getApplication()).insertLiked(annonceFavoris);
                                    holder.likeBtn.setImageResource(R.drawable.ic_favoris_red);
                                }
                            }

                            @Override
                            public void onFailure(Call<AnnonceFavoris> call, Throwable t) {
                                // Si erreur 404
                                Log.e(TAG, t.getMessage());
                            }
                        });
            } else {
                SappAPI.getApi().create(AnnonceFavorisAPI.class).disLikeAnnonceViaAPI(uneAnnonce.getIdAnnonce(), ID_USER_CURRENT)
                        .enqueue(new Callback<AnnonceFavoris>() {
                            @Override
                            public void onResponse(Call<AnnonceFavoris> call, Response<AnnonceFavoris> response) {
                                // Si conncetion Failed
                                if (!response.isSuccessful()) {
                                    Log.i(TAG, "Connection Failed \nFailedCode : " + response.code());
                                    return;
                                }
                                Log.i(TAG, "response : " + response);
                                AnnonceFavoris annonceFavoris = response.body();
                                int c = db.annonceFavorisDao().getAnnonceFavorisIfExist(annonceFavoris.getAnnonceId(), ID_USER_CURRENT);
                                if (c > 0) {
                                    new AnnonceFavorisRepo(activity.getApplication()).disLikeAnnonce(annonceFavoris);
                                    holder.likeBtn.setImageResource(R.drawable.ic_favoris);
                                }
                            }

                            @Override
                            public void onFailure(Call<AnnonceFavoris> call, Throwable t) {
                                // Si erreur 404
                                Log.e(TAG, t.getMessage());
                            }
                        });
            }
            notifyDataSetChanged();
        });

        // set OnClickListener
        holder.cardView_detail_Article.setOnClickListener(v -> {
            // Creation de l'intent (Envoyer Toutes les informations nécessaires vers l'Activité)
            Intent intent = new Intent(context, DetailAnnonce.class);
            intent.putExtra(ANNONCE_ID_REQUEST, uneAnnonce.getIdAnnonce());
            intent.putExtra(ANNONCE_IMAGE_REQUEST, uneAnnonce.getAnnonceImage().trim());
            intent.putExtra(ANNONCE_TITRE_REQUEST, uneAnnonce.getAnnonceTitre().trim());
            intent.putExtra(ANNONCE_PRICE_REQUEST, uneAnnonce.getAnnoncePrix());
            intent.putExtra(ANNONCE_DESCRIPTION_REQUEST, uneAnnonce.getAnnonceDescription().trim());
            intent.putExtra(ANNONCE_ZIP_REQUEST, uneAnnonce.getAnnonceZip().trim());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return listeAnnonces.size();
    }

    /**
     * Methode qui permet de setter une nouvelle liste à la liste de toutes les Annonces récupérée
     * depuis a base de données
     *
     * @param listeAllAnnonces la nouvelle liste à passer vers l'Adapter, contenant toutes les Annonces
     * @return void
     */
    public void addAnnonce(List<Annonce> listeAllAnnonces) {
        listeAnnonces = listeAllAnnonces;
        notifyDataSetChanged();
    }

    /**
     * Methode qui permet de Vérifier si une Annonce se trouve dans la liste des Annonces Aimées
     * par l'utilisateur Courant del'Application
     *
     * @param uneAnnonce Entité Annonce à vérifier
     *                   Si @code{uneAnnonce} est trouvée, retourne true
     *                   Sinon false
     * @return boolean
     */
    public boolean verifyContent(Annonce uneAnnonce) {
        if (listeAnnonceFavorite.size() > 0) {
            for (Annonce annonce : listeAnnonceFavorite) {
                if (annonce.getIdAnnonce() == uneAnnonce.getIdAnnonce())
                    return true;
            }
        }
        return false;
    }

    static class AnnonceVH extends RecyclerView.ViewHolder {
        /* On définit les Champs du model */
        TextView titre;
        TextView prix;
        TextView date;
        ImageView likeBtn;
        ImageView imageAnnonce;
        CardView cardView_detail_Article;

        public AnnonceVH(@NonNull View itemView) {
            super(itemView);
            imageAnnonce = itemView.findViewById(R.id.annonceImage);
            titre = itemView.findViewById(R.id.annonceTitre);
            prix = itemView.findViewById(R.id.annoncePrix);
            date = itemView.findViewById(R.id.annonceDate);
            likeBtn = itemView.findViewById(R.id.annonceLiked);
            cardView_detail_Article = itemView.findViewById(R.id.item_annonce);
        }
    }
}
