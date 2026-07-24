package org.assessment;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class AssessmentServiceApplicationTest {

    @Test
    void mainMethodRuns() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
            springApplicationMock.when(() -> SpringApplication.run(AssessmentServiceApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            AssessmentServiceApplication.main(new String[]{});

            springApplicationMock.verify(() -> SpringApplication.run(AssessmentServiceApplication.class, new String[]{}));
        }
    }
}
