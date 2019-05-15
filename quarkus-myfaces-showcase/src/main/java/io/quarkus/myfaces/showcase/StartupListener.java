package io.quarkus.myfaces.showcase;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@WebListener
public class StartupListener implements ServletContextListener {

    @ConfigProperty(name = "javax.faces.PROJECT_STAGE")
    String projectStage;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setInitParameter("javax.faces.PROJECT_STAGE", projectStage);
        sce.getServletContext().setInitParameter("primefaces.THEME", "luna-amber");
    }

}
