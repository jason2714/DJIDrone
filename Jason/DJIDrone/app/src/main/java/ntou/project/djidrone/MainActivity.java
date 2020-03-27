package ntou.project.djidrone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText account,password;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewId();
        initLinstener();
    }
    private void initViewId(){
        account=findViewById(R.id.userId);
        password=findViewById(R.id.password);
        submit=findViewById(R.id.submit);
    }

    private void initLinstener(){
        Onclick onclick = new Onclick();
        submit.setOnClickListener(onclick);
        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_ENTER)
                    checkInformation();
                return false;
            }
        });
    }
    private void checkInformation(){
        Intent intent = null;
        if(account.getText().toString().equals("DJIDrone")&&
                password.getText().toString().equals("ntoucse")){
            intent = new Intent(MainActivity.this, MobileActivity.class);
            startActivity(intent);
        }
        else{
            Log.d(define.LOG_TAG, "account : "+account.getText().toString()+
                    "\npassword : "+password.getText().toString());
            Toast.makeText(MainActivity.this,"帳號或密碼錯誤",Toast.LENGTH_SHORT).show();
        }
    }
    private class Onclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    checkInformation();
                    break;
            }
        }
    }
}