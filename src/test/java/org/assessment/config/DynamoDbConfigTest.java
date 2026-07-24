package org.assessment.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DynamoDbConfigTest {

    @Test
    void dynamoDbClientAndEnhancedClientBeansCreated() {
        DynamoDbConfig config = new DynamoDbConfig();
        ReflectionTestUtils.setField(config, "region", "us-east-1");

        DynamoDbClient client = config.dynamoDbClient();
        assertThat(client).isNotNull();

        DynamoDbClient mockClient = mock(DynamoDbClient.class);
        DynamoDbEnhancedClient enhancedClient = config.dynamoDbEnhancedClient(mockClient);
        assertThat(enhancedClient).isNotNull();
    }
}
