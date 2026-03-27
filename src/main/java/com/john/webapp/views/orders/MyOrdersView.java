package com.john.webapp.views.orders;

import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.dto.OrderResponseDto;
import com.john.webapp.views.MainLayout;
import com.john.webapp.service.ClientServiceClient;
import com.john.webapp.service.OrderServiceClient;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.util.List;

@PageTitle("Мої замовлення")
@Route(value = "my-orders", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
@RolesAllowed("CLIENT")
public class MyOrdersView extends Div {

    public MyOrdersView(OrderServiceClient orderService,
                        ClientServiceClient clientService,
                        AuthenticationContext authContext) {
        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(false);

        H3 title = new H3("Мої замовлення");

        Span hint = new Span("Подвійний клік на замовленні — переглянути кошторис");
        hint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("padding-bottom", "8px");

        Grid<OrderResponseDto> grid = new Grid<>(OrderResponseDto.class, false);

        grid.addColumn(OrderResponseDto::getId)
                .setHeader("№").setWidth("80px").setFlexGrow(0).setSortable(true);
        grid.addColumn(order -> {
                    if (order.getOrderDate() == null) return "—";
                    return new SimpleDateFormat("dd.MM.yyyy").format(order.getOrderDate());
                })
                .setHeader("Дата замовлення").setWidth("150px").setFlexGrow(0).setSortable(true);
        grid.addComponentColumn(order -> statusBadge(order.getStatus()))
                .setHeader("Статус").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        // Визначити client_id поточного користувача за його email
        String currentEmail = authContext.getPrincipalName().orElse("");
        Long clientId = clientService.getAllClients().stream()
                .filter(c -> currentEmail.equalsIgnoreCase(c.getEmail()))
                .map(ClientResponseDto::getId)
                .findFirst()
                .orElse(null);

        if (clientId != null) {
            List<OrderResponseDto> orders = orderService.getOrdersByClientId(clientId);
            grid.setItems(orders);
        }

        grid.addItemDoubleClickListener(e -> {
            if (e.getItem() == null || e.getItem().getId() == null) return;
            getUI().ifPresent(ui ->
                    ui.navigate("estimates/" + e.getItem().getId()));
        });

        layout.add(title, hint, grid);
        add(layout);
    }

    private Span statusBadge(String status) {
        Span badge = new Span(status != null ? status : "—");
        badge.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");
        if (status == null) return badge;
        switch (status) {
            case "виконано" -> badge.getStyle()
                    .set("background", "var(--lumo-success-color-10pct)")
                    .set("color", "var(--lumo-success-text-color)");
            case "виконується", "кошторис затверджено" -> badge.getStyle()
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)");
            case "скасовано" -> badge.getStyle()
                    .set("background", "var(--lumo-error-color-10pct)")
                    .set("color", "var(--lumo-error-text-color)");
            default -> badge.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("color", "var(--lumo-secondary-text-color)");
        }
        return badge;
    }
}