package io.quarkus.myfaces.showcase;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupListener implements ServletContextListener {

    /*@ConfigProperty(name = "javax.faces.PROJECT_STAGE")
    String projectStage;*/

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //uncoment to override default behavior which is based on quarkus profiles (dev, test and normal)
        //sce.getServletContext().setInitParameter("javax.faces.PROJECT_STAGE", projectStage);
        sce.getServletContext().setInitParameter("primefaces.THEME", "luna-amber");
    }

}
