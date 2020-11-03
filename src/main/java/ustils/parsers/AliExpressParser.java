package ustils.parsers;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

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
        Gets product data from the 'flash deals' page by sending requests.
     */
    public List<Map<String, Object>> parseFlashDeals(String url, int size) throws Exception {
        super.getWebClient().getPage(new URL(url));
        List<Map<String, Object>> items = new ArrayList();

        WebRequest request = new WebRequest(new URL("https://gpsfront.aliexpress.com/getRecommendingResults.do"),
                HttpMethod.GET);

        String cookies = super.getWebClient().getCookies(new URL(url)).toString();
        cookies = cookies.substring(1, cookies.length()-1);
        request.setAdditionalHeader("Cookie", cookies);

        List<NameValuePair> requestParams = new ArrayList<>();
        requestParams.add(new NameValuePair("limit", "50"));
        requestParams.add(new NameValuePair("offset", "0"));
        requestParams.add(new NameValuePair("widget_id", "5547572"));
        request.setRequestParameters(requestParams);

        String content = super.getWebClient().getPage(request).getWebResponse().getContentAsString();
        JSONObject jObject = new JSONObject(content);

        if (!jObject.getBoolean("success"))
            throw new Exception("Couldn't get data");

        String postback = jObject.getString("postback");
        JSONArray jArray = jObject.getJSONArray("results");
        for (int i = 0; i < jArray.length() && items.size() < size; i++) {
            JSONObject jItem = jArray.getJSONObject(i);

            if (jItem.getString("productDetailUrl").startsWith("//"))
                jItem.put("productDetailUrl", jItem.getString("productDetailUrl").substring(2));

            if (jItem.getString("productImage").startsWith("//"))
                jItem.put("productImage", jItem.getString("productImage").substring(2));

            jItem.put("productTitle", jItem.getString("productTitle").replaceAll(" {2}", " "));

            float originalPrice = new Float(jItem.getString("oriMinPrice").substring(4).replaceAll(",", ""));
            float discount = jItem.getFloat("discount");
            float currentPrice = originalPrice * (1 - discount/100);
            jItem.put("currentPrice", jItem.getString("oriMinPrice").substring(0, 4)
                    + String.format("%.2f", currentPrice).replace(',', '.'));

            items.add(jItem.toMap());
        }
        // todo size

        return items;
    }

    /*
        This method can only get data from the first page. Used only to show that the data obtained in this way is equal
         to the data obtained by the {@link #parseFlashDeals(String url, int size)}
     */
    public List<Map<String, Object>> parseFlashDeals(String url) throws IOException {
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
                props.put("productDetailUrl", link);

                for (DomElement div : a.getChildElements()) {
                    String divClass = div.getAttribute("class");

                    // get item image
                    if ("item-image".equals(divClass)) {
                        String src = div.getFirstElementChild().getAttribute("src");
                        src = src.startsWith("//") ? src.substring(2) : src; // Remove "//" at the beginning
                        props.put("productImage", src);
                    } else if ("item-details".equals(divClass))
                        //get item title, current price, original price and discount
                        for (DomElement detail : div.getChildElements()) {
                            String detailClass = detail.getAttribute("class");

                            if ("item-details-title".equals(detailClass))
                                props.put("productTitle", detail.getVisibleText());
                            else if ("current-price".equals(detailClass))
                                props.put("currentPrice", detail.getVisibleText());
                            else if ("original-price".equals(detailClass)) {
                                String text = detail.getVisibleText();
                                String originalPrice = text.substring(0, text.indexOf("|") - 1);
                                String discount = text.substring(text.indexOf("|") + 2, text.indexOf("%"));
                                props.put("oriMinPrice", originalPrice);
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
