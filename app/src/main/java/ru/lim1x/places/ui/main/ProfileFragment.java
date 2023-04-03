package ru.lim1x.places.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.lim1x.places.activities.MainActivity;
import ru.lim1x.places.activities.SettingsActivity;
import ru.lim1x.places.activities.SigninActivity;
import ru.lim1x.places.databinding.FragmentProfileBinding;
import ru.lim1x.places.room.App;

import ru.lim1x.places.R;

import ru.lim1x.places.room.daos.MarkerDao;
import ru.lim1x.places.room.daos.ProfileDao;
import ru.lim1x.places.room.daos.TrackerDao;
import ru.lim1x.places.room.database.PlacesDatabase;
import ru.lim1x.places.room.entities.Profile;

import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    private static final String ARG_PARAM1 = "param1";
    PlacesDatabase database;
    ProfileDao profileDao;
    MarkerDao markerDao;
    TrackerDao trackerDao;
    Context mContext;

    // TODO: Rename and change types of parameters
    private String mParam1;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.

     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(int param1) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getContext();
        View root;
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        database = App.getInstance().getDatabase();
        profileDao = database.profileDao();
        markerDao = database.markerDao();
        trackerDao = database.trackerDao();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        TextView username = getView().findViewById(R.id.username);
        username.setText(((MainActivity)getActivity()).getUsername());


        if(((MainActivity) getActivity()).profileType() !=1) {
            binding.signinButton.setVisibility(View.VISIBLE);
            binding.logoutButton.setVisibility(View.INVISIBLE);
        }
        else {
            binding.signinButton.setVisibility(View.INVISIBLE);
            binding.logoutButton.setVisibility(View.VISIBLE);
        }
        Log.i("PIDARAS", ((MainActivity) getActivity()).profileType() + "");

        binding.signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SigninActivity.class));
                getActivity().recreate();
            }
        });

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View inputDialogView = li.inflate(R.layout.clearing_dialog, null);
                AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(getContext());
                aDialogBuilder.setView(inputDialogView);
                aDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                            markerDao.nukeTable();
                                            trackerDao.nukeTable();

                                            List<Profile> profiles = profileDao.getAll();
                                            Iterator<Profile> profileIterator = profiles.iterator();

                                            while(profileIterator.hasNext()){
                                                Profile profile = profileIterator.next();
                                                profile.loggedout = 1;
                                                profileDao.update(profile);

                                            }
                                        getActivity().recreate();
                                        return;

                                    }
                                })
                        .setNegativeButton("Отменить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog inputDialog = aDialogBuilder.create();
                TextView header = inputDialogView.findViewById(R.id.clearing_dialog_header);
                TextView text = inputDialogView.findViewById(R.id.clearing_text);
                header.setText("Выход");
                text.setText("Все данные будут потеряны. Вы уверены?");
                inputDialog.show();

            }
        });


        Button settings_button = binding.settingsButton;

        settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingsActivity.class));
            }
        });


    }
}