package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;

    public String upload(byte[] content, String originalFilename) throws ClientException {
        String endpoint = aliOssProperties.getEndpoint();
        String bucketName = aliOssProperties.getBucketName();
        String region = aliOssProperties.getRegion();

        // 1. 如果你坚持使用环境变量方式（推荐），请确保本机已配好 OSS_ACCESS_KEY_ID 环境变量
        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 2. 自动生成 objectName (日期分区 + UUID)
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + UUID.randomUUID() + extension;

        // 3. V4 签名配置
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
            // 4. 修改拼接逻辑，适配你的 yml 配置
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            log.info("文件上传成功，访问路径: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        } finally {
            ossClient.shutdown();
        }
    }
}