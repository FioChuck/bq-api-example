package chasf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.google.cloud.bigquery.JobException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;

public class Main {
    public static void main(String[] args) throws Exception, JobException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/query", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException, JobException {

            Long out = null;
            try {
                out = query();
            } catch (JobException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String response = "Query Execution time : " + String.valueOf((double) out / 1000) + " seconds.";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static Long query() throws JobException, InterruptedException {
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(
                "SELECT * FROM `cf-data-analytics.spark_autoscaling.fraud_predictions` WHERE SEARCH(transaction_id,'2798cb9e\\-42e5\\-40ca\\-bdcd\\-4dcd4e01f8e2')")
                .setUseLegacySql(false)
                .setUseQueryCache(false)
                .build();

        TableResult results = bigquery.query(queryConfig);

        Job job = bigquery.getJob(results.getJobId().getJob());
        JobStatistics.QueryStatistics stats = job.getStatistics();

        System.out.printf("Job status: %s%n", job.getStatus().getState());
        System.out.printf("Job start time: %s%n", job.getStatistics().getStartTime());
        System.out.printf("Job end time: %s%n", job.getStatistics().getEndTime());

        Long jobDuration = job.getStatistics().getEndTime() - job.getStatistics().getStartTime();
        System.out.printf("Job duration: %s%n", jobDuration);
        System.out.printf("Total slot ms consumed: %s%n", stats.getTotalSlotMs());

        System.out.println("Printing all rows");

        results
                .iterateAll()
                .forEach(row -> row.forEach(val -> System.out.printf("%s,", val.toString())));

        System.out.printf("done");

        return jobDuration;
    }
}