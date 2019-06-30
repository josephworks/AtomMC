package org.atom;


import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.bukkit.Bukkit;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.logging.Level;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.TimerTask;
import java.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Metrics
{
    public static final int B_STATS_VERSION = 1;
    private static final String URL = "http://stats.foxyland.su/submitData/atom";
    private static boolean logFailedRequests;
    private static Logger logger;
    private final String name;
    private final String serverUUID;
    private final List<CustomChart> charts;

    public Metrics(final String name, final String serverUUID, final boolean logFailedRequests, final Logger logger) {
        this.charts = new ArrayList<CustomChart>();
        this.name = name;
        this.serverUUID = serverUUID;
        Metrics.logFailedRequests = logFailedRequests;
        Metrics.logger = logger;
        this.startSubmitting();
    }

    public void addCustomChart(final CustomChart chart) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null!");
        }
        this.charts.add(chart);
    }

    private void startSubmitting() {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Metrics.this.submitData();
            }
        }, 1000 * 60 * 5, 1000 * 60 * 30);
    }

    private JSONObject getPluginData() {
        final JSONObject data = new JSONObject();
        data.put("pluginName", this.name);
        final JSONArray customCharts = new JSONArray();
        for (final CustomChart customChart : this.charts) {
            final JSONObject chart = customChart.getRequestJsonObject();
            if (chart == null) {
                continue;
            }
            customCharts.add(chart);
        }
        data.put("customCharts", customCharts);
        return data;
    }

    private JSONObject getServerData() {
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");
        final String osVersion = System.getProperty("os.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();
        final JSONObject data = new JSONObject();
        data.put("serverUUID", this.serverUUID);
        data.put("osName", osName);
        data.put("osArch", osArch);
        data.put("osVersion", osVersion);
        data.put("coreCount", coreCount);
        return data;
    }

    private void submitData() {
        final JSONObject data = this.getServerData();
        final JSONArray pluginData = new JSONArray();
        pluginData.add(this.getPluginData());
        data.put("plugins", pluginData);
        try {
            sendData(data);
        }
        catch (Exception e) {
            if (Metrics.logFailedRequests) {
                Metrics.logger.log(Level.WARNING, "Could not submit stats of " + this.name, e);
            }
        }
    }

    private static void sendData(final JSONObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        }
        final HttpURLConnection connection = (HttpURLConnection)new URL(URL).openConnection();
        final byte[] compressedData = compress(data.toString());
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/1");
        connection.setDoOutput(true);
        final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(compressedData);
        outputStream.flush();
        outputStream.close();
        connection.getInputStream().close();
    }

    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return outputStream.toByteArray();
    }

    static {
        Metrics.logFailedRequests = false;
        Metrics.logger = Logger.getLogger("bStats");
    }

    public abstract static class CustomChart
    {
        final String chartId;

        CustomChart(final String chartId) {
            if (chartId == null || chartId.isEmpty()) {
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            }
            this.chartId = chartId;
        }

        private JSONObject getRequestJsonObject() {
            final JSONObject chart = new JSONObject();
            chart.put("chartId", this.chartId);
            try {
                final JSONObject data = this.getChartData();
                if (data == null) {
                    return null;
                }
                chart.put("data", data);
            }
            catch (Throwable t) {
                if (Metrics.logFailedRequests) {
                    Metrics.logger.log(Level.WARNING, "Failed to get data for custom chart with id " + this.chartId, t);
                }
                return null;
            }
            return chart;
        }

        protected abstract JSONObject getChartData() throws Exception;
    }

    public static class SimplePie extends CustomChart
    {
        private final Callable<String> callable;

        public SimplePie(final String chartId, final Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final String value = this.callable.call();
            if (value == null || value.isEmpty()) {
                return null;
            }
            data.put("value", value);
            return data;
        }
    }

    public static class AdvancedPie extends CustomChart
    {
        private final Callable<Map<String, Integer>> callable;

        public AdvancedPie(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final JSONObject values = new JSONObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue;
                }
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    public static class DrilldownPie extends CustomChart
    {
        private final Callable<Map<String, Map<String, Integer>>> callable;

        public DrilldownPie(final String chartId, final Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        public JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final JSONObject values = new JSONObject();
            final Map<String, Map<String, Integer>> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean reallyAllSkipped = true;
            for (final Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                final JSONObject value = new JSONObject();
                boolean allSkipped = true;
                for (final Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    value.put(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    values.put(entryValues.getKey(), value);
                }
            }
            if (reallyAllSkipped) {
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    public static class SingleLineChart extends CustomChart
    {
        private final Callable<Integer> callable;

        public SingleLineChart(final String chartId, final Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final int value = this.callable.call();
            if (value == 0) {
                return null;
            }
            data.put("value", value);
            return data;
        }
    }

    public static class MultiLineChart extends CustomChart
    {
        private final Callable<Map<String, Integer>> callable;

        public MultiLineChart(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final JSONObject values = new JSONObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue;
                }
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    public static class SimpleBarChart extends CustomChart
    {
        private final Callable<Map<String, Integer>> callable;

        public SimpleBarChart(final String chartId, final Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final JSONObject values = new JSONObject();
            final Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                final JSONArray categoryValues = new JSONArray();
                categoryValues.add(entry.getValue());
                values.put(entry.getKey(), categoryValues);
            }
            data.put("values", values);
            return data;
        }
    }

    public static class AdvancedBarChart extends CustomChart
    {
        private final Callable<Map<String, int[]>> callable;

        public AdvancedBarChart(final String chartId, final Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            final JSONObject data = new JSONObject();
            final JSONObject values = new JSONObject();
            final Map<String, int[]> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (final Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) {
                    continue;
                }
                allSkipped = false;
                final JSONArray categoryValues = new JSONArray();
                for (final int categoryValue : entry.getValue()) {
                    categoryValues.add(categoryValue);
                }
                values.put(entry.getKey(), categoryValues);
            }
            if (allSkipped) {
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    public static class PaperMetrics
    {
        public static void startMetrics() {
            final File configFile = new File(new File((File) FMLCommonHandler.instance().getMinecraftServerInstance().getServer().options.valueOf("plugins"), "bStats"), "config.yml");
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            if (!config.isSet("serverUuid")) {
                config.addDefault("enabled", true);
                config.addDefault("serverUuid", UUID.randomUUID().toString());
                config.addDefault("logFailedRequests", false);
                config.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);
                try {
                    config.save(configFile);
                }
                catch (IOException ex) {}
            }
            final String serverUUID = config.getString("serverUuid");
            final boolean logFailedRequests = config.getBoolean("logFailedRequests", false);
            if (config.getBoolean("enabled", true)) {
                final Metrics metrics = new Metrics("Atom", serverUUID, logFailedRequests, Bukkit.getLogger());
                final String[] minecraftVersion = new String[1];
                final String[] minecraftVersion2 = new String[1];
                metrics.addCustomChart(new SimplePie("minecraft_version", () -> {
                    minecraftVersion[0] = Bukkit.getVersion();
                    minecraftVersion2[0] = minecraftVersion[0].substring(minecraftVersion[0].indexOf("MC: ") + 4, minecraftVersion[0].length() - 1);
                    return minecraftVersion2[0];
                }));
                metrics.addCustomChart(new SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));
                metrics.addCustomChart(new SimplePie("online_mode", () -> Bukkit.getOnlineMode() ? "online" : "offline"));
                metrics.addCustomChart(new SimplePie("paper_version", () -> (Metrics.class.getPackage().getImplementationVersion() != null) ? Metrics.class.getPackage().getImplementationVersion() : "unknown"));
                metrics.addCustomChart(new DrilldownPie("java_version", () -> {
                    Map<String, Map<String, Integer>> map = new HashMap<>();
                    String javaVersion = System.getProperty("java.version");
                    HashMap<String, Integer> entry = new HashMap<String, Integer>();
                    entry.put(javaVersion, 1);
                    String majorVersion = javaVersion.split("\\.")[0];
                    String release;
                    Matcher versionMatcher;
                    int indexOf = javaVersion.lastIndexOf(46);
                    if (majorVersion.equals("1")) {
                        release = "Java " + javaVersion.substring(0, indexOf);
                    }
                    else {
                        versionMatcher = Pattern.compile("\\d+").matcher(majorVersion);
                        if (versionMatcher.find()) {
                            majorVersion = versionMatcher.group(0);
                        }
                        release = "Java " + majorVersion;
                    }
                    map.put(release, entry);
                    return map;
                }));
            }
        }
    }
}
