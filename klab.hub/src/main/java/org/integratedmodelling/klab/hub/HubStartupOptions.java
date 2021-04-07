package org.integratedmodelling.klab.hub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.klab.Configuration;
import org.integratedmodelling.klab.api.auth.ICertificate;
import org.integratedmodelling.klab.api.hub.IHubStartupOptions;
import org.integratedmodelling.klab.api.services.IConfigurationService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

public class HubStartupOptions implements IHubStartupOptions {

    @Option(name = "-dataDir", usage = "data directory (default: ~/.klab)", metaVar = "<DIRECTORY_PATH>")
    File dataDir = null;

    @Option(name = "-cert", usage = "certificate file (default: <dataDir>/" + ICertificate.DEFAULT_HUB_CERTIFICATE_FILENAME
            + ")", metaVar = "<FILE_PATH>")
    File certificateFile = null;

    @Option(name = "-certResource", usage = "certificate classpath resource (default null)", metaVar = "<CLASSPATH_RESOURCE>")
    String certificateResource = null;

    @Option(name = "-name", usage = "hub name (overrides name in certificate)", metaVar = "<SIMPLE_STRING>")
    String hubName = null;

    @Option(name = "-port", usage = "http port for REST communication", metaVar = "<INT>")
    int port = IConfigurationService.DEFAULT_HUB_PORT;

    @Option(name = "-help", usage = "print command line options and exit")
    boolean help;

    @Option(name = "-cloudConfig", usage = "allow for External Configuration of Node")
    boolean cloudConfig;

    private List<String> arguments = new ArrayList<>();

    /**
     * All defaults
     */
    public HubStartupOptions() {
    }

    public HubStartupOptions( String... args ) {
        initialize(args);
    }

    @Override
    public String[] getArguments(String... additionalArguments) {
        List<String> args = new ArrayList<>(this.arguments);
        if (additionalArguments != null) {
            for(String additionalArgument : additionalArguments) {
                args.add(additionalArgument);
            }
        }
        return args.toArray(new String[args.size()]);
    }

    /**
     * Read the passed arguments and initialize all fields from them.
     * 
     * @param arguments
     * @return true if arguments were OK, false otherwise.
     */
    public boolean initialize(String[] arguments) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(arguments);
        } catch (CmdLineException e) {
            return false;
        }
        return true;
    }

    public String usage() {
        ParserProperties properties = ParserProperties.defaults().withUsageWidth(110);
        CmdLineParser parser = new CmdLineParser(this, properties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        parser.printUsage(baos);
        return "Usage:\n\n" + baos.toString();
    }

    @Override
    public File getCertificateFile() {
        if (certificateFile == null) {
            certificateFile = new File(
                    Configuration.INSTANCE.getDataPath() + File.separator + ICertificate.DEFAULT_HUB_CERTIFICATE_FILENAME);
        }
        return certificateFile;
    }

    public static void main(String args[]) {
        System.out.println(new HubStartupOptions().usage());
    }

    @Override
    public File getDataDirectory() {
        if (dataDir == null) {
            dataDir = Configuration.INSTANCE.getDataPath();
        }
        return dataDir;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isHelp() {
        return help;
    }

    @Override
    public String getCertificateResource() {
        return certificateResource;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public void setCertificateFile(File certificateFile) {
        this.certificateFile = certificateFile;
    }

    public void setCertificateResource(String certificateResource) {
        this.certificateResource = certificateResource;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String getHubName() {
        return hubName;
    }

    @Override
    public boolean isCloudConfig() {
        return cloudConfig;
    }

}
