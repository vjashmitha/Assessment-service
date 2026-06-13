package org.assessment.config;

import org.assessment.entity.Assignment;
import org.assessment.entity.AssignmentReview;
import org.assessment.entity.Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<Assignment> assignmentTable(DynamoDbEnhancedClient enhancedClient,
                                                     @Value("${dynamodb.table.assignments}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(Assignment.class));
    }

    @Bean
    public DynamoDbTable<Submission> submissionTable(DynamoDbEnhancedClient enhancedClient,
                                                     @Value("${dynamodb.table.submissions}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(Submission.class));
    }

    @Bean
    public DynamoDbTable<AssignmentReview> assignmentReviewTable(DynamoDbEnhancedClient enhancedClient,
                                                                  @Value("${dynamodb.table.reviews}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(AssignmentReview.class));
    }
}
