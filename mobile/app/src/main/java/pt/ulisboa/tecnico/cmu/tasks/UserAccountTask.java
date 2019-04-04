package pt.ulisboa.tecnico.cmu.tasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

public class UserAccountTask extends AsyncTask<Void, Void, FullAccount> {

    private DbxClientV2 dbxClient;
    private TaskDelegate  delegate;
    private Exception error;

    public interface TaskDelegate {
        void onError(Exception error);
    }

    public UserAccountTask(DbxClientV2 dbxClient, TaskDelegate delegate){
        this.dbxClient =dbxClient;
        this.delegate = delegate;
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            //get the users FullAccount
            return dbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account == null || error != null){
            // Something went wrong
            delegate.onError(error);
        }
    }
}
