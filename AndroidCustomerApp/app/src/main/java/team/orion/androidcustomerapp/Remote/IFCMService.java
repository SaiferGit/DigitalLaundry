package team.orion.androidcustomerapp.Remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import team.orion.androidcustomerapp.Model.FCMResponse;
import team.orion.androidcustomerapp.Model.Sender;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAZF5GnSM:APA91bH94Z1db-LGrvVUYxj0g7Sr27IA83SwirK6Nl_C0Vw55-LWzXN131ubI8jUG0yvbq_O8aCa3p2WGjNCj2TZ7OPH3Qco81gfbK4Ox6gDquWOMJtdzky43oSIaUYt1xxk3xgD1XBq"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessege(@Body Sender body);
}
