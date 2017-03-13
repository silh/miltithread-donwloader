package org.http.donwloader.multithread;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.http.donwloader.multithread.exceptions.IncorrectSpeedException;
import org.http.donwloader.multithread.execution.Download;
import org.http.donwloader.multithread.execution.Scheduler;
import org.http.donwloader.multithread.execution.impl.DownloadScheduler;
import org.http.donwloader.multithread.input.InputParamType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.http.donwloader.multithread.input.InputParamType.*;

public class Main {
    private static final int DEFAULT_NUMBER_OF_THREADS = 1;
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern SPEED_PATTERN = Pattern.compile("[0-9]+[km0-9]?$");
    private static final int ONE_K = 1024;

    public static void main(String... args) throws Exception {
        //gathering params
        Map<InputParamType, String> rawParams = getRawParams(args);

        //Converting params to actual values
        int numberOfThreads = getNumberOfThreads(rawParams);
        int speed = getSpeed(rawParams);
        List<Download> downloads = getDownloads(rawParams);

        Scheduler scheduler = new DownloadScheduler(numberOfThreads, speed);
        String results = scheduler.start(downloads);
        System.out.println(results);
    }

    private static Map<InputParamType, String> getRawParams(String[] args) {
        Map<InputParamType, String> rawParams = Maps.newEnumMap(InputParamType.class);
        String key = "";
        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0) {
                key = args[i];
            } else {
                InputParamType inputParamTypeType = InputParamType.getParam(key);
                rawParams.put(inputParamTypeType, args[i]);
            }
        }
        return rawParams;
    }

    private static int getNumberOfThreads(Map<InputParamType, String> rawParams) {
        String numberOfThreadsString = rawParams.get(NUMBER_OF_THREADS);
        int numberOfThreads;
        if (Strings.isNullOrEmpty(numberOfThreadsString)) {
            numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
        } else if (NUMBERS_PATTERN.matcher(numberOfThreadsString).matches()) {
            numberOfThreads = Integer.parseInt(rawParams.get(NUMBER_OF_THREADS));
        } else {
            numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
        }
        return numberOfThreads;
    }

    private static int getSpeed(Map<InputParamType, String> rawParams) {
        String speedString = rawParams.get(SPEED);
        int speed;
        //Checking if input is correct
        if (SPEED_PATTERN.matcher(speedString).matches()) {
            //If it's a number - using it
            if (NUMBERS_PATTERN.matcher(speedString).matches()) {
                speed = Integer.parseInt(speedString);
            } else { //If it contains m or k at the end - use multiplier
                int multiplier;
                if (speedString.endsWith("k")) {
                    multiplier = ONE_K;
                } else { //ends with m
                    multiplier = ONE_K * ONE_K;
                }

                speed = Integer.parseInt(speedString.substring(0, speedString.length() - 1)) * multiplier;
            }
        } else {
            String message = String.format("Wrong input parameter speed - %s", speedString);
            System.out.println(message);
            throw new IncorrectSpeedException(message);
        }
        return speed;
    }

    private static List<Download> getDownloads(Map<InputParamType, String> rawParams) throws IOException {
        String inputFileName = rawParams.get(PATH_TO_FILE);
        String outputPath = rawParams.get(PATH_TO_RESULT);
        Stream<String> lines = Files.lines(Paths.get(inputFileName));
        return lines.map(line -> line.split(" "))
                .map(each -> new Download(each[0], each[1], outputPath))
                .collect(Collectors.toList());
    }
}
