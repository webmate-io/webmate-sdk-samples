package examples;

import com.testfabrik.webmate.javasdk.WebmateAPISession;
import com.testfabrik.webmate.javasdk.WebmateAuthInfo;
import com.testfabrik.webmate.javasdk.WebmateEnvironment;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.net.*;
import java.util.Map;

public class ProxyTest {

    @Test
    public void testWithProxyWebdriver() {

        String PROXY_HOST = "proxyhost";
        int PROXY_PORT = 3128;
        String PROXY_USER_DOMAIN = "userdomain";
        String PROXY_USER = "proxyuser";
        String PROXY_PASSWORD = "proxypass";

        WebmateAPISession apiSession = sessionWithNoAuthProxy(new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY),
                WebmateEnvironment.create(), PROXY_HOST, PROXY_PORT);

//        WebmateAPISession apiSession = sessionWithAuthProxy(new WebmateAuthInfo(MyCredentials.MY_WEBMATE_USERNAME, MyCredentials.MY_WEBMATE_APIKEY),
//                WebmateEnvironment.create(), PROXY_HOST, PROXY_PORT, PROXY_USER_DOMAIN, PROXY_USER, PROXY_PASSWORD);

        apiSession.device.getDeviceIdsForProject(MyCredentials.MY_WEBMATE_PROJECTID);
    }


    public WebmateAPISession sessionWithAuthProxy(WebmateAuthInfo authInfo, WebmateEnvironment env, String proxyHost, int proxyPort, String proxyUserDomain,
                                              String proxyUser, String proxyPassword) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        URI apiUrl = env.baseURI;

        credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new NTCredentials(proxyUser, proxyPassword, getWorkstation(), proxyUserDomain));
        if (apiUrl.getUserInfo() != null && !apiUrl.getUserInfo().isEmpty()) {
            credsProvider.setCredentials(new AuthScope(apiUrl.getHost(), apiUrl.getPort() > 0 ? apiUrl.getPort() : 443),
                    new UsernamePasswordCredentials(apiUrl.getUserInfo()));
        }
        builder.setProxy(proxy);
        builder.setDefaultCredentialsProvider(credsProvider);
        return new WebmateAPISession(authInfo, env, builder);
    }

    public WebmateAPISession sessionWithNoAuthProxy(WebmateAuthInfo authInfo, WebmateEnvironment env, String proxyHost, int proxyPort) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        builder.setProxy(proxy);
        return new WebmateAPISession(authInfo, env, builder);
    }

    private String getWorkstation() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            return env.get("HOSTNAME");
        } else {
            try {
                return InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException ex) {
                return "Unknown";
            }
        }
    }
}
