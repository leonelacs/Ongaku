package com.leonelacs.ongaku;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView ongakuRecycler;
    OngakuAdapter ongakuAdapter;

    int cIndex = 0;

    Intent startIntent;
    HousouService.HousouBinder housouBinder;
    ServiceConnection serviceConnection;

    ImageView mainAlbumCover;
    TextView mainTitle;
    TextView mainArtist;
    Button mainButtonPrevious;
    Button mainButtonPlay;
    Button mainButtonNext;
    SeekBar mainSeekBar;

    OngakuApp ongakuApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainAlbumCover = findViewById(R.id.MainAlbumCover);
        mainTitle = findViewById(R.id.MainTitle);
        mainArtist = findViewById(R.id.MainArtist);
        mainButtonPrevious = findViewById(R.id.MainButtonPrevious);
        mainButtonPlay = findViewById(R.id.MainButtonPlay);
        mainButtonNext = findViewById(R.id.MainButtonNext);
        mainSeekBar = findViewById(R.id.MainSeekBar);

        ongakuApp = (OngakuApp) getApplication();

        ongakuRecycler = (RecyclerView)findViewById(R.id.OngakuRecycler);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        ongakuRecycler.setLayoutManager(staggeredGridLayoutManager);
        ongakuAdapter = new OngakuAdapter(ongakuApp.globalOngakus);
        ongakuRecycler.setAdapter(ongakuAdapter);
        ongakuAdapter.notifyDataSetChanged();

        mainAlbumCover.setImageResource(R.drawable.default_cover_alpha_400);
        mainTitle.setText("");
        mainArtist.setText("");

        ongakuAdapter.setOnItemClickListener(new OngakuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Ongaku ongaku) {
                cIndex = position;
                refreshMainThings(position);
                housouBinder.select(position);
            }
        });

        //service
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                housouBinder = (HousouService.HousouBinder) iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        startIntent = new Intent(this, HousouService.class);
        bindService(startIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(startIntent);

        updateProgress();

        mainAlbumCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("cPosition", cIndex);
                intent.putExtra("cProgress", mainSeekBar.getProgress());
                intent.putExtra("cDuration", housouBinder.getHousouDuration());
                ShaffuruMode shuffle = housouBinder.getShuffle();
                RipiitoMode repeat = housouBinder.getRepeat();
                intent.putExtra("shuffle", shuffle);
                intent.putExtra("repeat", repeat);
                view.getContext().startActivity(intent);
            }
        });

        mainButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (housouBinder.getIsPlaying()) {
                    housouBinder.pause();
                }
                else {
                    refreshMainThings(housouBinder.play());
                }
            }
        });

        mainButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshMainThings(housouBinder.previous());
            }
        });

        mainButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshMainThings(housouBinder.next());
            }
        });

        mainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    housouBinder.setHousouProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(cIndex != housouBinder.getHIndex()){
                cIndex = housouBinder.getHIndex();
                refreshMainThings(cIndex);
            }
            int progress = housouBinder.getHousouProgress();
            mainSeekBar.setProgress(progress);
            updateProgress();
            return true;
        }
    });


    private void updateProgress() {
        Message msg = Message.obtain();
        handler.sendMessageDelayed(msg, 1000);
    }

    void refreshMainThings(int index) {
        mainTitle.setText(ongakuApp.globalOngakus.get(index).getTitle());
        mainArtist.setText(ongakuApp.globalOngakus.get(index).getArtist());
        if (ongakuApp.globalOngakus.get(index).getCover() == null) {
            mainAlbumCover.setImageResource(R.drawable.default_cover_alpha_400);
        }
        else {
            mainAlbumCover.setImageBitmap(ongakuApp.globalOngakus.get(index).getCover());
        }
        mainSeekBar.setMax(housouBinder.getHousouDuration());
    }
}
