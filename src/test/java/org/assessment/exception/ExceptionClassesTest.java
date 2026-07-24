package org.assessment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Custom Exception Classes Tests")
class ExceptionClassesTest {

    // -------------------------------------------------------------------------
    // ResourceNotFoundException
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("should be a RuntimeException")
        void isRuntimeException() {
            assertThat(new ResourceNotFoundException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should store and return the message")
        void storesMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException("Assignment not found with id: 42");
            assertThat(ex.getMessage()).isEqualTo("Assignment not found with id: 42");
        }

        @Test
        @DisplayName("should be throwable and catchable")
        void canBeThrown() {
            assertThatThrownBy(() -> { throw new ResourceNotFoundException("not found"); })
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("not found");
        }

        @Test
        @DisplayName("should preserve message with special characters")
        void preservesSpecialChars() {
            String msg = "Resource 'Assignment#001' not found";
            assertThat(new ResourceNotFoundException(msg).getMessage()).isEqualTo(msg);
        }
    }

    // -------------------------------------------------------------------------
    // ValidationException
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTest {

        @Test
        @DisplayName("should be a RuntimeException")
        void isRuntimeException() {
            assertThat(new ValidationException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should store and return the message")
        void storesMessage() {
            ValidationException ex = new ValidationException("Course ID must not be empty");
            assertThat(ex.getMessage()).isEqualTo("Course ID must not be empty");
        }

        @Test
        @DisplayName("should be throwable and catchable")
        void canBeThrown() {
            assertThatThrownBy(() -> { throw new ValidationException("invalid input"); })
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("invalid input");
        }

        @Test
        @DisplayName("should not be an instance of ResourceNotFoundException")
        void isDistinctFromResourceNotFound() {
            assertThat(new ValidationException("msg"))
                    .isNotInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // FileUploadException
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("FileUploadException")
    class FileUploadExceptionTest {

        @Test
        @DisplayName("should be a RuntimeException")
        void isRuntimeException() {
            assertThat(new FileUploadException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should store and return the message")
        void storesMessage() {
            FileUploadException ex = new FileUploadException("Failed to upload file to S3: IO error");
            assertThat(ex.getMessage()).isEqualTo("Failed to upload file to S3: IO error");
        }

        @Test
        @DisplayName("should be throwable and catchable")
        void canBeThrown() {
            assertThatThrownBy(() -> { throw new FileUploadException("upload failed"); })
                    .isInstanceOf(FileUploadException.class)
                    .hasMessage("upload failed");
        }

        @Test
        @DisplayName("should be distinct from ValidationException")
        void isDistinctType() {
            assertThat(new FileUploadException("msg"))
                    .isNotInstanceOf(ValidationException.class);
        }
    }
}
