package ustils.parsers;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Implementation of the parser for AliExpress
 */
public class AliExpressParser extends Parser {
    private enum PageType {
        FLASH_DEALS,
        ITEM
    }

    public AliExpressParser(WebClient webClient) {
        super(webClient);
    }

    /*
        @param url      page url
        @param size     number of elements to parse (not working)
     */
    public List<Map<String, Object>> parseFlashDeals(String url, int size) throws IOException {
        HtmlPage page = super.getWebClient().getPage(new URL(url));

        List<Map<String, Object>> items = new ArrayList();
        List<HtmlDivision> itemElements;

        itemElements = page.getByXPath("//div[contains(@class, 'deals-item-inner')]");

        /*
            while(itemElements.size() < size) {
                // simulating page scrolling to get new data
                itemElements = page.getByXPath("//div[contains(@class, 'deals-item-inner')]");
            }
         */

        for (HtmlDivision itemElement : itemElements) {
            Map<String, Object> props = new HashMap<>();

            if (itemElement.getFirstElementChild() != null) {
                DomElement child = itemElement.getFirstElementChild();
                handle(PageType.FLASH_DEALS, child, props);
            }
            items.add(props);
        }

        return items;
    }

    public Map<String, Object> parseItemPage(String url) {
        Map<String, Object> props = new HashMap<>();

        // todo
        // handle(PageType.ITEM, someElement, props);

        return props;
    }

    /*
        Handles DomElement for a specific page

        @param type     AliExpress page type
        @param e        html element
        @param props    map for save data
     */
    private void handle(PageType type, DomElement e, Map<String, Object> props) {

        switch (type) {
            case FLASH_DEALS: {
                if (e.getTagName().equals("a") && e.hasAttribute("href")) {
                    String link = e.getAttribute("href");
                    link = link.startsWith("//") ? link.substring(2) : link; // Remove "//" at the beginning
                    props.put("link", link);
                    for (DomElement child : e.getChildElements()) {
                        handle(PageType.FLASH_DEALS, child, props);
                    }
                } else if (e.hasAttribute("class")) {
                    String eClass = e.getAttribute("class");

                    switch (eClass) {
                        case "item-details": {
                            for (DomElement child : e.getChildElements()) {
                                handle(PageType.FLASH_DEALS, child, props);
                            }
                            break;
                        }
                        case "item-details-title": {
                            props.put("title", e.getVisibleText());
                            break;
                        }

                        case "current-price": {
                            props.put("current price", e.getVisibleText());
                            break;
                        }
                        case "original-price": {
                            String text = e.getVisibleText();
                            String originalPrice = text.substring(0, text.indexOf("|")-1);
                            String discount = text.substring(text.indexOf("|") + 2, text.indexOf(" off"));
                            props.put("original price", originalPrice);
                            props.put("discount", discount);
                            break;
                        }
                        case "stock": {
                            // todo
                        }
                        default: {
                            System.out.println("WARN: No handler for class \"" + eClass + "\"");
                        }
                    }
                }
            }
            case ITEM: {
                // todo handler for item page
            }
        }
    }
}
