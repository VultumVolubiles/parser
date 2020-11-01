package ustils.parsers;

import com.gargoylesoftware.htmlunit.WebClient;

/*
    Abstract class for site parsers
 */
public abstract class Parser {
    private final WebClient webClient;

    public Parser(WebClient webClient) {
        this.webClient = webClient;
    }

    public WebClient getWebClient() {
        return webClient;
    }
}
