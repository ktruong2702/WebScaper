import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadedWebCrawler {
    private static final int MAX_DEPTH = 2;
    private static final int MAX_THREADS = 5; // Adjust the number of threads as needed

    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        String startingUrl = "https://www.gsu.edu";
        crawl(1, startingUrl, new HashSet<>());
    }

    private static void crawl(int level, String url, Set<String> visitedUrls) {
        if (level <= MAX_DEPTH && !visitedUrls.contains(url)) {
            executorService.execute(() -> {
                if (!url.startsWith("http")){
                    return;
                }
                Document document = request(url, visitedUrls);

                if (document != null) {
                    String nextLink="";
                    for (Element link : document.select("a[href]")) {
                        try {
                            nextLink = link.absUrl("href");
                        }catch (Exception e){}
                        if (!nextLink.isEmpty()) {
                            crawl(level + 1, nextLink, visitedUrls);
                        }
                    }
                }
            });
        }
    }

    private static Document request(String url, Set<String> visitedUrls) {
        try {
            Connection connection = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000); // 5 seconds timeout

            connection.followRedirects(true); // Follow redirects

            Document document = connection.get();

            if (connection.response().statusCode() == 200 && !visitedUrls.contains(url)) {
                System.out.println("Link: " + url);
                System.out.println(document.title());

                visitedUrls.add(url);

                return document;
            }
            return null;
        } catch (IOException e) {
            // Handle exception, log it, or ignore it based on your needs
//            e.printStackTrace();
            return null;
        }
    }

    // Call shutdown() after crawling is complete
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executorService.shutdown()));
    }
}
