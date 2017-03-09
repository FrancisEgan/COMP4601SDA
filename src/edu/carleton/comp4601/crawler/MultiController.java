package edu.carleton.comp4601.crawler;

import java.util.ArrayList;

//github.com/FrankEgan/COMP4601SDA.git
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class MultiController {

	static String storageFolder = System.getProperty("user.dir") + "/pagestore/";
	static ArrayList<CrawlController> controllers = new ArrayList<CrawlController>();

    public static CrawlController buildController(String storageFolder, String seed) throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setIncludeBinaryContentInCrawling(true);
        config.setCrawlStorageFolder(storageFolder);
        config.setPolitenessDelay(1000);
        config.setMaxPagesToFetch(5);

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
