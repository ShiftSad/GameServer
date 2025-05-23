package dev.shiftsad.core.modules.impl;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import dev.shiftsad.core.modules.BootPriority;
import dev.shiftsad.core.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsulConfigModule implements Module {

    private ConsulClient consulClient;

    @Override
    public void initialize() {
        String consulHost = System.getenv("CONSUL_HOST");
        int consulPort = Integer.parseInt(System.getenv("CONSUL_PORT"));

        if (consulHost == null || consulPort <= 0) {
            throw new RuntimeException("CONSUL_HOST or CONSUL_PORT must be set");
        }

        consulClient = new ConsulClient(consulHost, consulPort);
    }

    @Override
    public void stop() {

    }

    @Override
    public @NotNull BootPriority getBootPriority() {
        return BootPriority.CRITICAL;
    }

    @Override
    public @NotNull String getName() {
        return "ConsulConfig";
    }

    public @Nullable String getConfigValue(String key) {
        Response<GetValue> response = consulClient.getKVValue(key);
        GetValue getValue = response.getValue();

        if (getValue != null && getValue.getValue() != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(getValue.getValue());
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public boolean setConfigValue(@NotNull String key, @NotNull String value) {
        Response<Boolean> response = consulClient.setKVValue(key, value);
        return response.getValue();
    }

    public boolean deleteConfigValue(@NotNull String key) {
        try {
            consulClient.deleteKVValue(key);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public Map<String, String> getConfigValuesByPrefix(String prefix) {
        Map<String, String> configMap = new HashMap<>();
        Response<List<GetValue>> response = consulClient.getKVValues(prefix);
        List<GetValue> values = response.getValue();

        if (values != null) {
            for (GetValue value : values) {
                if (value.getValue() != null) {
                    byte[] decodedBytes = Base64.getDecoder().decode(value.getValue());
                    String key = value.getKey();
                    if (key.startsWith(prefix)) {
                        key = key.substring(prefix.length());
                        if (key.startsWith("/")) {
                            key = key.substring(1);
                        }
                    }
                    configMap.put(key, new String(decodedBytes, StandardCharsets.UTF_8));
                }
            }
        }
        return configMap;
    }
}
