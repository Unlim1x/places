package ru.lim1x.places.ui.first_open;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ru.lim1x.places.activities.MainActivity;
import ru.lim1x.places.databinding.FragmentEntryBinding;
import ru.lim1x.places.room.App;

import ru.lim1x.places.R;

import ru.lim1x.places.room.daos.InitAppDao;
import ru.lim1x.places.room.daos.ProfileDao;
import ru.lim1x.places.room.database.PlacesDatabase;
import ru.lim1x.places.room.entities.InitApp;
import ru.lim1x.places.room.entities.Profile;
import ru.lim1x.places.ui.dialogs.OneButtonDialog;


public class EntryFragment extends Fragment {

    FragmentEntryBinding binding;
    PlacesDatabase database;
    InitAppDao initAppDao;
    ProfileDao profileDao;
    SharedPreferences mSettings;
    private static final String ARG_PARAM1 = "param1";

    public EntryFragment(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root;
        binding = FragmentEntryBinding.inflate(inflater, container, false);
        mSettings = getContext().getSharedPreferences("s1paraX", Context.MODE_PRIVATE);
        root = binding.getRoot();
        database = App.getInstance().getDatabase();
        initAppDao = database.initAppDao();
        profileDao = database.profileDao();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Button signup = getView().findViewById(R.id.entry_signup_button);

        Button local = getView().findViewById(R.id.entry_local_profile_button);
        ImageView local_info = getView().findViewById(R.id.entry_local_profile_info_button);


        signup.setVisibility(View.INVISIBLE);

        local.setVisibility(View.INVISIBLE);
        local_info.setVisibility(View.INVISIBLE);

        TextView hello = getView().findViewById(R.id.entry_text_hello);
        TextView permission = getView().findViewById(R.id.entry_text_permission);
        TextView profile = getView().findViewById(R.id.entry_text_profile);
        Button button = getView().findViewById(R.id.entry_button);
        hello.setVisibility(View.INVISIBLE);
        permission.setVisibility(View.INVISIBLE);
        profile.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);


        Animation animation_hello = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.text_anim_rise);
        Animation animation_hello_fade = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.text_anim_fade);
        Animation animation_permissions = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.text_anim_rise);
        Animation animation_permission_fade = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.text_anim_fade);
        Animation animation_profile = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.text_anim_rise);

        animation_hello.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                hello.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                hello.clearAnimation();
                hello.startAnimation(animation_hello_fade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation_hello_fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                hello.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hello.setVisibility(View.INVISIBLE);
                permission.startAnimation(animation_permissions);
                hello.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_permissions.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                permission.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                button.setVisibility(View.VISIBLE);
                permission.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_profile.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                profile.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                profile.clearAnimation();
                signup.setVisibility(View.VISIBLE);
                local.setVisibility(View.VISIBLE);

                local_info.setVisibility(View.VISIBLE);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_permission_fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                button.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                permission.setVisibility(View.INVISIBLE);
                profile.startAnimation(animation_profile);
                permission.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).getLocationPermission();
                permission.startAnimation(animation_permission_fade);
            }
        });

        local_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        /*
                        Toast.makeText(getApplicationContext(), "Вы сможете пользоваться приложением, но не сможете добавить друзей," +
                                        " поделиться с ними местами, а данные будут храниться только на Вашем устройстве",
                                Toast.LENGTH_LONG).show();
                        */
                OneButtonDialog infoDialog = new OneButtonDialog("Локальный профиль",
                        "Вы сможете пользоваться приложением, но не сможете добавить друзей," +
                                " поделиться с ними местами, а данные будут храниться только на Вашем устройстве.",
                        "Понятно", R.drawable.info);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                infoDialog.show(fragmentTransaction, "info_local");
            }
        });

        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean("auth", true);
                    editor.apply();
                    InitApp initApp = new InitApp();
                    initApp.first = 1;
                    initAppDao.insert(initApp);
                    Profile profile = new Profile();
                    profile.username = "Локальный профиль";
                    profile.phone = "null";
                    profile.type = 0;
                    profile.loggedout =0;
                    profileDao.insert(profile);
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);
                    return;


            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).openSignin();
            }
        });



        hello.startAnimation(animation_hello);



    }
}