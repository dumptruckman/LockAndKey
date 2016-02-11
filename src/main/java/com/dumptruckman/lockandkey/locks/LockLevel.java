package com.dumptruckman.lockandkey.locks;

import pluginbase.config.annotation.Comment;
import pluginbase.config.annotation.NoTypeKey;

@NoTypeKey
public final class LockLevel {


    private int level = 1;
    @Comment({"This is a string of all the valid characters than can be used for a lock code.",
             "A random selection of these will be created for every new key-lock combination."})
    private String lockCodeCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    @Comment({"This is the number of characters to use in a lock code.",
            "Having a small length and few characters raises the chance to produce keys that work on locks they were not created for."})
    private int lockCodeLength = 5;


}
