package net.minecraft.server.dedicated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import joptsimple.OptionSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.SERVER)
public class PropertyManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    public final Properties serverProperties = new Properties();
    private final File serverPropertiesFile;
    private OptionSet options = null;

    public PropertyManager(File propertiesFile)
    {
        this.serverPropertiesFile = propertiesFile;

        if (propertiesFile.exists())
        {
            FileInputStream fileinputstream = null;

            try
            {
                fileinputstream = new FileInputStream(propertiesFile);
                this.serverProperties.load(fileinputstream);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to load {}", propertiesFile, exception);
                this.generateNewProperties();
            }
            finally
            {
                if (fileinputstream != null)
                {
                    try
                    {
                        fileinputstream.close();
                    }
                    catch (IOException var11)
                    {
                        ;
                    }
                }
            }
        }
        else
        {
            LOGGER.warn("{} does not exist", (Object)propertiesFile);
            this.generateNewProperties();
        }
    }

    public PropertyManager(final OptionSet options) {
        this((File) options.valueOf("config"));
        this.options = options;
    }

    private <T> T getOverride(String name, T value) {
        if ((this.options != null) && (this.options.has(name))) {
            return (T) this.options.valueOf(name);
        }
        return value;
    }

    public void generateNewProperties()
    {
        LOGGER.info("Generating new properties file");
        this.saveProperties();
    }

    public void saveProperties()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            // CraftBukkit start - Don't attempt writing to file if it's read only
            if (this.serverPropertiesFile.exists() && !this.serverPropertiesFile.canWrite()) {
                return;
            }
            // CraftBukkit end
            fileoutputstream = new FileOutputStream(this.serverPropertiesFile);
            this.serverProperties.store(fileoutputstream, "Minecraft server properties");
        }
        catch (Exception exception)
        {
            LOGGER.warn("Failed to save {}", this.serverPropertiesFile, exception);
            this.generateNewProperties();
        }
        finally
        {
            if (fileoutputstream != null)
            {
                try
                {
                    fileoutputstream.close();
                }
                catch (IOException var10)
                {
                    ;
                }
            }
        }
    }

    public File getPropertiesFile()
    {
        return this.serverPropertiesFile;
    }

    public String getStringProperty(String key, String defaultValue)
    {
        if (!this.serverProperties.containsKey(key))
        {
            this.serverProperties.setProperty(key, defaultValue);
            this.saveProperties();
            this.saveProperties();
        }

        return getOverride(key, this.serverProperties.getProperty(key, defaultValue));
    }

    public int getIntProperty(String key, int defaultValue)
    {
        try
        {
            return getOverride(key, Integer.parseInt(this.getStringProperty(key, "" + defaultValue)));
        }
        catch (Exception var4)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return getOverride(key, defaultValue);
        }
    }

    public long getLongProperty(String key, long defaultValue)
    {
        try
        {
            return getOverride(key, Long.parseLong(this.getStringProperty(key, "" + defaultValue)));
        }
        catch (Exception var5)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return getOverride(key, defaultValue);
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue)
    {
        try
        {
            return getOverride(key, Boolean.parseBoolean(this.getStringProperty(key, "" + defaultValue)));
        }
        catch (Exception var4)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return getOverride(key, defaultValue);
        }
    }

    public void setProperty(String key, Object value)
    {
        this.serverProperties.setProperty(key, "" + value);
    }

    public boolean hasProperty(String key)
    {
        return this.serverProperties.containsKey(key);
    }

    public void removeProperty(String key)
    {
        this.serverProperties.remove(key);
    }
}