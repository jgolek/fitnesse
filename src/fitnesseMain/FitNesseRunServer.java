package fitnesseMain;

import static fitnesse.ConfigurationParameter.COMMAND;
import static fitnesse.ConfigurationParameter.LOG_LEVEL;
import static fitnesse.ConfigurationParameter.OMITTING_UPDATES;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.DaemonThreadFactory;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseExpediter;
import fitnesse.FitNesseServer;
import fitnesse.Updater;
import fitnesse.components.PluginsClassLoader;
import fitnesse.reporting.ExitCodeListener;
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.socketservice.SocketService;
import fitnesse.socketservice.SslServerSocketFactory;
import fitnesse.updates.WikiContentUpdater;

public class FitNesseRunServer {

    private static final Logger LOG = Logger.getLogger(FitNesseMain.class.getName());

    private static final ExitCodeListener exitCodeListener = new ExitCodeListener();

    public static void main(String[] args) throws Exception {

      Integer exitCode = 0;
      try {

        Arguments arguments = parseArguments(args);

        ContextConfigurator contextConfigurator = createContextConfigurator(arguments);

        configureLogging("verbose".equalsIgnoreCase(contextConfigurator.get(LOG_LEVEL)));
        loadPlugins(contextConfigurator.get(ConfigurationParameter.ROOT_PATH));

        if (contextConfigurator.get(COMMAND) != null) {
          contextConfigurator.withTestSystemListener(exitCodeListener);
        }

        FitNesseContext context = contextConfigurator.makeFitNesseContext();

        if (!establishRequiredDirectories(context.getRootPagePath())) {
          LOG.severe("FitNesse cannot be started...");
          LOG.severe("Unable to create FitNesse root directory in " + context.getRootPagePath());
          LOG.severe("Ensure you have sufficient permissions to create this folder.");
          exitCode = 1;
        }

        logStartupInfo(context);

//        if (update(context)) {
//          LOG.info("**********************************************************");
//          LOG.info("Files have been updated to a new version.");
//          LOG.info("Please read the release notes on ");
//          LOG.info("http://localhost:" + context.port + "/FitNesse.ReleaseNotes");
//          LOG.info("to find out about the new features and fixes.");
//          LOG.info("**********************************************************");
//        }

          try {            
            
              LOG.info("Starting FitNesse on port: " + context.port);
        
              FitNesse fitNesse = new FitNesse(context);
              
              ServerSocket serverSocket = createServerSocket(context);
              fitNesse.start(serverSocket);
                
              RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {
                  @Override
                  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    LOG.log(Level.WARNING, "Could not handle request. Thread pool is exhausted.");
                  }
              };
              
              ExecutorService executorService = new ThreadPoolExecutor(5, 100, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2), new DaemonThreadFactory(), rejectionHandler);
              FitNesseServer fitnesseServer = new FitNesseServer(context, executorService);
              
              new SocketService(fitnesseServer, false, serverSocket);
              
              //on data input this calls:
              //FitNesseExpediter sender = new FitNesseExpediter(s, context, executorService, requestTimeout);
              //sender.run()
              //FitNesseExpediter.
          
          } catch (BindException e) {
            LOG.severe("FitNesse cannot be started...");
            LOG.severe("Port " + context.port + " is already in use.");
            LOG.severe("Use the -p <port#> command line argument to use a different port.");
            exitCode = 1;
          }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Error while starting the FitNesse", e);
        exitCode = 1;
      }
      
      if (exitCode != null) {
        exit(exitCode);
      }
    }

    private static ContextConfigurator createContextConfigurator(Arguments arguments) {
      ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults();
      contextConfigurator = contextConfigurator.updatedWith(System.getProperties());
      contextConfigurator = contextConfigurator.updatedWith(ConfigurationParameter.loadProperties(new File(arguments
          .getConfigFile(contextConfigurator))));
      contextConfigurator = arguments.update(contextConfigurator);
      return contextConfigurator;
    }

    private static Arguments parseArguments(String[] args) {
      try {
        return new Arguments(args);
      } catch (IllegalArgumentException e) {
        Arguments.printUsage();
        exit(1);
      }
      return null;
    }

    protected static void exit(int exitCode) {
      System.exit(exitCode);
    }

    private static boolean establishRequiredDirectories(String rootPagePath) {
      return establishDirectory(new File(rootPagePath)) && establishDirectory(new File(rootPagePath, "files"));
    }

    private static boolean establishDirectory(File path) {
      return path.exists() || path.mkdir();
    }

    private static boolean update(FitNesseContext context) throws IOException {
      if (!"true".equalsIgnoreCase(context.getProperty(OMITTING_UPDATES.getKey()))) {
        Updater updater = new WikiContentUpdater(context);
        return updater.update();
      }
      return false;
    }

    private static void loadPlugins(String rootPath) throws Exception {
      new PluginsClassLoader(rootPath).addPluginsToClassLoader();
    }


    private static ServerSocket createServerSocket(FitNesseContext context) throws IOException {
      
      String protocol = context.getProperty(FitNesseContext.WIKI_PROTOCOL_PROPERTY);
      boolean useHTTPS = (protocol != null && protocol.equalsIgnoreCase("https"));
      String clientAuth = context.getProperty(FitNesseContext.SSL_CLIENT_AUTH_PROPERTY);
      final boolean sslClientAuth = (clientAuth != null && clientAuth.equalsIgnoreCase("required"));
      final String sslParameterClassName = context.getProperty(FitNesseContext.SSL_PARAMETER_CLASS_PROPERTY);

      return (useHTTPS ? new SslServerSocketFactory(sslClientAuth, sslParameterClassName) : new PlainServerSocketFactory()).createServerSocket(8081);
    }

    private static void logStartupInfo(FitNesseContext context) {
      // This message is on standard output for backward compatibility with
      // Jenkins Fitnesse plugin.
      // (ConsoleHandler of JUL uses standard error output for all messages).
      System.out
          .println("Bootstrapping FitNesse, the fully integrated standalone wiki and acceptance testing framework.");

      LOG.info("root page: " + context.getRootPage());
      LOG.info("logger: " + (context.logger == null ? "none" : context.logger.toString()));
      LOG.info("authenticator: " + context.authenticator);
      LOG.info("page factory: " + context.pageFactory);
      LOG.info("page theme: " + context.pageFactory.getTheme());
    }

    public static void configureLogging(boolean verbose) {
      if (loggingSystemPropertiesDefined()) {
        return;
      }

      InputStream in = FitNesseMain.class.getResourceAsStream((verbose ? "verbose-" : "") + "logging.properties");
      try {
        LogManager.getLogManager().readConfiguration(in);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Log configuration failed", e);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to close Log configuration file", e);
          }
        }
      }
      LOG.finest("Configured verbose logging");
    }

    private static boolean loggingSystemPropertiesDefined() {
      return System.getProperty("java.util.logging.config.class") != null
          || System.getProperty("java.util.logging.config.file") != null;
    }    
    
    
}
