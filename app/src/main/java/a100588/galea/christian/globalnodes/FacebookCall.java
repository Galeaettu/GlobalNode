package a100588.galea.christian.globalnodes;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;

/**
 * Created by Chris on 14/05/2017.
 */
public class FacebookCall {
    public void facebookTest(){
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken()
                , new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        response.getJSONArray();
                        Log.d("FB TEST",response.getJSONArray().toString() );
                    }
                });
        request.executeAsync();
        Log.d("FB TEST",request.toString());
    }
}
