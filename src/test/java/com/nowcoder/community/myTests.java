package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class myTests {

    @Test
    public void test() {
        HashMap<Object, Object> map = new HashMap<>();
        if (map.get(1) == null) {
            System.out.println("ok");
        }
    }


}
