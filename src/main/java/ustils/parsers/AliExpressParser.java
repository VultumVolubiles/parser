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

            float originalPrice = new Float(jItem.getString("oriMinPrice").substring(4));
            float discount = jItem.getFloat("discount");
            float currentPrice = originalPrice * (1 - discount/100);
            jItem.put("currentPrice", jItem.getString("oriMinPrice").substring(0, 4)
                    + String.format("%.2f", currentPrice));

            items.add(jItem.toMap());
        }
        // todo size

        return items;
    }

}
