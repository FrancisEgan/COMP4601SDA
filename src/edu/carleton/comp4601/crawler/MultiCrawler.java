package edu.carleton.comp4601.crawler;


	import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


	public class MultiCrawler extends WebCrawler {

	    private static final Pattern FILTERS = Pattern.compile(
	        ".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" +
	        "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	    private String[] myCrawlDomains;

	    @Override
	    public void onStart() {
	        myCrawlDomains = (String[]) myController.getCustomData();
	    }

	    @Override
	    public boolean shouldVisit(Page referringPage, WebURL url) {
	        String href = url.getURL().toLowerCase();
	        if (FILTERS.matcher(href).matches()) {
	            return false;
	        }

	        for (String crawlDomain : myCrawlDomains) {
	            if (href.startsWith(crawlDomain)) {
	                return true;
	            }
	        }

	        return false;
	    }

	    @Override
	    public void visit(Page page) {
	        int docid = page.getWebURL().getDocid();
	        String url = page.getWebURL().getURL();
	        int parentDocid = page.getWebURL().getParentDocid();

	        System.out.println("Docid: " + docid);
	        System.out.println("URL: " + url);
	        System.out.println("Docid of parent page: " + parentDocid);

	        if (page.getParseData() instanceof HtmlParseData) {
	            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
	            String text = htmlParseData.getText();
	            String html = htmlParseData.getHtml();
	            Set<WebURL> links = htmlParseData.getOutgoingUrls();

	            System.out.println("Text length: " + text.length());
	            System.out.println("Html length: " + html.length());
	            System.out.println("Number of outgoing links: " + links.size());
	        }
	        
            // get a unique name for storing this image
            String extension = url.substring(url.lastIndexOf('.'));
            String hashedName = UUID.randomUUID() + extension;

            // store image
            String storageFolder = this.getMyController().getConfig().getCrawlStorageFolder();
            String filename = storageFolder + "/" + hashedName;
            try {
                Files.write(new File(filename).toPath(), page.getContentData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                System.out.println("Stored: " + url);
            } catch (IOException iox) {
            	iox.printStackTrace();
                System.err.println("Failed to write file: " + filename);
            }

	        System.out.println("=============");
	    }
}
