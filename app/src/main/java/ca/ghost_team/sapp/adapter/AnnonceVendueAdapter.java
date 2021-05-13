package ca.ghost_team.sapp.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import ca.ghost_team.sapp.BaseApplication;
import ca.ghost_team.sapp.R;
import ca.ghost_team.sapp.activity.AnnonceVendue;
import ca.ghost_team.sapp.database.SappDatabase;
import ca.ghost_team.sapp.model.Annonce;
import ca.ghost_team.sapp.model.AnnonceFavoris;
import ca.ghost_team.sapp.repository.AnnonceRepo;
import ca.ghost_team.sapp.repository.MessageRepo;
import ca.ghost_team.sapp.service.API.AnnonceAPI;
import ca.ghost_team.sapp.service.SappAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ca.ghost_team.sapp.BaseApplication.ID_USER_CURRENT;

public class AnnonceVendueAdapter extends RecyclerView.Adapter<AnnonceVendueAdapter.AnnonceVendueVH> {

    private static final String TAG = AnnonceVendueAdapter.class.getSimpleName();
    Context context;
    private List<Annonce> listeAnnonceVendue;
    private SappDatabase db;

    public AnnonceVendueAdapter(Context context) {
        this.context = context;
        listeAnnonceVendue = new ArrayList<>();
        this.db = Room.databaseBuilder(context, SappDatabase.class, BaseApplication.NAME_DB)
                .allowMainThreadQueries().build();
    }

    @NonNull
    @Override
    public AnnonceVendueVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_annonce_vendue_item, parent, false);
        return new AnnonceVendueAdapter.AnnonceVendueVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnonceVendueVH holder, int position) {
        Annonce annonce = listeAnnonceVendue.get(position);

        if(!annonce.getAnnonceImage().equals("null"))
            holder.annonceImage.setImageURI(Uri.parse(annonce.getAnnonceImage()));
        else
            holder.annonceImage.setImageResource(R.drawable.collection);

        holder.annonceTitre.setText(annonce.getAnnonceTitre());
        holder.annonceDescription.setText(annonce.getAnnonceDescription());
        holder.annoncePrice.setText("$" + annonce.getAnnoncePrix());

        // Trash
        holder.annonceTrash.setOnClickListener((v)->{


           AlertDialog x =new  AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.titre_Supprimer_Annonce)
                   .setMessage(context.getResources().getString(R.string.suprimer_Anonces_Question))
                   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                            ajjouter(position,annonce);
                        Toast.makeText(context, R.string.efacerAnnonce,Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(R.string.no, null).show();


    });    }

    public void ajjouter (int position,Annonce annonce){  //  void ajouterx (){
        Annonce uneAnnonce = listeAnnonceVendue.get(position);
        listeAnnonceVendue.remove(uneAnnonce);
        Log.i(TAG,"Annonce " + uneAnnonce + " supprimé");


        SappAPI.getApi().create(AnnonceAPI.class).deleteMyAnnonce(
                annonce.getIdAnnonce(),
                annonce.getUtilisateurId(),
                annonce.getAnnonceTitre(),
                annonce.getAnnoncePrix()).enqueue(new Callback<Annonce>() {
            @Override
            public void onResponse(Call<Annonce> call, Response<Annonce> response) {
                // Si conncetion Failed
                if (!response.isSuccessful()) {
                    Log.i(TAG, "Connection Failed \nFailedCode : " + response.code());
                    return;
                }

                Log.i(TAG, "response : " + response);
                Annonce reponse = response.body();
                // Envoyer une Requête pour supprimer l'Annonce
                db.annonceDao().deleteAnnonce(reponse.getIdAnnonce());
            }

            @Override
            public void onFailure(Call<Annonce> call, Throwable t) {
                // Si erreur 404
                Log.e(TAG, t.getMessage());
            }
        });
        notifyDataSetChanged();
    }




    @Override
    public int getItemCount() {
        return listeAnnonceVendue.size();
    }

    public void addAnnonce(List<Annonce> maListe) {
        listeAnnonceVendue = maListe;
        notifyDataSetChanged();
    }

    // class intern
    static class AnnonceVendueVH extends RecyclerView.ViewHolder{

        ImageView annonceImage;
        TextView annonceTitre;
        TextView annonceDescription;
        TextView annoncePrice;
        ImageButton annonceTrash;
        CardView cardViewAnnonceVendue;

        public AnnonceVendueVH(@NonNull View itemView) {
            super(itemView);

            annonceImage = itemView.findViewById(R.id.AnnonceVendueImage);
            annonceTitre = itemView.findViewById(R.id.AnnonceVendueTitre);
            annonceDescription = itemView.findViewById(R.id.AnnonceVendueDescription);
            annoncePrice = itemView.findViewById(R.id.AnnonceVenduePrix);
            annonceTrash = itemView.findViewById(R.id.AnnonceVendueTrash);
            cardViewAnnonceVendue= itemView.findViewById(R.id.cardViewAnnonceVendue);
        }
    }
}
