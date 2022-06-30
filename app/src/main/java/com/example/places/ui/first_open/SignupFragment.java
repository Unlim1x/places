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
import android.widget.ProgressBar;
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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    ProgressBar progressBar;
    TextView textResendCode;
    TextView plusseven;
    Button change_number;
    Button resend_code;
    boolean code_accepted = false;
    int generated_code;
    byte time = 60;
    String phone_number_db;
    private static final String server_host = "192.168.0.163";
    public static final int server_port = 25565;

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
        textResendCode = binding.singupResendCodeText;
        plusseven = binding.plusseven;
        Button accept_code = binding.signupButtonAcceptCode;
        progressBar = binding.signupProgressbar;
        change_number = binding.signupButtonChangeNumber;
        resend_code = binding.signupButtonResendCode;
        code.setVisibility(View.INVISIBLE);
        accept_code.setVisibility(View.INVISIBLE);
        incorrect_code.setVisibility(View.INVISIBLE);
        phone_number.setMaxEms(12);

        change_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobile_or_code.setText("Введите номер телефона:");
                phone_number.setVisibility(View.VISIBLE);
                code.setVisibility(View.INVISIBLE);
                accept_code.setVisibility(View.INVISIBLE);
                send_code.setVisibility(View.VISIBLE);
                incorrect_code.setVisibility(View.INVISIBLE);
                change_number.setVisibility(View.INVISIBLE);
                resend_code.setVisibility(View.INVISIBLE);
                plusseven.setVisibility(View.VISIBLE);
            }
        });

        resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPhone(phone_number_db);
                textResendCode.setVisibility(View.VISIBLE);
                resendCodeGeneratorButton();
                resendCodeGeneratorText();
                resend_code.setVisibility(View.INVISIBLE);
                change_number.setVisibility(View.INVISIBLE);
            }
        });

        send_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SKOLKO MAXEMS", String.valueOf(phone_number.getMaxEms()));
                Log.i("SKOLKO DLINA NOMERA", String.valueOf(phone_number.length()));
                if (phone_number.length() == 10){


                    String phone;

                    char first = phone_number.getText().toString().charAt(0);
                    if (first == '9') {
                        phone = "7" + phone_number.getText().toString();
                        phone_number_db = phone;
                    }
                    else {
                        phone = phone_number.getText().toString();
                        if (phone_number.getText().toString().charAt(0) == '+' && phone_number.getText().toString().charAt(1) == '7'){
                            phone_number_db = phone_number.getText().toString().substring(1);
                        }
                        if (phone_number.getText().toString().charAt(0) == '8'){
                            phone_number_db = "7"+phone_number.getText().toString().substring(1);
                        }
                    }
//TODO: ПЕРЕНЕСТИ НА СЕРВЕР
                    //String s_code = i_code+" - Ваш код для авторизации в приложении Места";

                    // sendPhone(phone)
                    // sendCode(code);
                    resend_code.setVisibility(View.INVISIBLE);
                    plusseven.setVisibility(View.INVISIBLE);
                    mobile_or_code.setText("Мы отправили код в смс на номер \n" + phone + "\n Введите полученный код ниже:");
                    phone_number.setVisibility(View.INVISIBLE);
                    resend_code.setVisibility(View.INVISIBLE);
                    change_number.setVisibility(View.INVISIBLE);
                    textResendCode.setVisibility(View.VISIBLE);
                    code.setVisibility(View.VISIBLE);
                    accept_code.setVisibility(View.VISIBLE);
                    incorrect_code.setVisibility(View.INVISIBLE);
                    incorrect_code.setText("Неверный код");
                    sendPhone(phone_number_db);

                    resendCodeGeneratorButton();
                    resendCodeGeneratorText();

                }
                else{
                    incorrect_code.setText("Номер указан неверно");
                    incorrect_code.setVisibility(View.VISIBLE);
                }
            }
        });

        accept_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                if(code.getText().toString().length()==6)
                try {
                    if (sendCode(phone_number_db, code.getText().toString())){
                        incorrect_code.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        ContentValues cv = new ContentValues();
                        cv.put("first", 1);
                        database.insert("init", null, cv);

                        ContentValues pcv = new ContentValues();
                        pcv.put("username", phone_number_db);
                        pcv.put("phone", phone_number_db);
                        pcv.put("type", 1);
                        pcv.put("loggedout", 0);
                        database.insert("profiles", null, pcv);
                        Intent intent = getActivity().getIntent();
                        getActivity().finish();
                        startActivity(intent);
                    }
                    else{

                        incorrect_code.setVisibility(View.VISIBLE);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                else
                {
                    incorrect_code.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });



    }

    private void resendCodeGeneratorButton(){
        Timer myTimer;
        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            public void run() {
                timerTickButton();
            }
        }, 60000); // каждую минуту
    }
    private void resendCodeGeneratorText(){
        Timer myTimer;
        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            public void run() {
                if (time >=0)
                timerTickText();
                else{
                    time = 60;
                    return;
                }
            }
        }, 0, 1000); // каждую секунду
    }
    private void timerTickButton() {
        getActivity().runOnUiThread(doButton);
    }
    private void timerTickText() {
        getActivity().runOnUiThread(doText);
    }
    private Runnable doButton = new Runnable() {
        public void run() {
            resend_code.setVisibility(View.VISIBLE);
            change_number.setVisibility(View.VISIBLE);
            textResendCode.setVisibility(View.INVISIBLE);
        }
    };
    private Runnable doText = new Runnable() {
        public void run() {
            textResendCode.setText("Изменить номер или отправить код повторно можно через " + time-- + "c");
        }
    };

    private void sendPhone(String phone){
       //TODO: Надо какой-то апи найти для отправки смс. Я пока не понимаю как.
        CompletableFuture<Void> voidCompletableFuture;
        voidCompletableFuture = CompletableFuture.runAsync(()->{
            try {
                Socket server = new Socket(server_host, server_port);
                DataOutputStream dataOutputStream = new DataOutputStream(server.getOutputStream());
                dataOutputStream.writeUTF("phone");
                dataOutputStream.writeUTF(phone);
                server.close();

            } catch (IOException e) {
                e.printStackTrace();
                //TODO: Вывести сообщение о недоступности сервера
            }
        });
    }
    private boolean sendCode(String phone, String code) throws ExecutionException, InterruptedException {
        //TODO: Надо какой-то апи найти для отправки смс. Я пока не понимаю как.
        CompletableFuture<Boolean> supplier;
        supplier = CompletableFuture.supplyAsync(()->{
            try {
                Socket server = new Socket(server_host, server_port);
                DataOutputStream dataOutputStream = new DataOutputStream(server.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(server.getInputStream());
                dataOutputStream.writeUTF("code");
                dataOutputStream.writeUTF(phone);
                dataOutputStream.writeUTF(code);
                boolean result = dataInputStream.readBoolean();
                server.close();
                return result;

            } catch (IOException e) {
                e.printStackTrace();
                //TODO: Вывести сообщение о недоступности сервера
            }
            return false;
        });

        return supplier.get();
    }
}