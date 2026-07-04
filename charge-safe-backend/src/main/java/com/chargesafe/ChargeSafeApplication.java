package com.chargesafe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.chargesafe.modules.*.mapper", "com.chargesafe.common"})
public class ChargeSafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargeSafeApplication.class, args);
    }
}
