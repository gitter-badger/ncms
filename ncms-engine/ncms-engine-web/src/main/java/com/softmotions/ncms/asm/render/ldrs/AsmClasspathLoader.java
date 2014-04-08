package com.softmotions.ncms.asm.render.ldrs;

import httl.spi.loaders.ClasspathLoader;
import com.softmotions.ncms.asm.render.AsmLoader;
import com.softmotions.ncms.asm.render.AsmResource;

import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmClasspathLoader implements AsmLoader {

    private final ClasspathLoader loader;

    public AsmClasspathLoader() {
        this.loader = new ClasspathLoader();
    }

    public List<String> list(String suffix) throws IOException {
        return loader.list(suffix);
    }

    public boolean exists(String name, Locale locale) {
        return loader.exists(name, locale);
    }

    public AsmResource load(String name, Locale locale, String encoding) throws IOException {
        return new HttlAsmResourceAdapter(loader.load(name, locale, encoding));
    }
}

