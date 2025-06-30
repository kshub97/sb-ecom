package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImages(String path, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); //FileName of request file
        String randomId = UUID.randomUUID().toString(); //Generating random id so to attach it with fileName to make unique
        String fileName = randomId + "_" +originalFilename; //concatenating fileName / Id to make each file unique before saving.
        String fullPath = path + File.separator + fileName; //Adding file to given path

        File folder = new File(path); //check if requested path exist if not create it
        if (!folder.exists()){
            folder.mkdirs(); // ✅ creates all parent directories if missing
        }
        Files.copy(file.getInputStream(), Path.of(fullPath), StandardCopyOption.REPLACE_EXISTING); //(file need to save , target location/path)
        return fileName;
    }
}
