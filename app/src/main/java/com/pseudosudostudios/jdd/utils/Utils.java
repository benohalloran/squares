/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */

package com.pseudosudostudios.jdd.utils;

import java.util.Locale;

/**
 * Created by Ben on 1/25/2015.
 */
public class Utils {
    public static String returnFirstCap(String raw) {
        try {
            return raw.substring(0, 1).toUpperCase(Locale.getDefault())
                    + raw.substring(1).toLowerCase(Locale.getDefault());
        } catch (Exception e) {
            return raw;
        }
    }
}
