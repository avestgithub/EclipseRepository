import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.message.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.*;
import org.apache.http.impl.client.DefaultHttpClient;
import com.alibaba.fastjson.JSONObject;
import  org.apache.http.NameValuePair;
import javax.script.*;
import java.io.*;
import java.util.regex.*;
import java.net.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class SinaWeibo {
	    private HttpClient client;
	    private String username;    //登录帐号(明文)
	    private String password;    //登录密码(明文)
	    private String su;          //登录帐号(Base64加密)
        private String sp;          //登录密码(各种参数RSA加密后的密文)
	    private long servertime;    //初始登录时，服务器返回的时间戳,用以密码加密以及登录用
	    private String nonce;       //初始登录时，服务器返回的一串字符，用以密码加密以及登录用
	    private String rsakv;       //初始登录时，服务器返回的一串字符，用以密码加密以及登录用
	    private String pubkey;      //初始登录时，服务器返回的RSA公钥
	     
	    private String errInfo;     //登录失败时的错误信息
	    private String location;    //登录成功后的跳转连接
	     
	    public SinaWeibo(String username,String password){
	        client = new DefaultHttpClient();
	        this.username = username;
	        this.password = password;
	    }
	     
	    /**
	     * 初始登录信息<br>
	     * 返回false说明初始失败
	     * @return
	     */
	   public boolean preLogin(){
	        boolean flag = false;
	        try {
	            su = Base64.encodeBase64String(URLEncoder.encode(username, "UTF-8").getBytes());
	            String url = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.5)&_="+getTimestamp();
	            url += "&su="+su;
	            String content;
	            content = HttpTools.getRequest(client, url);
	            //System.out.println(content);
	            JSONObject json = JSONObject.parseObject(content);
	            servertime = json.getLongValue("servertime");
	            nonce = json.getString("nonce");
	            rsakv = json.getString("rsakv");
	            pubkey = json.getString("pubkey");
	            flag = encodePwd();
	        } catch (UnsupportedEncodingException e) {
	            // TODO Auto-generated catch block
	            //e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            // TODO Auto-generated catch block
	            //e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            //e.printStackTrace();
	        }
	        return flag;
	    }
	     
	    /**
056	     * 登录
057	     * @return true:登录成功
058	     */
	    public boolean login(){
	        if(preLogin()){
	            String url = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.5)";
	            List<NameValuePair> parms = new ArrayList<NameValuePair>();
	            parms.add(new BasicNameValuePair("entry","weibo"));
	            parms.add(new BasicNameValuePair("gateway","1"));
	            parms.add(new BasicNameValuePair("from",""));
	            parms.add(new BasicNameValuePair("savestate","7"));
	            parms.add(new BasicNameValuePair("useticket","1"));
	            parms.add(new BasicNameValuePair("pagerefer","http://login.sina.com.cn/sso/logout.php?entry=miniblog&r=http%3A%2F%2Fweibo.com%2Flogout.php%3Fbackurl%3D%2F"));
	            parms.add(new BasicNameValuePair("vsnf","1"));
	            parms.add(new BasicNameValuePair("su",su));
	            parms.add(new BasicNameValuePair("service","miniblog"));
	            parms.add(new BasicNameValuePair("servertime",servertime+""));
	            parms.add(new BasicNameValuePair("nonce",nonce));
	            parms.add(new BasicNameValuePair("pwencode","rsa2"));
	            parms.add(new BasicNameValuePair("rsakv",rsakv));
	            parms.add(new BasicNameValuePair("sp",sp));
	            parms.add(new BasicNameValuePair("encoding","UTF-8"));
	            parms.add(new BasicNameValuePair("prelt","182"));
	            parms.add(new BasicNameValuePair("url","http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
	            parms.add(new BasicNameValuePair("returntype","META"));
	            try {
	                String content = HttpTools.postRequest(client, url, parms);
	                //System.out.println(content);
	                String regex = "location\\.replace\\(\"(.+?)\"\\);";
	                Pattern p = Pattern.compile(regex);
	                Matcher m = p.matcher(content);
	                if(m.find()){
	                    location = m.group(1);
	                    if(location.contains("reason=")){
	                        errInfo = location.substring(location.indexOf("reason=")+7);
	                        errInfo = URLDecoder.decode(errInfo, "GBk");
	                    }else{
	                        String result = HttpTools.getRequest(client, location);
	                       System.out.println(result);
	                        result = HttpTools.getRequest(client, "http://weibo.com/3818904514/myfollow");
	                        //System.out.println(result);
	                        return true;
	                    }
	                }
	            } catch (ClientProtocolException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	        return false;
	    }
	     
	    /**
110	     * 密码进行RSA加密<br>
111	     * 返回false说明加密失败
112	     * @return
113	     */
	    private boolean encodePwd(){
	        ScriptEngineManager sem = new ScriptEngineManager();
	        ScriptEngine se = sem.getEngineByName("javascript");
	        try {
	            se.eval(new FileReader("encoder.js"));
	            Invocable invocableEngine = (Invocable) se;
	            String callbackvalue=(String) invocableEngine.invokeFunction("encodePwd",pubkey,servertime,nonce,password);
	            sp = callbackvalue;
	            return true;
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            System.out.println("加密脚本encoder.sj未找到");
	        } catch (ScriptException e) {
	            // TODO Auto-generated catch block
	            //e.printStackTrace();
	        } catch (NoSuchMethodException e) {
	            // TODO Auto-generated catch block
	            //e.printStackTrace();
	        }
	        errInfo = "密码加密失败";
	        return false;
	    }
	    
	    public String getErrInfo() {
	        return errInfo;
	    }
	     
	    /**
	     * 获取时间戳
	     * @return
	     */
	    private long getTimestamp(){
	        Date now = new Date();
        return now.getTime();
	    }
	     
	    public static void main(String[] args) throws ClientProtocolException, IOException {
	    	SinaWeibo weibo = new SinaWeibo("qiusd1989@gmail.com","6182288qiusd");
	    	if(weibo.login()){
	            System.out.println("登录成功");
	            //String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; //����JDBC��
	            //String dbURL = "jdbc:sqlserver://localhost:1433; DatabaseName=weibo"; //l�ӷ��������ݿ�test
	            //String userName = "sa"; //Ĭ���û���
	            //String userPwd = "1234"; //��װsql server 2005ʱ������
	            //Connection dbConn;
	            try {
		            //Class.forName(driverName);
		            //dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
		            //System.out.println("Connection Successful!"); //���l�ӳɹ� ����̨���Connection Successful!
		            //Statement stmt = dbConn.createStatement();
//		            String query = "SELECT id FROM youxiao where row between 5216 and 5455";
//		            ResultSet rs = stmt.executeQuery(query);
		            String Result=null;
//		            while (rs.next()) 
//		            {
		            	String s = "lovekym";
//		            	String s = rs.getString(1);
//		                System.out.println(s);
		                //Result = HttpTools.getRequest(weibo.client, "http://weibo.com/"+s+"/myfollow");
			            Result = HttpTools.getRequest(weibo.client, "http://weibo.com/p/100505"+s+"/info?from=page_100505&mod=TAB#place");
			            Result = HttpTools.getRequest(weibo.client, "http://weibo.com/"+s+"?from=feed&loc=nickname");
			            
			            //System.out.println(Result);
			            System.out.println(Result);
//			            BufferedWriter writer=new BufferedWriter(new FileWriter("D:\\youxiao\\"+s+".txt"));
//			            writer.write(Result);
//			            writer.newLine();
//			            writer.flush();
//			            writer.close();
//			        }
		            } catch (Exception e) {
		            e.printStackTrace();
		            }
	        }else{
	            System.out.println("登录失败："+weibo.getErrInfo());
	        }
	    }
	}





