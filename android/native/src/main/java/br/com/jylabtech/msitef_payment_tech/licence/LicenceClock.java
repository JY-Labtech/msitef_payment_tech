package br.com.jylabtech.msitef_payment_tech.licence;

import java.util.Date;

/** Relógio injetável (java.util.function não está disponível no minSdk 23). */
public interface LicenceClock {
    Date now();

    LicenceClock SYSTEM = new LicenceClock() {
        @Override
        public Date now() {
            return new Date();
        }
    };
}
