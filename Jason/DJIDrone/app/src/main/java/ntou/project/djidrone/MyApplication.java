package ntou.project.djidrone;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MyApplication.this);
    }
}

