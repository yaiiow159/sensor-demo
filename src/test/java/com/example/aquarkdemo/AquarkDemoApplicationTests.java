package com.example.aquarkdemo;

import com.example.aquarkdemo.config.ApiCallProps;
import com.example.aquarkdemo.util.RestTemplateUtil;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AquarkDemoApplicationTests {

    @Autowired
    private ApiCallProps apiCallProps;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Test
    void testApiCallIfSuccess() {
        List<String> apiCallList = apiCallProps.getApiCallList();
        for (String apiCall : apiCallList) {
            try {
                Object o = restTemplateUtil.get(apiCall, null, Object.class);
                System.out.println("呼叫api " + apiCall + " 成功");
                System.out.println("呼叫回傳結果" + o.toString());
            } catch (ConnectionRequestTimeoutException e) {
                System.out.println("呼叫api " + apiCall + " 失敗");
            }
        }
    }

}
