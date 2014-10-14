package com.softmotions.ncms.qa;

import com.softmotions.ncms.NcmsEnvironment;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class QAModule extends AbstractModule {

    private final NcmsEnvironment env;

    public QAModule(NcmsEnvironment env) {
        this.env = env;
    }

    protected void configure() {
        Multibinder<PageQAPlugin> qaBinder =
                Multibinder.newSetBinder(binder(), PageQAPlugin.class);
        qaBinder.addBinding().to(PageNotFoundQAPlugin.class).in(Singleton.class);

        bind(PageQARS.class).in(Singleton.class);
    }
}
