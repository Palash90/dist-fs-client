package com.dist_fs.client.service;

import com.dist_fs.client.beans.model.Chunk;
import com.dist_fs.client.beans.model.UploadRequest;
import com.dist_fs.client.beans.model.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class UploadClientService {

    @Value("${server}")
    private String server;

    @Value("${uploadUrl}")
    private String uploadUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void uploadFile(String filePath) {
        UploadResponse[] uploadResponses = getUploadResponse(filePath);

        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            for (UploadResponse uploadResponse : uploadResponses) {
                // Get chunk details and URLs
                Chunk chunk = uploadResponse.getChunk();
                List<String> urls = uploadResponse.getUrls();

                // Prepare buffer for reading the chunk
                byte[] buffer = new byte[(int) chunk.getChunkSize()];
                int bytesRead;

                bytesRead = fileInputStream.read(buffer);

                if (bytesRead == -1) {
                    break; // End of file
                }

                // Create the actual chunk data to send
                byte[] chunkData = bytesRead == chunk.getChunkSize() ? buffer : java.util.Arrays.copyOf(buffer, bytesRead);

                // Read and upload the file in chunks
                for (String uploadUrl : urls) {

                    // Prepare headers
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.set("Chunk-Id", chunk.getChunkId().toString()); // Include the chunk ID

                    // Create HttpEntity for the request
                    HttpEntity<ByteArrayResource> requestEntity = new HttpEntity<>(new ByteArrayResource(chunkData), headers);

                    // Upload the chunk to the corresponding URL
                    ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Failed to upload chunk to " + uploadUrl);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private UploadResponse[] getUploadResponse(String filePath) {
        File file = new File(filePath);

        UploadRequest request = new UploadRequest();
        request.setFilePath(file.getName());
        request.setFileSize(file.length());

        return restTemplate
                .postForEntity(server + uploadUrl, request, UploadResponse[].class)
                .getBody();
    }
}
