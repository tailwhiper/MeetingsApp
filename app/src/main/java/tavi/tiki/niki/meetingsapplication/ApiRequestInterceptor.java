package tavi.tiki.niki.meetingsapplication;

/**
 * Created by Никита on 25.12.2015.
 */
import android.util.Base64;
import retrofit.RequestInterceptor;

/**
 * Interceptor used to authorize requests.
 */
public class ApiRequestInterceptor implements RequestInterceptor {
    private String userName;
    private String password;

    public ApiRequestInterceptor(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void intercept(RequestFacade requestFacade) {

        if ((userName != null)&&(password!=null)) {
            final String authorizationValue = encodeCredentialsForBasicAuthorization();
            requestFacade.addHeader("Authorization", authorizationValue);
        }
    }

    private String encodeCredentialsForBasicAuthorization() {
        final String userAndPassword = userName + ":" + password;
        return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
    }


}