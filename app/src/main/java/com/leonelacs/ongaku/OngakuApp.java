package com.leonelacs.ongaku;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class OngakuApp extends Application {

    public List<Ongaku> globalOngakus = new ArrayList<>();

    public void setGlobalOngakus(List<Ongaku> globalOngakus) {
        this.globalOngakus = globalOngakus;
    }

    public Ongaku getOngaku(int index) {
        return globalOngakus.get(index);
    }

    public int getGlobalOngakusSize() {
        return globalOngakus.size();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalOngakus = getOngakuFromPhone();
    }

    public List<Ongaku> getOngakuFromPhone() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Ongaku> myuujikkuFiles = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Ongaku m = new Ongaku();
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                int ongakuDesu = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                Bitmap cover = getKabaa(albumId);
                if (ongakuDesu != 0 && duration / (500 * 60) >= 1) {
                    m.setId(id);
                    m.setTitle(title);
                    m.setArtist(artist);
                    m.setAlbum(album);
                    m.setAlbumId(albumId);
                    m.setDuration(duration);
                    m.setSize(size);
                    m.setPath(path);
                    m.setCover(cover);
                    myuujikkuFiles.add(m);
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return myuujikkuFiles;
    }

    public Bitmap getKabaa(long albumId) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Long.toString(albumId)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            //bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover_alpha_400);
            bm = null;
        }
        return bm;
    }


}
