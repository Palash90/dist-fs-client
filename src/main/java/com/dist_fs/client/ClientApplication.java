package com.dist_fs.client;

import com.dist_fs.client.service.DownloadClientService;
import com.dist_fs.client.service.UploadClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    @Autowired
    private UploadClientService uploadClientService;

    @Autowired
    private DownloadClientService downloadClientService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.NONE) // Disable web server
                .run(args)
                .close();
    }

    @Override
    public void run(String... args) throws Exception {
        //uploadClientService.uploadFile("img.png.png");
        downloadClientService.downloadFile("img.png.png");
    }
}
