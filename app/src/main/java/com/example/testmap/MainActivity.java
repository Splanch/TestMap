package com.example.testmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        requestPermissionsIfNecessary(new String[]{
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        final IMapController mapController = map.getController();
        mapController.setZoom(13);
        GeoPoint startPoint = new GeoPoint(48.1113387, -1.6800198);
        mapController.setCenter(startPoint);


        final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);

        // Click sur le bouton "map_center, centre la carte sur la position .
        ImageButton btnCenterOnLocation = findViewById(R.id.ic_map_center);
        btnCenterOnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final GeoPoint myLocation = mLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.getController().animateTo(myLocation);
                        }
                    });
                }
            }
        });

        // Set la carte à la localisation de base
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                final GeoPoint myLocation = mLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.getController().animateTo(myLocation);
                        }
                    });
                }
                ;
            }
        });
        map.getOverlays().add(mLocationOverlay);

    }

    //Get latitude and longitute when clic

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionType = ev.getAction();
        switch (actionType) {
            case MotionEvent.ACTION_UP:
                Projection proj = map.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int) ev.getX(), (int) ev.getY());
                String longitude = Double.toString(((double) loc.getLongitude()) / 1000000);
                String latitude = Double.toString(((double) loc.getLatitude()) / 1000000);

                Toast toast = Toast.makeText(getApplicationContext(), "Longitude: " + longitude + " Latitude: " + latitude, Toast.LENGTH_LONG);
                toast.show();

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void goAilleurs(View view) {
        startActivity(new Intent(this, PhotoActivity.class));

    }
}