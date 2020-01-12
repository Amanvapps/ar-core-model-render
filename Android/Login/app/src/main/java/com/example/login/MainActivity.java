package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;




public class MainActivity extends AppCompatActivity {

    private EditText Username , Password ;
    private Button LoginButton ;
    JSONObject jsonObject ;

    ApiServices apiServices ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Username = (EditText) findViewById(R.id.username_edit_text);
        Password = (EditText) findViewById(R.id.password_edit_text);
        LoginButton = (Button)findViewById(R.id.login_button);



        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://stage.blocedu.com/api-auth/api/v1/user/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();



        apiServices = retrofit.create(ApiServices.class);

        Toast.makeText(getApplicationContext() , "yes" , Toast.LENGTH_SHORT).show();

        LoginButton.setOnClickListener(new View.OnClickListener() {

                    @Override
            public void onClick(View v) {


                  login();
            }
        });




    }


    private void login()
    {
        Toast.makeText(getApplicationContext(), "Inside login method", Toast.LENGTH_SHORT).show();
           createPost();
    }




    private void createPost() {
        Login post = new Login("student007@yopmail.com" , "Testing@123");




        Gson gson = new Gson();
        String json = gson.toJson(post);

        Username.setText(json);

        try {
            jsonObject = new JSONObject(json);
        }catch (JSONException err){
            Log.d("Error", err.toString());
        }





        Call<Login> call = apiServices.createLogin(post);

        call.enqueue(new Callback<Login>()
        {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response)
            {
                if(!response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Code : " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }


                int content = response.code();

                if(content == 200)
                {
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Login Unsucessful" , Toast.LENGTH_SHORT).show();
                }




            }

            @Override
            public void onFailure(Call<Login> call, Throwable t) {
              //  textView.setText(t.getMessage());



            }
        });



    }


}
