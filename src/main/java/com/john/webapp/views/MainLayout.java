package com.john.webapp.views;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.lineawesome.LineAwesomeIcon;
import com.john.webapp.views.clients.ClientsView;
import com.john.webapp.views.furnicraftdesigns.FurniCraftDesignsView;
import com.john.webapp.views.orders.MyOrdersView;
import com.john.webapp.views.orders.OrdersAdminView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
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
	
	private final AuthenticationContext authContext;

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

    public MainLayout(AuthenticationContext authContext) {
    	this.authContext = authContext;
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
        
        Button logoutBtn = new Button(VaadinIcon.SIGN_OUT.create(),
                e -> authContext.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        logoutBtn.setTooltipText("Вийти");
        logoutBtn.getStyle()
                .set("margin-left", "var(--lumo-space-m)")
                .set("color", "var(--lumo-secondary-text-color)");
 
        top.add(nav, logoutBtn);
        
        header.add(top, nav);

        return header;
    }

    private MenuItemInfo[] createMenuItems() {
    	List<MenuItemInfo> items = new ArrayList<>();
    	
        items.add(new MenuItemInfo(
                "Головна",
                LineAwesomeIcon.HOME_SOLID.create(),
                FurniCraftDesignsView.class
        ));
        
        if (authContext.hasRole("ADMIN")) {
            items.add(new MenuItemInfo(
                    "Клієнти",
                    LineAwesomeIcon.ADDRESS_BOOK_SOLID.create(),
                    ClientsView.class
            ));
            items.add(new MenuItemInfo(
                    "Замовлення",
                    LineAwesomeIcon.CLIPBOARD_LIST_SOLID.create(),
                    OrdersAdminView.class
            ));
        }
        
        if (authContext.hasRole("CLIENT")) {
            items.add(new MenuItemInfo(
                    "Мої замовлення",
                    LineAwesomeIcon.SHOPPING_CART_SOLID.create(),
                    MyOrdersView.class
            ));
        }
 
        return items.toArray(new MenuItemInfo[0]);
    }
}
