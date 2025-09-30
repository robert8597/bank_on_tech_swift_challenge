package com.db.fms_sds.botchallenge.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Slf4j
public class FifthTaskTest {

    @Autowired
    FifthTask fifthTask;

    @Test
    public void ackDistributionTest() {
        String uri = fifthTask.ackDistribution();

        assertThat(uri)
                .as("Wrong URI")
                .contains("44984189499");
    }
}
