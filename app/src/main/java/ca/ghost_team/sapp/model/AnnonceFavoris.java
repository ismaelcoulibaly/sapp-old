package ca.ghost_team.sapp.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName="AnnonceFavoris",primaryKeys = {"utilisateurId","annonceId"},
        foreignKeys = {
                @ForeignKey(entity = Utilisateur.class,
                        parentColumns = "idUtilisateur",
                        childColumns = "utilisateurId"),
                @ForeignKey(entity = Annonce.class,
                        parentColumns = "idAnnonce",
                        childColumns = "annonceId")
        })
public class AnnonceFavoris {
    public int annonceId;
    public int utilisateurId;

    public AnnonceFavoris(int utilisateurId, int annonceId) {
        this.utilisateurId = utilisateurId;
        this.annonceId = annonceId;
    }

    public int getAnnonceId() {
        return annonceId;
    }

    public void setAnnonceId(int annonceId) {
        this.annonceId = annonceId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

}
