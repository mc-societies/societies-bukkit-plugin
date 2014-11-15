package net.catharos.societies.bukkit;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Represents a SocietiesPlugin
 */
public class SocietiesPlugin extends JavaPlugin {

    private SocietiesLoader loader;

    @Override
    public void onEnable() {
        File destination = new File(getDataFolder(), "libraries");
        destination.mkdirs();
        try {
            URL librariesURL = new URL("http", "societies.frederik-schmitt.de", "/societies-libraries.jar");

            InputStream is = librariesURL.openStream();

            TarArchiveInputStream taris = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(is)));

            ArchiveEntry entry;

            while ((entry = taris.getNextEntry()) != null) {
//                byte[] content = new byte[(int) entry.getSize()];
//                int read = taris.read(content, 0, content.length);
//                IOUtils.write(content, new FileOutputStream(new File(getDataFolder(), entry.getName())));


                IOUtils.copy(taris, new FileOutputStream(new File(destination, entry.getName())));
            }

            taris.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (File file : destination.listFiles()) {
            try {
                addFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loader = new SocietiesLoader(this);
        loader.onEnable();
    }

    @Override
    public void onDisable() {
        loader.onDisable();
    }

//    @Override
//    public void onEnable() {
//        loader.onEnable();
//    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return loader.onCommand(sender, command, label, args);
    }

    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

}
