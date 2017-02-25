package edu.carleton.comp4601.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	Crawler crawler;
	
    public void test() throws Exception {
    	
    	crawler = new Crawler();

        String rootFolder = "/Users/thomasmurphy/Documents/Coding/Eclipse Projects/COMP4601-SDA2/pagestore/";
        int numberOfCrawlers = 1;
        String storageFolder = rootFolder + "test/";

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(rootFolder);
        config.setMaxPagesToFetch(5);

        String[] crawlDomains = {"http://sikaman.dyndns.org:8888/courses/4601/handouts/"};

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        for (String domain : crawlDomains) {
            controller.addSeed(domain);
        }

        crawler.configure(crawlDomains, storageFolder);

        controller.start(Crawler.class, numberOfCrawlers);
    }

  /*  public static void main(String[] args) throws Exception {
    	Controller mainCont = new Controller(1);
    	CrawlController cont1 = mainCont.buildController(100,100);
    	
    	cont1.addSeed("https://en.wikipedia.org/");
    	cont1.start(Crawler.class, 1);
    }
    
    public Controller(int crawlerID){
    	
		
    }

    private CrawlController buildController(int politness, int maxPages) throws Exception{
        CrawlConfig config = new CrawlConfig();
        
        config.setCrawlStorageFolder("/pagestore/");
        
        config.setPolitenessDelay(1000);
        config.setMaxPagesToFetch(50);
        
        PageFetcher fetcher = new PageFetcher(config);
        RobotstxtServer server = new RobotstxtServer(new RobotstxtConfig(), fetcher);
    	
    	return new CrawlController(config, fetcher, server);
    }*/
}
