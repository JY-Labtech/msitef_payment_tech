package br.com.jylabtech.msitef_payment_tech.licence;

/** Abstração da consulta de licença no servidor (permite fake em testes JVM). */
public interface LicenceNetwork {
    LicenceResult checkLicence(String url, String licenceKey) throws Exception;
}
