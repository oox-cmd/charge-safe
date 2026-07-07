package com.chargesafe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({"com.chargesafe.modules.*.mapper", "com.chargesafe.common"})
public class ChargeSafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargeSafeApplication.class, args);
    }
}
