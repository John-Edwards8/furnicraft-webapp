package com.john.webapp.views.login;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    public LoginView() {
        setAction(RouteUtil.getRoutePath(
                VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("FurniCraft");
        i18n.getHeader().setDescription(
                "Дав Бог ремонт, дасть і на меблі");
        i18n.setAdditionalInformation(null);

        LoginI18n.Form form = i18n.getForm();
        form.setUsername("Email");
        form.setPassword("Пароль");
        form.setSubmit("Увійти");
        i18n.setForm(form);

        setI18n(i18n);
        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        setError(event.getLocation().getQueryParameters()
                .getParameters().containsKey("error"));
    }
}