package com.example.places.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.places.MainActivity;
import com.example.places.R;
import com.example.places.SettingsActivity;
import com.example.places.SigninActivity;
import com.example.places.databinding.FragmentMainBinding;
import com.example.places.databinding.FragmentPlacesBinding;
import com.example.places.databinding.FragmentProfileBinding;
import com.example.places.ui.first_open.EntryFragment;
import com.example.places.ui.first_open.SignupFragment;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    private static final String ARG_PARAM1 = "param1";
    SQLiteDatabase database;
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
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = ((MainActivity)getActivity()).getDataBase();
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
                                        Log.i("deleting",""+ database.delete("tracker", null, null));
                                        Log.i("deleting",""+ database.delete("markers", null, null));
                                        ContentValues cv = new ContentValues();
                                        cv.put("loggedout", 1);
                                        database.update("profiles", cv, "loggedout = 0", null);
                                        cv.put("loggedout", 0);
                                        database.update("profiles", cv, "loggedout = 1", null);
                                        getActivity().finish();
                                        getActivity().recreate();
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