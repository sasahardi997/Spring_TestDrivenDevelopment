package com.hardi.Hoaxify.service.file;

import com.hardi.Hoaxify.config.AppConfiguration;
import com.hardi.Hoaxify.domain.FileAttachment;
import com.hardi.Hoaxify.repository.FileAttachmentRepository;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@EnableScheduling
public class FileService {

    AppConfiguration appConfiguration;

    Tika tika;

    FileAttachmentRepository fileAttachmentRepository;

    public FileService(AppConfiguration appConfiguration, FileAttachmentRepository fileAttachmentRepository) {
        super();
        this.appConfiguration = appConfiguration;
        this.tika = new Tika();
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public String saveProfileImage(String base64Image) throws IOException {
        String imageName = generateRandomName();
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
        FileUtils.writeByteArrayToFile(target, decodedBytes);
        return imageName;
    }

    public String detectType(byte[] fileArr) {
        return tika.detect(fileArr);
    }

    public void deleteProfileImage(String image) throws IOException {
        Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
    }

    public FileAttachment saveAttachment(MultipartFile file) {
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(new Date());
        String randomName = generateRandomName();
        fileAttachment.setName(randomName);

        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + randomName);
        try {
            byte[] filesAsByte = file.getBytes();
            FileUtils.writeByteArrayToFile(target, filesAsByte);
            fileAttachment.setFileType(detectType(filesAsByte));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileAttachmentRepository.save(fileAttachment);
    }

    private String generateRandomName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupStorage() {
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> oldFiles =  fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        for(FileAttachment file: oldFiles) {
            deleteAttachmentImage(file.getName());
            fileAttachmentRepository.deleteById(file.getId());
        }
    }

    public void deleteAttachmentImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentsPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
