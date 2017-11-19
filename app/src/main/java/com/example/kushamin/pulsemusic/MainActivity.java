package com.example.kushamin.pulsemusic;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.UUID;

import kaaes.spotify.webapi.android.*;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistsCursorPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.QueryMap;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback{

    private static final String CLIENT_ID = "0346290c54c64ae08bb913ebbf1a228a";
    private static final String REDIRECT_URI = "heartcustomprotocol://callback";
    private Response savedTracks;
    private String favArtists;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    final UUID appUuid = UUID.fromString("20b1d1d1-178a-40d1-8b9b-a596f2ae13a4");
    //boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    // Most (but not all) of the Spotify Web API endpoints require authorisation.
    private static final int REQUEST_CODE = 1337;
    AuthenticationRequest.Builder builder =
            new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

    private Player mPlayer;

    // Create a new receiver to get AppMessages from the C app
    PebbleKit.PebbleDataReceiver dataReceiver = new PebbleKit.PebbleDataReceiver(appUuid) {

        @Override
        public void receiveData(Context context, int transaction_id,
                                PebbleDictionary dict) {
            // A new AppMessage was received, tell Pebble
            PebbleKit.sendAckToPebble(context, transaction_id);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming","user-follow-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        // Most (but not all) of the Spotify Web API endpoints require authorisation.
        // If you know you'll only use the ones that don't require authorisation you can skip this step


        /*
        // Create a new dictionary
        PebbleDictionary dict = new PebbleDictionary();
        // The key representing a contact name is being transmitted
        final int AppKeyContactName = 0;
        final int AppKeyAge = 1;

        // Get data from the app
        //final String contactName = getContact();
        final int age = getAge();

        // Add data to the dictionary
        dict.addString(AppKeyContactName, contactName);
        dict.addInt32(AppKeyAge, age);

        // Send the dictionary
        PebbleKit.sendDataToPebble(getApplicationContext(), appUuid, dict);
        */



        spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                Log.d("Album success", album.name);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });

        //This doesn't work properly but it doesnt rly matter
        spotify.getMySavedTracks(new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                 //handle successful response
                savedTracks = response;
            }

            @Override
            public void failure(SpotifyError error) {
                 //handle error
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {


                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                        api.setAccessToken(response.getAccessToken());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }

                });
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // Register the receiver
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);
    }

    /*
    @Override
    public void receiveData(Context context, int transaction_id,
                            PebbleDictionary dict) {
        // If the tuple is present...
        Long ageValue = dict.getInteger(AppKeyAge);
        if(ageValue != null) {
            // Read the integer value
            int age = ageValue.intValue();
        }
    }
    */

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {


        Log.d("MainActivity", "User logged in");
        //This is not busted and important
        spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", new SpotifyCallback<Album>() {
            @Override
            public void success(Album album, Response response) {
                Log.d("Album success", album.name);
            }

            @Override
            public void failure(SpotifyError error) {
                Log.d("Album failure", error.toString());
            }
        });


        mPlayer.playUri(null, "spotify:track:7GhIk7Il098yCjg4BQjzvb", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }


}
