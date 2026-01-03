package com.john.webapp.views;

import org.vaadin.lineawesome.LineAwesomeIcon;
import com.john.webapp.views.clients.ClientsView;
import com.john.webapp.views.furnicraftdesigns.FurniCraftDesignsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
public class MainLayout extends AppLayout {

    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon,
                            Class<? extends Component> view) {
            this.view = view;

            RouterLink link = new RouterLink(menuTitle, view);
            link.addClassNames(
                    Display.FLEX,
                    Gap.XSMALL,
                    Height.MEDIUM,
                    AlignItems.CENTER,
                    Padding.Horizontal.SMALL,
                    TextColor.BODY
            );
            link.setRoute(view);

            if (icon != null) {
                link.addComponentAsFirst(icon);
            }

            add(link);
        }

        public Class<?> getView() {
            return view;
        }
    }

    public MainLayout() {
        addToNavbar(createHeader());
    }

    private Component createHeader() {
        Header header = new Header();
        header.addClassNames(
                BoxSizing.BORDER,
                Display.FLEX,
                FlexDirection.COLUMN,
                Width.FULL
        );

        Div top = new Div();
        top.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);

        H1 appName = new H1("FurniCraft");
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
        top.add(appName);

        Nav nav = new Nav();
        nav.addClassNames(
                Display.FLEX,
                Overflow.AUTO,
                Padding.Horizontal.MEDIUM,
                Padding.Vertical.XSMALL
        );

        UnorderedList list = new UnorderedList();
        list.addClassNames(
                Display.FLEX,
                Gap.SMALL,
                ListStyleType.NONE,
                Margin.NONE,
                Padding.NONE
        );

        for (MenuItemInfo item : createMenuItems()) {
            list.add(item);
        }

        nav.add(list);
        header.add(top, nav);

        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{
                new MenuItemInfo(
                        "FurniCraft Designs",
                        LineAwesomeIcon.HOME_SOLID.create(),
                        FurniCraftDesignsView.class
                ),
                new MenuItemInfo(
                        "Clients",
                        LineAwesomeIcon.ADDRESS_BOOK_SOLID.create(),
                        ClientsView.class
                )
        };
    }
}
