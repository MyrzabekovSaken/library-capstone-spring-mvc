package com.library.app.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer {
    private static final String HIDDEN_HTTP_METHOD_FILTER = "hiddenHttpMethodFilter";
    private static final String DISPATCHER = "dispatcher";
    private static final String SPRING_SECURITY_FILTER_CHAIN = "springSecurityFilterChain";
    private static final String PATH_ALL = "/*";
    private static final String PATH = "/";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        FilterRegistration.Dynamic hidden = servletContext.addFilter(
                HIDDEN_HTTP_METHOD_FILTER, new HiddenHttpMethodFilter());
        hidden.addMappingForUrlPatterns(null, true, PATH_ALL);

        AnnotationConfigWebApplicationContext context =
                new AnnotationConfigWebApplicationContext();
        context.register(WebAppConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        ServletRegistration.Dynamic registration =
                servletContext.addServlet(DISPATCHER, dispatcherServlet);
        registration.setLoadOnStartup(1);
        registration.addMapping(PATH);

        FilterRegistration.Dynamic securityFilter =
                servletContext.addFilter(SPRING_SECURITY_FILTER_CHAIN, DelegatingFilterProxy.class);
        securityFilter.addMappingForUrlPatterns(null, false, PATH_ALL);
    }
}

