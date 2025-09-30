package com.db.fms_sds.botchallenge.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ThirdTaskTest {

    @Autowired
    ThirdTask thirdTask;

    @Test
    public void pullListOfDistributionsTest()
    {
        thirdTask.pullListOfDistributions();
    }
}
