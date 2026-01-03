package com.john.webapp.views.furnicraftdesigns;

import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("FurniCraft Designs")
@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.HOME_SOLID)
//@RolesAllowed("ADMIN")
public class FurniCraftDesignsView extends Composite<VerticalLayout> {

    public FurniCraftDesignsView() {
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H1 h1 = new H1();
        Paragraph textLarge = new Paragraph();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutColumn2.addClassName(Gap.XSMALL);
        layoutColumn2.setPadding(false);
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        h1.setText("Welcome to Order Market!");
        h1.setWidth("100%");
        textLarge.setText("Welcome to FurniCraft Designs! We have been in the furniture market for 5 years!");
        layoutColumn2.setAlignSelf(FlexComponent.Alignment.START, textLarge);
        textLarge.setWidth("100%");
        textLarge.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        getContent().add(layoutColumn2);
        layoutColumn2.add(h1);
        layoutColumn2.add(textLarge);
    }
}
