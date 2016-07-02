package com.softmotions.ncms.asm;

import java.util.Map;

import com.softmotions.commons.cont.KVOptions;

/**
 * Parsed assembly options.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmOptions extends KVOptions {

    public AsmOptions() {
    }

    public AsmOptions(Map map) {
        super(map);
    }

    public AsmOptions(String spec) {
        loadOptions(spec);
    }
}



