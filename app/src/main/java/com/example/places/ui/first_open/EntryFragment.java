package com.example.places.ui.first_open;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.places.MainActivity;
import com.example.places.R;
import com.example.places.databinding.FragmentEntryBinding;


public class EntryFragment extends Fragment {

    FragmentEntryBinding binding;
    private static final String ARG_PARAM1 = "param1";




    public static EntryFragment newInstance(int param1) {
        EntryFragment fragment = new EntryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

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
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView already_have = getView().findViewById(R.id.entry_text_profile_already_exist);
        Button signup = getView().findViewById(R.id.entry_signup_button);
        Button signin = getView().findViewById(R.id.entry_signin_button);
        Button local = getView().findViewById(R.id.entry_local_profile_button);
        ImageView local_info = getView().findViewById(R.id.entry_local_profile_info_button);

        already_have.setVisibility(View.INVISIBLE);
        signup.setVisibility(View.INVISIBLE);
        signin.setVisibility(View.INVISIBLE);
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


        Animation animation_hello = AnimationUtils.loadAnimation(getContext(), R.anim.text_anim_rise_fade);
        Animation animation_permissions = AnimationUtils.loadAnimation(getContext(), R.anim.text_anim_rise);
        Animation animation_permission_fade = AnimationUtils.loadAnimation(getContext(), R.anim.text_anim_fade);
        Animation animation_profile = AnimationUtils.loadAnimation(getContext(), R.anim.text_anim_rise);

        animation_hello.setAnimationListener(new Animation.AnimationListener() {
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
                button.setText("Хорошо");
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
                signin.setVisibility(View.VISIBLE);
                local_info.setVisibility(View.VISIBLE);
                already_have.setVisibility(View.VISIBLE);

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
                Toast.makeText(getActivity(), "Вы сможете пользоваться приложением, но не сможете добавить друзей," +
                                " поделиться с ними местами, а данные будут храниться только на Вашем устройстве",
                        Toast.LENGTH_LONG).show();
            }
        });

        hello.startAnimation(animation_hello);



    }
}