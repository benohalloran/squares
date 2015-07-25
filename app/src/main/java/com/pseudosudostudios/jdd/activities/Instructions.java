/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.pseudosudostudios.jdd.R;

public class Instructions extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.about).setIntent(
                new Intent(this, AboutActivity.class));
        return true;
    }

}
