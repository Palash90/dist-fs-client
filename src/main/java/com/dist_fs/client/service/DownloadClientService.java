package com.dist_fs.client.service;

import com.dist_fs.client.beans.model.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;

@Service
public class DownloadClientService {

    @Value("${server}")
    private String server;

    @Value("${downloadUrl}")
    private String downloadUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void downloadFile(String filePath) {
        UploadResponse[] response = restTemplate
                .postForEntity(server + downloadUrl, filePath, UploadResponse[].class)
                .getBody();

        for (UploadResponse r : response) {
            System.out.println(r);
        }
    }
}
