package com.vignesh;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchAndStoreStockPrice {

    private static final String ALPHA_VANTAGE_API_URL = "https://www.alphavantage.co/query";
    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your actual API key
    private static final String DYNAMODB_TABLE_NAME = "StockPrices";

    private static final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);

    public Map<String, Object> handleRequest(Map<String, Object> event) {
        // Log the incoming event to see its structure
        System.out.println("Received event: " + event);

        // Safely get the query string parameters
        Object queryParamsObject = event.get("queryStringParameters");
        Map<String, String> queryParams = null;

        if (queryParamsObject instanceof Map) {
            queryParams = (Map<String, String>) queryParamsObject;
        }

        // Log the query parameters to see if "symbol" is there
        System.out.println("Query Parameters: " + queryParams);

        // Retrieve the stock symbol from query parameters
        String stockSymbol = queryParams != null ? queryParams.get("symbol") : null;

        if (stockSymbol == null || stockSymbol.isEmpty()) {
            return createResponse(400, "Error: Stock symbol is required");
        }

        // Fetch the stock price from Alpha Vantage API
        double stockPrice = fetchStockPrice(stockSymbol);

        if (stockPrice == -1) {
            return createResponse(500, "Error: Could not fetch stock price for symbol: " + stockSymbol);
        }

        // Store stock price in DynamoDB
        String timestamp = Instant.now().toString();
        boolean isStored = storeStockPriceInDynamoDB(stockSymbol, stockPrice, timestamp);

        // Return the response
        if (isStored) {
            String message = String.format("Stock price for %s at %s: $%.2f", stockSymbol, timestamp, stockPrice);
            return createResponse(200, message);
        } else {
            return createResponse(500, "Error: Failed to store stock price in DynamoDB");
        }
    }

    private double fetchStockPrice(String stockSymbol) {
        try {
            String url = String.format("%s?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s",
                    ALPHA_VANTAGE_API_URL, stockSymbol, API_KEY);

            // Make HTTP request to Alpha Vantage API
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                return -1;  // Indicate failure
            }

            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode timeSeriesNode = rootNode.path("Time Series (1min)");

            if (timeSeriesNode.isMissingNode()) {
                return -1;
            }

            JsonNode latestData = timeSeriesNode.fields().next().getValue();  // Get most recent entry
            JsonNode closePriceNode = latestData.path("4. close");

            if (closePriceNode.isMissingNode()) {
                return -1;
            }

            return closePriceNode.asDouble();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean storeStockPriceInDynamoDB(String stockSymbol, double stockPrice, String timestamp) {
        try {
            Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

            ValueMap valueMap = new ValueMap();
            valueMap.withString("StockSymbol", stockSymbol)
                    .withString("Timestamp", timestamp)
                    .withNumber("Price", stockPrice);

            PutItemSpec putItemSpec = new PutItemSpec().withItem(Item.fromMap(valueMap));
            table.putItem(putItemSpec);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper function to create a response
    private Map<String, Object> createResponse(int statusCode, String body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("body", body);
        return response;
    }
}
