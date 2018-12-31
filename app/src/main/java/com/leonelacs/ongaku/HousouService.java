package com.leonelacs.ongaku;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class HousouService extends Service {

    MediaPlayer mediaPlayer = new MediaPlayer();
    HousouBinder housouBinder;
//    List<String> ongakuPathList;
    String hPath;
    int hPosition;
    int hIndex;
    RipiitoMode repeat = RipiitoMode.ALL_LOOP;
    ShaffuruMode shuffle = ShaffuruMode.ORDER;

    OngakuApp ongakuApp;


    public HousouService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return housouBinder;
    }

    class HousouBinder extends Binder {
//        public void receiveOngakuPathList(List<String> paths) {
//            ongakuPathList.clear();
//            ongakuPathList.addAll(paths);
//        }

        public int getHousouProgress() {
            return mediaPlayer.getCurrentPosition();
        }

        public int getHousouDuration() {
            return mediaPlayer.getDuration();
        }

        public void setHousouProgress(int time) {
            mediaPlayer.seekTo(time);
        }

        public RipiitoMode getRepeat() {
            return repeat;
        }

        public ShaffuruMode getShuffle() {
            return shuffle;
        }

        public RipiitoMode changeRepeat() {
            if (repeat == RipiitoMode.NO_LOOP) { repeat = RipiitoMode.SINGLE_LOOP; }
            else if (repeat == RipiitoMode.SINGLE_LOOP) { repeat = RipiitoMode.ALL_LOOP; }
            else { repeat = RipiitoMode.NO_LOOP; }
            return repeat;
        }

        public ShaffuruMode changeShuffle() {
            if (shuffle == ShaffuruMode.ORDER) { shuffle = ShaffuruMode.RANDOM; }
            else { shuffle = ShaffuruMode.ORDER; }
            return shuffle;
        }

        public int play() {
            if (hPath == null) {
                try {
                    mediaPlayer.reset();
                    hPath = ongakuApp.globalOngakus.get(0).getPath();
                    hIndex = 0;
                    mediaPlayer.setDataSource(hPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            else {
                mediaPlayer.start();
            }
            return hIndex;
        }
        public void pause() { mediaPlayer.pause(); }

        public int previous() {
            if (hIndex == 0) {
                try {
                    mediaPlayer.reset();
                    hIndex = ongakuApp.globalOngakus.size() - 1;
                    hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                    mediaPlayer.setDataSource(hPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            else {
                try {
                    mediaPlayer.reset();
                    hIndex = hIndex - 1;
                    hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                    mediaPlayer.setDataSource(hPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            return hIndex;
        }

        public int next() {
            if (hIndex == ongakuApp.globalOngakus.size() - 1) {
                try {
                    mediaPlayer.reset();
                    hIndex = 0;
                    hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                    mediaPlayer.setDataSource(hPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            else {
                try {
                    mediaPlayer.reset();
                    hIndex = hIndex + 1;
                    hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                    mediaPlayer.setDataSource(hPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            return hIndex;
        }

        public void select(int index) {
            try {
                mediaPlayer.reset();
                hIndex = index;
                hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                mediaPlayer.setDataSource(hPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        public boolean getIsPlaying() {
            return mediaPlayer.isPlaying();
        }

        public int getHIndex() {
            return hIndex;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ongakuApp = (OngakuApp) getApplication();
        housouBinder = new HousouBinder();
        try {
            mediaPlayer.reset();
            hIndex = 0;
            hPath = ongakuApp.globalOngakus.get(0).getPath();
            mediaPlayer.setDataSource(hPath);
            mediaPlayer.prepare();
        }
        catch (Exception e) { e.printStackTrace(); }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (repeat == RipiitoMode.NO_LOOP) {
                    try {
                        mediaPlayer.reset();
                        hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                        mediaPlayer.setDataSource(hPath);
                        mediaPlayer.prepare();
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
                else if (repeat == RipiitoMode.SINGLE_LOOP) {
                    try {
                        mediaPlayer.reset();
                        hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                        mediaPlayer.setDataSource(hPath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
                else {
                    if (shuffle == ShaffuruMode.ORDER) {
                        housouBinder.next();
                    }
                    else {
                        try {
                            mediaPlayer.reset();
                            hIndex = (int)(Math.random() * ongakuApp.globalOngakus.size());
                            hPath = ongakuApp.globalOngakus.get(hIndex).getPath();
                            mediaPlayer.setDataSource(hPath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }
                        catch (Exception e) { e.printStackTrace(); }

                    }
                }
            }
        });
    }
}
