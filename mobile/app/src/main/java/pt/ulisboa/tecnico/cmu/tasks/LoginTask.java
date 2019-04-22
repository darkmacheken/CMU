package pt.ulisboa.tecnico.cmu.tasks;

import android.content.Context;
import android.os.AsyncTask;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.ulisboa.tecnico.cmu.R;
import pt.ulisboa.tecnico.cmu.utils.HttpUtils;

public class LoginTask extends AsyncTask<Void, Void, Void> {
    private final Context context;

    public LoginTask(Context context) {
        this.context = context;
    }

    protected Void doInBackground(Void... urls) {
        OkHttpClient client = HttpUtils.getHttpClient(context);

        RequestBody body = RequestBody.create(HttpUtils.JSON, "{\"username\": \"pedro.daniel10@hotmail.com\"}");
        Request request = new Request.Builder()
            .url(context.getResources().getString(R.string.server_url) + "/login")
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();
            response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}