package com.leonelacs.ongaku;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    ImageView detailAlbumCover;
    TextView detailTitle;
    TextView detailArtist;
    TextView detailAlbumName;
    TextView detailCurrent;
    TextView detailFull;
    SeekBar detailSeekBar;

    Button detailButtonShaffuru;
    Button detailButtonPrevious;
    Button detailButtonPlay;
    Button detailButtonNext;
    Button detailButtonRipiito;

    OngakuApp ongakuApp;

    Intent startIntent;
    HousouService.HousouBinder housouBinder;
    ServiceConnection serviceConnection;

    int cIndex = 0;
    ShaffuruMode shuffle = ShaffuruMode.ORDER;
    RipiitoMode repeat = RipiitoMode.ALL_LOOP;

    Bitmap back;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ongakuApp = (OngakuApp) getApplication();

        detailAlbumCover = findViewById(R.id.DetailAlbumCover);
        detailTitle = findViewById(R.id.DetailTitle);
        detailArtist = findViewById(R.id.DetailArtist);
        detailAlbumName = findViewById(R.id.DetailAlbumName);
        detailCurrent = findViewById(R.id.DetailCurrent);
        detailFull = findViewById(R.id.DetailFull);
        detailSeekBar = findViewById(R.id.DetailSeekBar);

        detailButtonShaffuru = findViewById(R.id.DetailButtonShaffuru);
        detailButtonPrevious = findViewById(R.id.DetailButtonPrevious);
        detailButtonPlay = findViewById(R.id.DetailButtonPlay);
        detailButtonNext = findViewById(R.id.DetailButtonNext);
        detailButtonRipiito = findViewById(R.id.DetailButtonRipiito);

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

        int cPosition = getIntent().getIntExtra("cPosition", 0);
        int cProgress = getIntent().getIntExtra("cProgress", 0);
        int cDuration = getIntent().getIntExtra("cDuration", 0);
        shuffle = (ShaffuruMode) getIntent().getSerializableExtra("shuffle");
        repeat = (RipiitoMode) getIntent().getSerializableExtra("repeat");

        cIndex = cPosition;

        if (ongakuApp.globalOngakus.get(cPosition).getCover() != null) {
            detailAlbumCover.setImageBitmap(ongakuApp.globalOngakus.get(cPosition).getCover());
            back = Bitmap.createBitmap(ongakuApp.globalOngakus.get(cPosition).getCover());
            back = blur3Times(back);
            findViewById(R.id.DetailBackground).setBackground(new BitmapDrawable(getResources(), back));
        }
        else {
            detailAlbumCover.setImageResource(R.drawable.default_cover_alpha);
            back = BitmapFactory.decodeResource(getResources(),R.drawable.default_cover_alpha_400);
            findViewById(R.id.DetailBackground).setBackground(getDrawable(R.drawable.default_cover));
        }

        detailTitle.setText(ongakuApp.globalOngakus.get(cPosition).getTitle());
        detailArtist.setText(ongakuApp.globalOngakus.get(cPosition).getArtist());
        detailAlbumName.setText(ongakuApp.globalOngakus.get(cPosition).getAlbum());
        detailCurrent.setText(toTimeFormat(cProgress));
        detailFull.setText(toTimeFormat(cDuration));
        detailSeekBar.setProgress(cProgress);
        detailSeekBar.setMax(cDuration);

        updateProgress();

        detailButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (housouBinder.getIsPlaying()) {
                    housouBinder.pause();
                }
                else {
                    refreshDetailThings(housouBinder.play());
                }
            }
        });

        detailButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshDetailThings(housouBinder.previous());
            }
        });

        detailButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshDetailThings(housouBinder.next());
            }
        });

        detailButtonShaffuru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle = housouBinder.changeShuffle();
                if (shuffle == ShaffuruMode.ORDER) {
                    detailButtonShaffuru.setText("SEQ");
                }
                else {
                    detailButtonShaffuru.setText("RAN");
                }
            }
        });

        detailButtonRipiito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeat = housouBinder.changeRepeat();
                if (repeat == RipiitoMode.NO_LOOP) {
                    detailButtonRipiito.setText("OFF");
                }
                else if (repeat == RipiitoMode.SINGLE_LOOP) {
                    detailButtonRipiito.setText("ONE");
                }
                else {
                    detailButtonRipiito.setText("ALL");
                }
            }
        });

        detailSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    void refreshDetailThings(int index) {
        detailTitle.setText(ongakuApp.globalOngakus.get(index).getTitle());
        detailArtist.setText(ongakuApp.globalOngakus.get(index).getArtist());
        detailAlbumName.setText(ongakuApp.globalOngakus.get(index).getAlbum());
        if (ongakuApp.globalOngakus.get(index).getCover() == null) {
            detailAlbumCover.setImageResource(R.drawable.default_cover_alpha);
            findViewById(R.id.DetailBackground).setBackground(getDrawable(R.drawable.default_cover));
        }
        else {
            detailAlbumCover.setImageBitmap(ongakuApp.globalOngakus.get(index).getCover());
            back = ongakuApp.globalOngakus.get(index).getCover();
            back = blur3Times(back);
            findViewById(R.id.DetailBackground).setBackground(new BitmapDrawable(getResources(), back));
        }
        detailSeekBar.setMax(housouBinder.getHousouDuration());
        detailFull.setText(toTimeFormat(housouBinder.getHousouDuration()));

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(cIndex != housouBinder.getHIndex()){
                cIndex = housouBinder.getHIndex();
                refreshDetailThings(cIndex);
            }
            int progress = housouBinder.getHousouProgress();
            detailSeekBar.setProgress(progress);
            detailCurrent.setText(toTimeFormat(housouBinder.getHousouProgress()));
            updateProgress();
            return true;
        }
    });


    private void updateProgress() {
        Message msg = Message.obtain();
        handler.sendMessageDelayed(msg, 1000);
    }

    String toTimeFormat(int time_mm) {
        time_mm /= 1000;
        int full_min = time_mm / 60;
        int full_sec = time_mm % 60;
        String str_full_min = "", str_full_sec = "";
        if (full_min < 10) {
            str_full_min = "0";
        }
        if (full_sec < 10) {
            str_full_sec = "0";
        }
        str_full_min = str_full_min + String.valueOf(full_min);
        str_full_sec = str_full_sec + String.valueOf(full_sec);
        return str_full_min + ":" + str_full_sec;
    }
    String toTimeFormat(long time_mm) {
        time_mm /= 1000;
        long full_min = time_mm / 60;
        long full_sec = time_mm % 60;
        String str_full_min = "", str_full_sec = "";
        if (full_min < 10) {
            str_full_min = "0";
        }
        if (full_sec < 10) {
            str_full_sec = "0";
        }
        str_full_min = str_full_min + String.valueOf(full_min);
        str_full_sec = str_full_sec + String.valueOf(full_sec);
        return str_full_min + ":" + str_full_sec;
    }

    Bitmap blurBitmap(Bitmap bmp) {
        Bitmap bitmap = Bitmap.createBitmap(bmp);
        Bitmap blurred = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(this);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation in = Allocation.createFromBitmap(rs, bitmap);
        Allocation out = Allocation.createFromBitmap(rs, blurred);
        blur.setRadius(25f);
        blur.setInput(in);
        blur.forEach(out);
        out.copyTo(blurred);
        rs.destroy();
        return blurred;
    }

    Bitmap blur3Times(Bitmap bmp) {
        Bitmap blurred = blurBitmap(bmp);
        blurred = blurBitmap(blurred);
        blurred = blurBitmap(blurred);
        return blurred;
    }
}
