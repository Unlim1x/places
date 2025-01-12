package ru.lim1x.places.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import ru.lim1x.places.R;
import ru.lim1x.places.databinding.FragmentMainBinding;
import ru.lim1x.places.ui.first_open.SignupFragment;

public class SigninActivity extends AppCompatActivity {

    FragmentMainBinding fbinding;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        fbinding = FragmentMainBinding.inflate(getLayoutInflater());
        setContentView(fbinding.getRoot());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new SignupFragment())
                .commit();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
