package org.assessment.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class AwsS3ConfigTest {

    @Test
    void s3ClientBeanCreated() {
        AwsS3Config config = new AwsS3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "fakeAccessKey");
        ReflectionTestUtils.setField(config, "secretKey", "fakeSecretKey");

        S3Client s3Client = config.s3Client();
        assertThat(s3Client).isNotNull();
    }
}
