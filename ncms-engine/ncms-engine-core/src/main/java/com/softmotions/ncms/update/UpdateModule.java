package com.softmotions.ncms.update;

import com.softmotions.weboot.lifecycle.Start;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class UpdateModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(UpdateModule.class);

    protected void configure() {
        bind(UpdateInitializer.class).asEagerSingleton();

        // default: empty collection of updates
        Multibinder<HotFix> hotfixes = Multibinder.newSetBinder(binder(), HotFix.class);
    }

    public static class UpdateInitializer extends MBDAOSupport {

        private final List<HotFix> hotFixes;

        @Inject
        public UpdateInitializer(SqlSession sess, Set<HotFix> hotFixes) {
            super(UpdateInitializer.class, sess);

            this.hotFixes = new ArrayList<>(hotFixes);
            this.hotFixes.sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        }

        @Start(order = Integer.MAX_VALUE)
        public void init() {
            try {
                for (HotFix hotFix : hotFixes) {
                    applyHotFix(hotFix);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Transactional
        private void applyHotFix(HotFix hotfix) throws Exception {
            String id = hotfix.getId();
            if (id == null) {
                log.info("Applying HotFix: {}", hotfix.getClass().getName());
                hotfix.apply();
            } else if (count("isApplied", id) == 0) {
                log.info("Applying HotFix: {}#{}", hotfix.getClass().getName(), id);
                hotfix.apply();
                update("setApplied", id);
            } else {
                log.info("Skipping HotFix: {}#{}", hotfix.getClass().getName(), id);
            }
        }
    }
}