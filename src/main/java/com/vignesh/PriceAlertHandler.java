package com.vignesh;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class PriceAlertHandler implements RequestHandler<Map<String, String>, String> {

    private static final AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient(); // SNS client
    private static final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private static final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ALPHA_VANTAGE_API_URL = "https://www.alphavantage.co/query";
    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your Alpha Vantage API key
    private static final String DYNAMODB_TABLE_NAME = "StockPrices";
    private static final String SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:867344464221:StockPriceAlerts"; // Replace with your SNS ARN

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        String stockSymbol = event.get("symbol");
        double priceThreshold = Double.parseDouble(event.get("priceThreshold"));

        if (stockSymbol == null || stockSymbol.isEmpty()) {
            return "Error: Stock symbol is required.";
        }

        try {
            // Fetch the current stock price
            double stockPrice = fetchStockPrice(stockSymbol);
            if (stockPrice == -1.0) {
                return "Error: Could not fetch stock price for symbol: " + stockSymbol;
            }

            // Store the stock price in DynamoDB
            String timestamp = Instant.now().toString();
            storeStockPriceInDynamoDB(stockSymbol, stockPrice, timestamp);

            // Check if the stock price exceeds the price threshold
            if (stockPrice >= priceThreshold) {
                // Send an SNS alert
                sendSNSNotification(stockSymbol, stockPrice);
            }

            return String.format("Stock price for %s at %s: $%.2f", stockSymbol, timestamp, stockPrice);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    // Fetches the stock price from Alpha Vantage API
    private double fetchStockPrice(String stockSymbol) throws Exception {
        String url = String.format("%s?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s", ALPHA_VANTAGE_API_URL, stockSymbol, API_KEY);
        Request request = new Request.Builder().url(url).build();
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("Failed to fetch stock price.");
        }

        String responseBody = response.body().string();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode timeSeriesNode = rootNode.path("Time Series (1min)");

        if (timeSeriesNode.isMissingNode()) {
            return -1.0;
        }

        JsonNode latestData = timeSeriesNode.fields().next().getValue();
        JsonNode closePriceNode = latestData.path("4. close");

        return closePriceNode.isMissingNode() ? -1.0 : closePriceNode.asDouble();
    }

    // Stores the stock price in DynamoDB
    private void storeStockPriceInDynamoDB(String stockSymbol, double stockPrice, String timestamp) {
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
        Item item = new Item()
                .withPrimaryKey("StockSymbol", stockSymbol, "Timestamp", timestamp)
                .withNumber("Price", stockPrice);
        table.putItem(new PutItemSpec().withItem(item));
    }

    // Sends an SNS notification if the stock price exceeds the threshold
    private void sendSNSNotification(String stockSymbol, double stockPrice) {
        String message = String.format("ALERT: The stock price for %s has exceeded the threshold. Current price: $%.2f", stockSymbol, stockPrice);
        PublishRequest publishRequest = new PublishRequest()
                .withMessage(message)
                .withTopicArn(SNS_TOPIC_ARN);

        PublishResult result = snsClient.publish(publishRequest);
        System.out.println("Message sent to SNS. Message ID: " + result.getMessageId());
    }
}
