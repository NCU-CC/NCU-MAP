package tw.edu.ncu.cc.location.client.tool.config;

public interface LocationConfig {

    public String getServerAddress();
    public String getApiToken();
    public LocationConfig setServerAddress( String serverAddress );
    public LocationConfig setApiToken( String apiToken );

    public LocationConfig configure( String configFilePath );

}
