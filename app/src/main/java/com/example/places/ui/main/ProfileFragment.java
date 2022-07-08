package com.example.places.ui.main;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.places.databinding.FragmentPlacesBinding;
import com.example.places.databinding.FragmentProfileBinding;

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

        Button delete_tracker_data = binding.deleteTrackerData;
        delete_tracker_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("deleting",""+database.delete("tracker", null, null));
            }
        });

        binding.deletePlacesData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("deleting",""+database.delete("markers", null, null));
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