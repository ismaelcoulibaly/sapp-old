package ca.ghost_team.sapp.navigation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.ghost_team.sapp.BaseApplication;
import ca.ghost_team.sapp.MainActivity;
import ca.ghost_team.sapp.R;
import ca.ghost_team.sapp.adapter.AnnonceAdapter;
import ca.ghost_team.sapp.database.SappDatabase;
import ca.ghost_team.sapp.databinding.LayoutHomeBinding;
import ca.ghost_team.sapp.model.Annonce;
import ca.ghost_team.sapp.model.AnnonceFavoris;
import ca.ghost_team.sapp.repository.AnnonceFavorisRepo;
import ca.ghost_team.sapp.repository.AnnonceRepo;
import ca.ghost_team.sapp.service.API.AnnonceAPI;
import ca.ghost_team.sapp.service.API.AnnonceFavorisAPI;
import ca.ghost_team.sapp.service.SappAPI;
import ca.ghost_team.sapp.viewmodel.AnnonceViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ca.ghost_team.sapp.BaseApplication.ID_USER_CURRENT;

public class Home extends Fragment {
    private LayoutHomeBinding binding;
    private AnnonceAdapter adapter;
    private final String TAG = Home.class.getSimpleName();
    private AnnonceViewModel annonceViewModel;
    private RecyclerView recyclerViewAnnonce;
    private RelativeLayout filterAll;
    private ImageButton filterPant;
    private ImageButton filterTshirt;
    private ImageButton filterHoodie;
    private ImageButton filterCap;
    private ImageButton filterShort;
    private ImageButton filterMore;
    private TextView filterAllText;
    private MainActivity activity;
    private SappDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((MainActivity) context).setTitle(R.string.home);
        this.activity = (MainActivity) getActivity();

        // Recuperation de toutes les annonces
        SappAPI.getApi().create(AnnonceAPI.class)
                .getAllAnnonceViaAPI()
                .enqueue(new Callback<List<Annonce>>() {
                    @Override
                    public void onResponse(Call<List<Annonce>> call, Response<List<Annonce>> response) {
                        // Si conncetion Failed
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "Connection Failed \nFailedCode : " + response.code());
                            return;
                        }
                        List<Annonce> newAnnonce = response.body();
                        Log.i(TAG, "newAnnonce : " + newAnnonce);
                        // inserer l'annonce dans la base de données locale via le Repository
                        for (Annonce annonce : newAnnonce) {
                            new AnnonceRepo(activity.getApplication()).insertAnnonce(annonce);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Annonce>> call, Throwable t) {
                        // Si erreur 404
                        Log.e(TAG, t.getMessage());
                    }
                });

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

                        for (AnnonceFavoris annonce: annonceFavorisList) {
                            // Ajouter (ou insérer l'enregistrement dans la Table des Annonces Favories)
                            //insertIntoDatabaseWeb(annonce.getAnnonceId());
                            int c = db.annonceFavorisDao().getAnnonceFavorisIfExist(annonce.getAnnonceId(), ID_USER_CURRENT);
                            if(c == 0)
                                new AnnonceFavorisRepo(activity.getApplication()).insertLiked(annonce);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AnnonceFavoris>> call, Throwable t) {
                        // Si erreur 404
                        Log.e(TAG, t.getMessage());
                    }
                });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.db = Room.databaseBuilder(getContext(), SappDatabase.class, BaseApplication.NAME_DB)
                .allowMainThreadQueries().build();

        // Binding Fields
        filterAll = binding.filterAll;
        filterCap = binding.filterCap;
        filterHoodie = binding.filterHoodie;
        filterPant = binding.filterPant;
        filterMore = binding.filterMore;
        filterShort = binding.filterShort;
        filterTshirt = binding.filterTshirt;
        filterAllText = binding.filterAllText;

        // Evenements au Clic
        filterAll.setOnClickListener(this::filterAllAnnonce);
        filterCap.setOnClickListener(this::filterCapAnnonce);
        filterHoodie.setOnClickListener(this::filterHoodieAnnonce);
        filterPant.setOnClickListener(this::filterPantAnnonce);
        filterMore.setOnClickListener(this::filterMoreAnnonce);
        filterShort.setOnClickListener(this::filterShortAnnonce);
        filterTshirt.setOnClickListener(this::filterTshirtAnnonce);

        // Par defaut, on demarre sur les filterALL
        filterAll.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterAllText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

        // Définition du RecyclerView
        recyclerViewAnnonce = binding.recyclerViewHome;
        GridLayoutManager grid = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerViewAnnonce.setLayoutManager(grid);

        // Définition de l'Adapter et ViewModel
        adapter = new AnnonceAdapter(getActivity());
        annonceViewModel = new ViewModelProvider(this).get(AnnonceViewModel.class);

        // Lire les contenues de la LiveData de toutes les Annonces depuis la base de données
        annonceViewModel.getAllAnnonces().observe(getViewLifecycleOwner(), annonces -> {
            adapter.addAnnonce(annonces);
            adapter.notifyDataSetChanged();
            Log.i(TAG, "Finish put Annonces in RecyclerView");
        });

        // Setter toutes les modifications de l'Adapter dans le RecyclerView pour l'Affichage
        recyclerViewAnnonce.setAdapter(adapter);







    }

    /**
     * Methode FilterList
     * <p>
     * Methode assez générique qui permet de filter les Annonces selon le ID Categorie passée ne paramètre
     *
     * @param categorie : La categorie ciblée
     * @return void
     */
    public void filterList(int categorie) {
        List<Annonce> annonceFilteredList = new ArrayList<>();
        annonceViewModel.getAllAnnonces().observe(getViewLifecycleOwner(), annonces -> {
            for (Annonce annonce : annonces) {
                if (annonce.getCategorieId() == categorie) {
                    annonceFilteredList.add(annonce);
                }
            }
            adapter.addAnnonce(annonceFilteredList);
            recyclerViewAnnonce.setAdapter(adapter);
            Log.i(TAG, "Method Filter Generic finished");
        });
    }

    private void filterPantAnnonce(View view) {
        initColorFilter();
        filterPant.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(1);
    }

    private void filterTshirtAnnonce(View view) {
        initColorFilter();
        filterTshirt.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(2);
    }

    private void filterHoodieAnnonce(View view) {
        initColorFilter();
        filterHoodie.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(3);
    }

    private void filterShortAnnonce(View view) {
        initColorFilter();
        filterShort.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(4);
    }

    private void filterCapAnnonce(View view) {
        initColorFilter();
        filterCap.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(5);
    }

    private void filterMoreAnnonce(View view) {
        initColorFilter();
        filterMore.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterList(6);
    }

    private void filterAllAnnonce(View view) {
        initColorFilter();
        filterAll.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_yellow));
        filterAllText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        annonceViewModel.getAllAnnonces().observe(getViewLifecycleOwner(), annonces -> {
            adapter.addAnnonce(annonces);
            Log.i(TAG, "Finish put Annonces in RecyclerView");
        });
        recyclerViewAnnonce.setAdapter(adapter);
    }

    /**
     * Methode qui permet de réinitilaiser les couleurs de Background de les ImageButton lorsqu'on clique sur un filtre en particulier
     *
     * @return void
     */
    private void initColorFilter() {
        filterAll.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_white_rounded_dark));
        filterAllText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        filterTshirt.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
        filterShort.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
        filterPant.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
        filterHoodie.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
        filterMore.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
        filterCap.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white_rounded_dark));
    }
}
