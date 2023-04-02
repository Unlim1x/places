package ru.lim1x.places.ui.first_open;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.lim1x.places.activities.MainActivity;
import ru.lim1x.places.back.http.CodeGenerator;
import ru.lim1x.places.back.http.PClient;
import ru.lim1x.places.databinding.FragmentSignupBinding;
import ru.lim1x.places.room.App;
import ru.lim1x.places.room.daos.InitAppDao;
import ru.lim1x.places.room.daos.ProfileDao;
import ru.lim1x.places.room.database.PlacesDatabase;
import ru.lim1x.places.room.entities.InitApp;
import ru.lim1x.places.room.entities.Profile;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;



public class SignupFragment extends Fragment {

    FragmentSignupBinding binding;
    PlacesDatabase database;
    ProfileDao profileDao;
    InitAppDao initAppDao;
    Button send_code;
    ProgressBar progressBar;
    TextView textResendCode;
    TextView plusseven;
    Button change_number;
    Button resend_code;
    Timer buttonTimer;
    Timer textTimer;
    PClient client;
    EditText[] otpETs = new EditText[6];
    boolean code_accepted = false;

    byte time = 60;
    String phone_number_db;
    int attempts = 0;
    public SignupFragment(){
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = App.getInstance().getDatabase();
        profileDao = database.profileDao();
        initAppDao = database.initAppDao();
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


    private int checkWhoHasFocus() {
        for (int i = 0; i < otpETs.length; i++) {
            EditText tempET = otpETs[i];
            if (tempET.hasFocus()) {
                return i;
            }
        }
        return 123;
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
        client = new PClient();

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
                    //binding.layoutWithCode.setVisibility(View.VISIBLE);

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
                       // accept_code.setEnabled(false);
                        incorrect_code.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);

                        Runnable runnable = ()-> {

                            if (initAppDao.getInit() == 0) {
                                InitApp initApp = new InitApp();
                                initApp.first = 1;
                                initAppDao.insert(initApp);
                            }

                            Profile localProfile = profileDao.getLocal();
                            localProfile.loggedout = 1;
                            profileDao.update(localProfile);

                            Profile auth_profile = new Profile();
                            auth_profile.username = phone_number_db;
                            auth_profile.phone = phone_number_db;
                            auth_profile.type = 1;
                            auth_profile.loggedout = 0;

                            Profile is_here = profileDao.getByPhone(phone_number_db);
                            if (is_here!=null)
                                profileDao.update(auth_profile);
                            else
                                profileDao.insert(auth_profile);

                            if (buttonTimer != null)
                                buttonTimer.cancel();

                            if (textTimer != null)
                                textTimer.cancel();

                            // Intent intent = getActivity().getIntent();
                            progressBar.setVisibility(View.INVISIBLE);
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            return;
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();

                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
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
        buttonTimer = myTimer;

        myTimer.schedule(new TimerTask() {
            public void run() {
                timerTickButton();
            }
        }, 60000); // каждую минуту
    }
    Timer myTimer;
    private void resendCodeGeneratorText(){

        myTimer = new Timer();
        textTimer = myTimer;
        myTimer.schedule(new TimerTask() {
            public void run() {
                if (time >=0)
                timerTickText();
                else{
                    myTimer.cancel();
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

    @Override
    public void onStop(){
        super.onStop();
        if (myTimer != null)
            myTimer.cancel();

    }


    private void sendPhone(String phone){
        CompletableFuture.runAsync(() -> {
            client.setCode(CodeGenerator.generateCode());
            client.setPhone(phone);
            requireActivity().runOnUiThread(() -> {
                Toast msg = Toast.makeText(getContext(), client.getCode(), Toast.LENGTH_LONG);
                msg.show();
            });
            //client.send();
        });
    }
    private boolean sendCode(String phone, String code) throws ExecutionException, InterruptedException {
        return code.equals(client.getCode());
    }
}