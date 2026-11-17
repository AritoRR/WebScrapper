package scrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scrapper.api.ColorApi;
import scrapper.api.DogApi;
import scrapper.api.NekoApi;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;



public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static Integer maxThreads;
    private static Integer timeOut;
    private static String[] services;

    public static boolean extractingArgs(String[] args)
    {
        try {
            if (args.length < 4) {
                throw new InvalidParameterException("Invalid number of arguments");
            }

            maxThreads = Integer.parseInt(args[0]);
            timeOut = Integer.parseInt(args[1]);
            if (maxThreads <= 0 || timeOut <= 0) {
                throw new InvalidParameterException("Wrong parameter");
            }

            services = Arrays.copyOfRange(args, 2, args.length - 1);

            String type = args[args.length - 1];
            if (!type.equals("json") && !type.equals("csv")) {
                throw new InvalidParameterException(type + " is not a valid output format");
            }
            OutputWriter.setType(type.toLowerCase());
        }
        catch (InvalidParameterException |
               NumberFormatException e) {
            logger.error(e);
            return false;
        }
        return true;
    }

    public static void scrape(ExecutorService executor, CountDownLatch latch)
    {
        AtomicBoolean running = new AtomicBoolean(true);

        for (String serviceName : services) {
            switch (serviceName) {
                case "dog-api" ->
                        executor.execute(new Scrapper(executor, latch, timeOut, new DogApi(), running));
                case "neko-api" ->
                        executor.execute(new Scrapper(executor, latch, timeOut, new NekoApi(), running));
                case "colormind-api" ->
                        executor.execute(new Scrapper(executor, latch, timeOut, new ColorApi(), running));
                default -> logger.warn("Unsupported source {}. Skipped...", serviceName);
            }
        }

        new Thread(() -> {
            try {
                latch.await();
                running.set(false);
                executor.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public static void main(String[] args) {
        if (!extractingArgs(args)) {
            System.exit(1);
        }
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        CountDownLatch latch = new CountDownLatch(9);
        scrape(executor, latch);
    }
}
