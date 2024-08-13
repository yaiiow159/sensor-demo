package com.example.aquarkdemo.util;

import com.example.aquarkdemo.config.RestTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * @author Timmy
 *
 * http連線 工具類
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class RestTemplateUtil {

    private final RestTemplate restTemplate;

    // GET 方法呼叫
    public <T> T get(String url, Map<String, String> queryParams, Class<T> responseType) throws ConnectionRequestTimeoutException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        if (queryParams != null) {
            queryParams.forEach(uriBuilder::queryParam);
        }

        String finalUrl = uriBuilder.toUriString();
        return restTemplate.getForObject(finalUrl, responseType);
    }

    // POST 方法呼叫
    public <T> T post(String url, Object requestBody, Class<T> responseType) throws ConnectionRequestTimeoutException{
        HttpEntity<Object> request = new HttpEntity<>(requestBody);
        return restTemplate.postForObject(url, request, responseType);
    }

    // PUT 方法呼叫
    public <T> T put(String url, Object requestBody, Class<T> responseType) throws ConnectionRequestTimeoutException {
        HttpEntity<Object> request = new HttpEntity<>(requestBody);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
        return response.getBody();
    }

    // DELETE 方法呼叫
    public void delete(String url, Map<String, String> queryParams) throws ConnectionRequestTimeoutException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        if (queryParams != null) {
            queryParams.forEach(uriBuilder::queryParam);
        }

        String finalUrl = uriBuilder.toUriString();
        restTemplate.delete(finalUrl);
    }
}
