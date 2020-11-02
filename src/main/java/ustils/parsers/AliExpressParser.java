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

        for (HtmlDivision itemElement : itemElements) {
            Map<String, Object> props = new HashMap<>();
            DomElement a = itemElement.getFirstElementChild();

            // get deal item link
            if (a.getTagName().equals("a") && a.hasAttribute("href")) {
                String link = a.getAttribute("href");
                link = link.startsWith("//") ? link.substring(2) : link; // Remove "//" at the beginning
                props.put("link", link);

                for (DomElement div : a.getChildElements()) {
                    String divClass = div.getAttribute("class");

                    // get item image
                    if ("item-image".equals(divClass)) {
                        String src = div.getFirstElementChild().getAttribute("src");
                        src = src.startsWith("//") ? src.substring(2) : src; // Remove "//" at the beginning
                        props.put("image", src);
                    } else if ("item-details".equals(divClass))
                        //get item title, current price, original price and discount
                        for (DomElement detail : div.getChildElements()) {
                            String detailClass = div.getAttribute("class");

                            if ("item-details-title".equals(detailClass))
                                props.put("title", detail.getVisibleText());
                            else if ("current-price".equals(detailClass))
                                props.put("current price", detail.getVisibleText());
                            else if ("original-price".equals(detailClass)) {
                                String text = detail.getVisibleText();
                                String originalPrice = text.substring(0, text.indexOf("|") - 1);
                                String discount = text.substring(text.indexOf("|") + 2, text.indexOf(" off"));
                                props.put("original price", originalPrice);
                                props.put("discount", discount);
                            }
                        }
                }

                items.add(props);
            }
        }
        return items;
    }

}
