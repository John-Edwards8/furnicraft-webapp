package com.john.webapp.views;

import java.util.List;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.john.webapp.dto.OrderResponseDto;
import com.john.webapp.service.OrderServiceClient;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@PageTitle("My Orders")
@Route(value = "my-orders", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
@RolesAllowed("CLIENT")
public class MyOrdersView extends Div {
	private final OrderServiceClient orderService;
	
	private static final Long CURRENT_CLIENT_ID = 1L;
	 
    public MyOrdersView(OrderServiceClient orderService) {
        this.orderService = orderService;
 
        setSizeFull();
        addClassNames("my-orders-view");
 
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
 
        H3 title = new H3("My Orders");
 
        Paragraph hint = new Paragraph(
                "Тут відображаються всі ваші замовлення. " +
                "Для деталей або змін зверніться до менеджера."
        );
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
 
        Grid<OrderResponseDto> grid = buildGrid();
 
        layout.add(title, hint, grid);
        add(layout);
    }
 
    private Grid<OrderResponseDto> buildGrid() {
        Grid<OrderResponseDto> grid = new Grid<>(OrderResponseDto.class, false);
 
        grid.addColumn(OrderResponseDto::getId)
                .setHeader("№")
                .setAutoWidth(true)
                .setSortable(true);
 
        grid.addColumn(order -> {
                    if (order.getOrderDate() == null) return "—";
                    return new java.text.SimpleDateFormat("dd.MM.yyyy")
                            .format(order.getOrderDate());
                })
                .setHeader("Date")
                .setAutoWidth(true)
                .setSortable(true);
 
        grid.addColumn(OrderResponseDto::getStatus)
                .setHeader("Status")
                .setAutoWidth(true)
                .setSortable(true);
 
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();
 
        List<OrderResponseDto> orders = orderService.getOrdersByClientId(CURRENT_CLIENT_ID);
        grid.setItems(orders);
 
        return grid;
    }

}
