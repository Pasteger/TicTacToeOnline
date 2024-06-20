package jjd.pasteger.tictactoe.servernetty;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class ServerConfig {
    private PropertiesConfiguration config;

    public ServerConfig(String configFile) {
        try {
            Configurations configs = new Configurations();
            config = configs.properties(new File(configFile));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return config.getInt("server.port");
    }
}
