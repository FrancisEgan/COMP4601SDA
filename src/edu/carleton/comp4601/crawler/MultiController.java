package edu.carleton.comp4601.crawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class MultiController {

    public static CrawlController buildController(String storageFolder, String seed) throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(storageFolder);
        config.setPolitenessDelay(1000);
        config.setMaxPagesToFetch(50);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        String[] crawler1Domains = {"http://www.cnn.com/"}; // for shouldVisit in crawler
        controller.setCustomData(crawler1Domains);
        controller.addSeed(seed);

        return controller;
        
        //controller.waitUntilFinish();
        //System.out.println("Crawler 1 is finished.");
    }
}
