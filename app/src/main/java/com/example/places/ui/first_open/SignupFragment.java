package com.example.places.ui.first_open;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.places.MainActivity;
import com.example.places.R;
import com.example.places.databinding.FragmentSignupBinding;
import com.example.places.ui.dialogs.OneButtonDialog;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.dezhik.sms.sender.SenderService;
import ru.dezhik.sms.sender.SenderServiceConfiguration;
import ru.dezhik.sms.sender.SenderServiceConfigurationBuilder;
import ru.dezhik.sms.sender.api.InvocationStatus;
import ru.dezhik.sms.sender.api.smsru.SMSRuResponseStatus;
import ru.dezhik.sms.sender.api.smsru.cost.SMSRuCostRequest;
import ru.dezhik.sms.sender.api.smsru.cost.SMSRuCostResponse;


public class SignupFragment extends Fragment {

    FragmentSignupBinding binding;
    SQLiteDatabase database;
    Button send_code;
    int generated_code;
    String phone_number_db;
    private static final String ARG_PARAM1 = "param1";

    public SignupFragment(SQLiteDatabase database){
        this.database = database;
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
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView mobile_or_code = binding.signupTextEnterMobileOrCode;
        TextView incorrect_code = binding.singupIncorrectCode;
        EditText phone_number = binding.phonenumber;
        EditText code = binding.signupCode;
        send_code = binding.signupButtonSendCode;
        Button accept_code = binding.signupButtonAcceptCode;

        code.setVisibility(View.INVISIBLE);
        accept_code.setVisibility(View.INVISIBLE);
        incorrect_code.setVisibility(View.INVISIBLE);
        phone_number.setMaxEms(12);

        send_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SKOLKO MAXEMS", String.valueOf(phone_number.getMaxEms()));
                Log.i("SKOLKO DLINA NOMERA", String.valueOf(phone_number.length()));
                if (phone_number.getMaxEms()==phone_number.length() || phone_number.getMaxEms()== phone_number.length()-1
                || phone_number.getMaxEms()== phone_number.length()-2){

                    int i_code = generateCode();
                    generated_code = i_code;
                    String phone;

                    char first = phone_number.getText().charAt(0);
                    if (first == '9') {
                        phone = "8" + phone_number.getText().toString();
                        phone_number_db = phone;
                    }
                    else {
                        phone = phone_number.getText().toString();
                        if (phone_number.getText().charAt(0) == '+'){
                            phone_number_db = "8"+phone_number.getText().toString().substring(2);
                        }
                        if (phone_number.getText().charAt(0) == '7'){
                            phone_number_db = "8"+phone_number.getText().toString().substring(1);
                        }
                    }

                    String s_code = i_code+" - Ваш код для авторизации в приложении Места";
                    sendCode(phone, s_code);


                    mobile_or_code.setText("Мы отправили код в смс на номер \n" + phone + "\n Введите полученный код ниже:");
                    phone_number.setVisibility(View.INVISIBLE);
                    code.setVisibility(View.VISIBLE);
                    accept_code.setVisibility(View.VISIBLE);

                }
            }
        });

        accept_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (generated_code == Integer.parseInt(code.getText().toString())) {
                //TODO: Во внешнюю БД сохранить пользователя!
                    try {
                        Connection connection = DriverManager.getConnection(
                                "jdbc:postgresql://192.168.0.162:25565/banking_db", "postgres", "postgres");
                        if (connection != null) {
                            Log.i("DB","Connected to the database!");
                        } else {
                            Log.i("DB","Failed to make connection!");
                        }
                        String query = "SELECT COUNT (*) FROM users WHERE phone_number = " + phone_number_db;
                        List<String> strings = new ArrayList<>();
                        PreparedStatement statement = connection.prepareStatement(query);
                        boolean hasResult = statement.execute();
                        if (hasResult){
                            ResultSet resultSet = statement.getResultSet();
                            while(resultSet.next()) {
                                if (resultSet.getInt(1) == 0){
                                    ContentValues cv = new ContentValues();
                                    cv.put("first", 2);
                                    database.insert("init", null, cv);

                                    ContentValues pcv = new ContentValues();
                                    pcv.put("username", phone_number_db);
                                    pcv.put("password", "default");
                                    pcv.put("type", 1);
                                    pcv.put("loggedout", 0);
                                    database.insert("profiles", null, pcv);
                                    query = "INSERT INTO users (phone_number, username, image_root)" +
                                            "VALUES (?, ?, ?)";
                                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                                    preparedStatement.setString(1, phone_number_db);
                                    preparedStatement.setString(2, phone_number_db);
                                    preparedStatement.setString(3, "default");
                                    preparedStatement.executeUpdate();
                                }
                            }

                        }
                    } catch (SQLException e) {
                        System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
                        ContentValues cv = new ContentValues();
                        cv.put("first", 2);
                        database.insert("init", null, cv);

                        ContentValues pcv = new ContentValues();
                        pcv.put("username", phone_number_db);
                        pcv.put("password", "default");
                        pcv.put("type", 1);
                        pcv.put("loggedout", 0);
                        database.insert("profiles", null, pcv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                database.close();
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });



    }

    private int generateCode(){
        double rand = Math.random();
        long result = Math.round(rand*1000000);
        return (int)result;
    }

    private void sendCode(String phone, String code){
       //TODO: Надо какой-то апи найти для отправки смс. Я пока не понимаю как.
    }
}