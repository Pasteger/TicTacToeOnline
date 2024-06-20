package jjd.pasteger.tictactoe.clientnetty;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class ClientConfig {
    private PropertiesConfiguration config;

    public ClientConfig(String configFile) {
        try {
            Configurations configs = new Configurations();
            config = configs.properties(new File(configFile));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return config.getString("server.host");
    }

    public int getPort() {
        return config.getInt("server.port");
    }
}
