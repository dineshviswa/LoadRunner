/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.surya.soft.loadtestrunner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 * @author dviswanathan
 */
public class LoadTestRunner implements Runnable {

    static String userEmailId;
    static int threadCount;
    static URL webServiceURL;
    static int threadID;
    static String contentType = "";
    static String acceptType = "";
    static int totalRequest;
    static int noOfRequest;
    public static List<Double> getRequestResponseTime = new ArrayList<Double>();
    public static List<Double> postRequestResponseTime = new ArrayList<Double>();

    static ArrayList<String> mylist = new ArrayList<String>();

    public LoadTestRunner(String emailid, int threadCnt, URL url, int id, String reqType, String acptType, int toRequest, int req, List<Double> getResTime, List<Double> postResTime) {
        userEmailId = emailid;
        threadCount = threadCnt;
        webServiceURL = url;
        threadID = id;
        contentType = reqType;
        acceptType = acptType;
        totalRequest = toRequest;
        noOfRequest = req;
        getRequestResponseTime = getResTime;
        postRequestResponseTime = postResTime;
    }

    /**
     * Main Method
     *
     * @param args
     */
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("Config.properties"));


            // Getting input arguments
            userEmailId = properties.getProperty("userEmailID");
            webServiceURL = new URL(properties.getProperty("webURL"));
            contentType = "application/json";
            acceptType = "application/json";
            threadCount = Integer.parseInt(properties.getProperty("ThreadCount"));
            totalRequest = Integer.parseInt(properties.getProperty("Totalrequest"));

            if (totalRequest < threadCount) {
                System.out.println("Total Request should be higher than thread count. Please change it in the configuration file");
                System.exit(0);
            }

            try {

                Thread[] workerThreads = new Thread[threadCount];
                int threadsInitialized = 0;
                System.out.println("Load Runner process has started..");
                int requestCount = 0;
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date StartTimeStamp = new Date();
                System.out.println("Start Time : " + formatter.format(StartTimeStamp));

                long start = System.currentTimeMillis();
                int resourceThreadCount = new Double(Math.ceil(totalRequest / threadCount)).intValue();
                int remainingCount = 0;
                if ((resourceThreadCount * threadCount) == totalRequest) {
                    remainingCount = 0;
                } else {
                    remainingCount = totalRequest - (resourceThreadCount * threadCount);
                }
                LoadTestRunner loadRunner = null;
                for (int index = 0; index < threadCount; index++) {
                    int resourceCount = new Double(Math.ceil(totalRequest / threadCount)).intValue();
                    int count = ((totalRequest - requestCount) > resourceCount
                            ? resourceCount : (totalRequest - requestCount));
                    if (count > 0) { // To handle round exceptions
                        if (remainingCount != 0) {
                            resourceCount += 1;
                            remainingCount--;
                        }
                        loadRunner = new LoadTestRunner(userEmailId, threadCount, webServiceURL, index + 1, contentType, acceptType, totalRequest, resourceCount, getRequestResponseTime, postRequestResponseTime);
                        workerThreads[index] = new Thread(loadRunner);
                        workerThreads[index].start();
                        Thread.sleep(1000);  // Let us wait till the thread get initialized
                        threadsInitialized++;
                        requestCount += resourceCount;
                    }
                }

                while (true) {
                    boolean workersActive = false;
                    for (int i = 0; i < threadsInitialized; i++) {
                        if (workerThreads[i].getState() == Thread.State.RUNNABLE
                                || workerThreads[i].isAlive()) {
                            workersActive = true;
                        }
                    }
                    if (workersActive) {
                        Thread.sleep(500L);  // Sleep 1/2 second
                    } else {
                        break;
                    }
                }
                Date EndTimeStamp = new Date();

                Percentile percentile = new Percentile();
                StandardDeviation standardDeviation = new StandardDeviation(false);
                Mean mean = new Mean();

                System.out.println("Get Request Response Time Calculation : ");
                System.out.println("---------------------------------------");
                System.out.println("10th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()])), 10.0) + " seconds");
                System.out.println("50th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()])), 50.0) + " seconds");
                System.out.println("90th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()])), 90.0) + " seconds");
                System.out.println("95th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()])), 95.0) + " seconds");
                System.out.println("99th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()])), 99.0) + " seconds");
                System.out.println("Mean value: " + mean.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()]))) + " seconds");
                System.out.println("Standard Deviation value: " + standardDeviation.evaluate(ArrayUtils.toPrimitive(getRequestResponseTime.toArray(new Double[getRequestResponseTime.size()]))) + " seconds");

                System.out.println("Post Request Response Time Calculation : ");
                System.out.println("---------------------------------------");
                System.out.println("10th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()])), 10.0) + " seconds");
                System.out.println("50th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()])), 50.0) + " seconds");
                System.out.println("90th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()])), 90.0) + " seconds");
                System.out.println("95th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()])), 95.0) + " seconds");
                System.out.println("99th percentile value: " + percentile.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()])), 99.0) + " seconds");
                System.out.println("Mean value: " + mean.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()]))) + " seconds");
                System.out.println("Standard Deviation value: " + standardDeviation.evaluate(ArrayUtils.toPrimitive(postRequestResponseTime.toArray(new Double[postRequestResponseTime.size()]))) + " seconds");

                System.out.println("Program is exited successfully");
            } catch (Exception ex) {
                System.out.println("Error : " + ex.getMessage());
            }

            System.exit(1);

        } catch (Exception interrupt) {

            System.out.println("Error : " + interrupt.getMessage());
        }
    }

    public LoadTestRunner() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Run Method
     */
    @Override
    public void run() {
        try {
            Thread currentThread = Thread.currentThread();
            for (int i = 0; i < noOfRequest; i++) {
                sendHTTPRequest(webServiceURL, userEmailId, i + 1, currentThread.getId());
            }
        } catch (Exception ex) {
            System.out.println("Error : " + ex.getMessage());
        }
    }

    public String getTimeDifference(Date startTime, Date endTime) {
        float diffenceFloat = endTime.getTime() - startTime.getTime();
        String diffenceSeconds = String.valueOf((diffenceFloat / 1000 % 60));
        return diffenceSeconds;
    }
    
     /**
     * Send HTTP Request
     *
     * @param httpRestRequest
     * @param transactionID
     * @return
     * @throws Exception
     */
    public void sendHTTPRequest(URL url, String emailID,int requestNumber, long threadID) throws Exception {
        HttpURLConnection getRequest = null;
        BufferedReader in = null;
        try {
            //logger.info("Sending HTTP request to the web service " + httpRestRequest.webServiceURL);
            // // Setup the Request
            System.out.println("Thread ID : " + threadID + " Request Number : " + requestNumber);
            getRequest = (HttpURLConnection) url.openConnection();
            getRequest.setConnectTimeout(50000);
            getRequest.setReadTimeout(60000);
            getRequest.setRequestMethod("GET");
            getRequest.addRequestProperty("X-Surya-Email-Id", emailID);

            // Get Response
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date getStartTimeStamp = new Date();
            
            int responseCode = getRequest.getResponseCode();
            Date getEndTimeStamp = new Date();
            String getTransactiondiff = getTimeDifference(getStartTimeStamp, getEndTimeStamp);
            LoadTestRunner.getRequestResponseTime.add(Double.parseDouble(getTransactiondiff));
            StringBuilder response = new StringBuilder();
            String inputLine;
            if (responseCode >= 400) {
                in = new BufferedReader(new InputStreamReader(getRequest.getErrorStream(), "UTF-8"));
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    response.append("\n");
                }
                in.close();
            } else {
                in = new BufferedReader(new InputStreamReader(getRequest.getInputStream(), "UTF-8"));
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    HttpURLConnection postRequest = null;
                    postRequest = (HttpURLConnection) url.openConnection();
                    postRequest.setConnectTimeout(50000);
                    postRequest.setReadTimeout(60000);
                    postRequest.setRequestMethod("POST");
                    postRequest.setRequestProperty("Content-Type", "application/json");

                    byte[] byteArray = inputLine.getBytes("UTF-8");

                    postRequest.setRequestProperty("Content-Length", "" + Integer.toString(byteArray.length));
                    postRequest.setUseCaches(false);
                    postRequest.setDoInput(true);
                    postRequest.setDoOutput(true);

                    try (DataOutputStream wr = new DataOutputStream(postRequest.getOutputStream())) {
                        wr.writeBytes(inputLine);
                        wr.flush();
                    }
                    Date postStartTimeStamp = new Date();
                    int postResponseCode = postRequest.getResponseCode();
                    Date postEndTimeStamp = new Date();
                    String postTransactiondiff = getTimeDifference(postStartTimeStamp, postEndTimeStamp);
                    LoadTestRunner.postRequestResponseTime.add(Double.parseDouble(postTransactiondiff));
                    StringBuilder postResponse = new StringBuilder();
                    String postInputLine;
                    BufferedReader postIn = null;
                    if (responseCode >= 400) {
                        postIn = new BufferedReader(new InputStreamReader(postRequest.getErrorStream(), "UTF-8"));
                        postResponse = new StringBuilder();
                        while ((postInputLine = postIn.readLine()) != null) {
                            postResponse.append(inputLine);
                            postResponse.append("\n");
                        }
                        postIn.close();
                    } else {
                        postIn = new BufferedReader(new InputStreamReader(postRequest.getInputStream(), "UTF-8"));
                        postResponse = new StringBuilder();
                        while ((postInputLine = postIn.readLine()) != null) {
                            postResponse.append(postInputLine);
                            postResponse.append("\n");
                        }
                        postIn.close();
                    }
                }
                response.append(inputLine);
                response.append("\n");
            }
            in.close();
        } //logger.info("<TransactionID:" + transactionID + "> - Response received");
        catch (IOException e) {
            System.out.println("Exception : " + e.getMessage());

        } finally {
            if (in != null) {
                in.close();
            }
        }

    }
}
