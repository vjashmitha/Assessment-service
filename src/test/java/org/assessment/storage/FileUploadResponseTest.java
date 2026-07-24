package org.assessment.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileUploadResponse Tests")
class FileUploadResponseTest {

    @Test
    @DisplayName("should be instantiable with no-args constructor")
    void canInstantiate() {
        FileUploadResponse response = new FileUploadResponse();
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("two instances should not be same object")
    void twoInstancesAreDistinct() {
        FileUploadResponse r1 = new FileUploadResponse();
        FileUploadResponse r2 = new FileUploadResponse();
        assertThat(r1).isNotSameAs(r2);
    }
}
