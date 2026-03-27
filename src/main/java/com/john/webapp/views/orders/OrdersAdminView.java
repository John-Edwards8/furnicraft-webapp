package com.john.webapp.views.orders;

import com.john.webapp.components.AppGridContextMenu;
import com.john.webapp.components.GridContextMenuAction;
import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.dto.OrderResponseDto;
import com.john.webapp.service.ClientServiceClient;
import com.john.webapp.service.OrderServiceClient;
import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Замовлення")
@Route(value = "orders", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.CLIPBOARD_LIST_SOLID)
@RolesAllowed("ADMIN")
public class OrdersAdminView extends Div {
	
	private final OrderServiceClient orderService;
    private final ClientServiceClient clientService;
    private Grid<OrderResponseDto> grid;

    public OrdersAdminView(OrderServiceClient orderService,
            ClientServiceClient clientService) {
		this.orderService = orderService;
		this.clientService = clientService;
		setSizeFull();
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(true);
		layout.setSpacing(false);
		
		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setAlignItems(
		 com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		H3 title = new H3("Замовлення");
		header.setFlexGrow(1, title);
		header.add(title);
		
		grid = buildGrid();
		
		// Підказка про подвійний клік
		Span hint = new Span("Подвійний клік на рядку — переглянути кошториси");
		hint.getStyle()
		 .set("color", "var(--lumo-secondary-text-color)")
		 .set("font-size", "var(--lumo-font-size-s)")
		 .set("padding", "4px 0 8px 0");
		
		layout.add(header, hint, grid);
		add(layout);
	}
    
    private Grid<OrderResponseDto> buildGrid() {
        // Завантажуємо клієнтів один раз для відображення імен
        Map<Long, String> clientNames = clientService.getAllClients()
                .stream()
                .collect(Collectors.toMap(
                        ClientResponseDto::getId,
                        c -> c.getName() + " " + c.getSurname()
                ));
 
        Grid<OrderResponseDto> g = new Grid<>(OrderResponseDto.class, false);
 
        g.addColumn(OrderResponseDto::getId)
                .setHeader("№")
                .setWidth("80px")
                .setFlexGrow(0)
                .setSortable(true);
 
        g.addColumn(order -> {
                    if (order.getOrderDate() == null) return "—";
                    return new SimpleDateFormat("dd.MM.yyyy").format(order.getOrderDate());
                })
                .setHeader("Дата")
                .setWidth("120px")
                .setFlexGrow(0)
                .setSortable(true);
 
        // Клієнт — ім'я або "Клієнт #X" якщо не знайдено
        g.addComponentColumn(order -> {
                    String name = order.getClientId() != null
                            ? clientNames.getOrDefault(
                                    order.getClientId(),
                                    "Клієнт #" + order.getClientId())
                            : "—";
                    Span span = new Span(name);
                    span.getStyle().set("font-weight", "500");
                    return span;
                })
                .setHeader("Клієнт")
                .setAutoWidth(true)
                .setSortable(false);
 
        // Статус як кольоровий бейдж
        g.addComponentColumn(order -> statusBadge(order.getStatus()))
                .setHeader("Статус")
                .setAutoWidth(true);
 
        g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        g.setSizeFull();
        g.setItems(orderService.getAllOrders());
 
        // Подвійний клік → кошториси
        g.addItemDoubleClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.navigate("estimates/" + e.getItem().getId())));
 
        // ПКМ
        new AppGridContextMenu<>(g, List.of(
                new GridContextMenuAction<>(
                        "Переглянути кошториси",
                        VaadinIcon.LIST,
                        order -> getUI().ifPresent(ui ->
                                ui.navigate("estimates/" + order.getId()))
                ),
                new GridContextMenuAction<>(
                        "Видалити замовлення",
                        VaadinIcon.TRASH,
                        true,
                        this::deleteOrder
                )
        ));
 
        return g;
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
    
    private void deleteOrder(OrderResponseDto order) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Видалити замовлення #" + order.getId() + "?");
        dialog.setText("Замовлення буде видалено безповоротно разом з усіма кошторисами.");
        dialog.setCancelable(true);
        dialog.setCancelText("Скасувати");
        dialog.setConfirmText("Видалити");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                orderService.deleteOrder(order.getId(), order.getClientId());
                grid.setItems(orderService.getAllOrders());
                Notification n = Notification.show("Замовлення видалено.",
                        3000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification n = Notification.show(
                        "Помилка: " + ex.getMessage(), 5000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }
}