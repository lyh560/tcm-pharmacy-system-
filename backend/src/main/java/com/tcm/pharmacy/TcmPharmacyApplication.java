package com.tcm.pharmacy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tcm.pharmacy.mapper")
public class TcmPharmacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmPharmacyApplication.class, args);
    }
}
