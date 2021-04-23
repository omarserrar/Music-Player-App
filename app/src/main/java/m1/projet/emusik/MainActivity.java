package m1.projet.emusik;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.Settings;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import at.markushi.ui.CircleButton;
import m1.projet.emusik.Server.Song;
import m1.projet.emusik.Server.StreamServer;
import m1.projet.emusik.Server.StreamServerPrx;
import m1.projet.emusik.Server._StreamServerPrxI;
import m1.projet.emusik.client.ClientSession;


public class MainActivity extends AppCompatActivity {
    private  AsrController asrController;
    CircleButton record, stop, valid;
    TextView resultText, commandText;
    public FragmentContainerView fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initAsrController();
    }

    public void addFragment(int id, Bundle bundle){
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        if(bundle != null)
            navController.navigate(id, bundle);
        else
            navController.navigate(id);
    }
    protected void initAsrController(){
        record = findViewById(R.id.record);
        stop = findViewById(R.id.stop);
        valid = findViewById(R.id.valid);
        resultText = findViewById(R.id.resultText);
        commandText = findViewById(R.id.command);
        fragment = findViewById(R.id.nav_host_fragment);

        asrController = new AsrController(record, stop, valid, resultText, commandText);

        record.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1234);
            }
            else{
                Log.d("TAG", "Authorized 2");
                asrController.startRecording();
            }

        });
        stop.setOnClickListener(v -> {
            asrController.stopRecording();
        });
        valid.setOnClickListener(v -> {
            asrController.valid();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "Authorized");
                    asrController.startRecording();

                } else {
                    Log.d("TAG", "permission denied by user");
                }
                return;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }


}