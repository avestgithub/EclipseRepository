
import java.io.IOException;
import java.util.List;
import java.io.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import  org.apache.http.NameValuePair;
public class HttpTools {

	    public static String getRequest(HttpClient client,String url) throws ClientProtocolException, IOException{
	        HttpGet get = new HttpGet(url);
	        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
	        //get.addHeader("Referer", "http://2013.weibo.com/");
	        HttpResponse response = client.execute(get);
	        HttpEntity entity = response.getEntity();
	       	        String content = EntityUtils.toString(entity,"UTF-8");
	        //System.out.println(content);
	        EntityUtils.consume(entity);
	        
	        return content;
	    }
	     
	    /**
23	     * ��POST��ʽHTTP����
24	     * @param client
25	     * @param url
26	     * @param parms
27	     * @return
28	     * @throws ClientProtocolException
29	     * @throws IOException
30	     */
	    public static String postRequest(HttpClient client,String url,List<NameValuePair> parms) throws ClientProtocolException, IOException{
	        HttpPost post = new HttpPost(url);
	        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
	        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
	        //post.addHeader("Referer", "http://2013.weibo.com/");
	        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(parms, "UTF-8");
	        post.setEntity(postEntity);
	        HttpResponse response = client.execute(post);
	        HttpEntity entity = response.getEntity();
	        String content = EntityUtils.toString(entity,"UTF-8");
	        EntityUtils.consume(entity);
	        return content;
	    }
	    public static String postRequest(HttpClient client,String url) throws ClientProtocolException, IOException{
	        HttpPost post = new HttpPost(url);
	        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
	        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
	        //post.addHeader("Referer", "http://2013.weibo.com/");
//	        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(parms, "UTF-8");
//	        post.setEntity(postEntity);
	        HttpResponse response = client.execute(post);
	        HttpEntity entity = response.getEntity();
	        String content = EntityUtils.toString(entity,"UTF-8");
	        EntityUtils.consume(entity);
	        return content;
	    }
	}