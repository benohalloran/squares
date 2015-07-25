/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.pseudosudostudios.jdd.R;

/**
 * Displays the about screen. When the studio logo is clicked, it goes to the
 * store listing with all the apps
 */
public class AboutActivity extends Activity {
    public static final String marketURL = "market://search?q=pub:Pseudo+Sudo+Studios";
    public static final String facebookURL = "https://www.facebook.com/PseudoSudoStudios";

    public static final Intent marketIntent = new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(marketURL));
    public static final Intent facebookIntent = new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(facebookURL));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
       /* ImageView img = (ImageView) findViewById(R.id.about_studio_icon);
        img.setClickable(true);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(marketIntent);
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Other apps by Pseudo Sudo Studios").setIntent(marketIntent);
        menu.add("Like us on Facebook").setIntent(marketIntent);
        return false; //Will be changing names around
    }
}
