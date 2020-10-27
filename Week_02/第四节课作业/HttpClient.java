import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {
    public static void main(String[] args) {
        //使用HttpClient访问 http://localhost:8801/
        HttpGet httpGet = new HttpGet("http://localhost:8801/");
        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpGet)) {
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println("内容长度：" + content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
