package com.dist_fs.client.service;

import com.dist_fs.client.beans.model.Chunk;
import com.dist_fs.client.beans.model.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DownloadClientService {

    @Value("${server}")
    private String server;

    @Value("${downloadUrl}")
    private String downloadUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void downloadFile(String filePath) {
        UploadResponse[] responses = restTemplate
                .postForEntity(server + downloadUrl, filePath, UploadResponse[].class)
                .getBody();

        int fileSize = 0;
        for(UploadResponse r: responses) {
            fileSize += (int) r.getChunk().getChunkSize();
        }


        byte[] fileData = new byte[fileSize];
        int byteCounter = 0;

        for (UploadResponse r : responses) {
            // Get chunk details and URLs
            Chunk chunk = r.getChunk();
            List<String> urls = r.getUrls();


            // Read and upload the file in chunks
            for (String downloadUrl : urls) {

                ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl + "/" + chunk.getChunkId(), byte[].class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Failed to download " + filePath);
                }

                byte[] responseData = response.getBody();
                for (byte b : responseData) {
                    fileData[byteCounter] = b;
                    byteCounter++;
                }
                break;
            }
        }


        try {
            FileOutputStream fos = new FileOutputStream(filePath + "-reconstructed");
            fos.write(fileData);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
