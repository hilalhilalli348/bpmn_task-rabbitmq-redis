package com.example.bpmn_task.utils;


import com.example.bpmn_task.dto.currency.ValCurs;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.TimeZones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

// merkezi bankin cari tarixdeki valyuta mezenneleri
@Service
public final class CurrencyService {


    private final RestTemplate restTemplate;
    private final JedisPool jedisPool;

    @Autowired
    public CurrencyService(RestTemplate restTemplate, JedisPool jedisPool) {
        this.restTemplate = restTemplate;
        this.jedisPool = jedisPool;
    }

    // her gun saat 8-de bu metod calisdirilir avtomatik.
    @Scheduled(cron = "0 0 8 * * ?", zone = "GMT+4")
    private void getCurrencyEveryDayAndSaveInRedis() {
        String nowDate = "https://cbar.az/currencies/"+LocalDate
                .now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                .concat(".xml");

        ValCurs valCurs = restTemplate.getForObject(nowDate, ValCurs.class);
        jedisPool.getResource().set("currency".getBytes(), SerializationUtils.serialize(valCurs));
    }

    public BigDecimal getCurrency(String currencyCode) {


        try {
            SSLUtils.turnOffSslChecking();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        // redis-den oxuma
        ValCurs valCurs = SerializationUtils.deserialize(jedisPool.getResource().get("currency".getBytes()));

        System.err.println("calisdi");


        AtomicReference<BigDecimal> USDCurrency = new AtomicReference<>(new BigDecimal(0));

        valCurs.getValTypes().forEach(
                valType -> {
                    valType.getValTypeList()
                            .forEach(
                                    valute -> {
                                        if (valute.getCode().equals(currencyCode)) {
                                            USDCurrency.set(new BigDecimal(valute.getValue().toString()));
                                        }

                                    }
                            );
                }
        );


        return USDCurrency.get();
    }


}
