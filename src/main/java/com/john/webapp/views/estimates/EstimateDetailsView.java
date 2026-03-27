package com.john.webapp.views.estimates;

import com.john.webapp.dto.ChangeRequestDto;
import com.john.webapp.dto.EstimateResponseDto;
import com.john.webapp.dto.EstimateLineItemDto;
import com.john.webapp.service.ChangeRequestServiceClient;
import com.john.webapp.service.EstimateServiceClient;
import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@PageTitle("Деталі кошторису")
@Route(value = "estimate-details/:estimateId", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "CLIENT"})
public class EstimateDetailsView extends Div implements BeforeEnterObserver {

    private final EstimateServiceClient estimateService;
    private final ChangeRequestServiceClient changeRequestService;
    private final AuthenticationContext authContext;

    private Long estimateId;
    private EstimateResponseDto estimate;
    private Grid<EstimateLineItemDto> itemsGrid;
    private Grid<ChangeRequestDto> requestsGrid;
    private Span totalSpan;
    private Span requestsBadge;

    public EstimateDetailsView(EstimateServiceClient estimateService,
                                ChangeRequestServiceClient changeRequestService,
                                AuthenticationContext authContext) {
        this.estimateService = estimateService;
        this.changeRequestService = changeRequestService;
        this.authContext = authContext;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        estimateId = event.getRouteParameters().get("estimateId")
                .map(Long::parseLong).orElse(null);
        if (estimateId == null) { event.forwardTo(""); return; }
        estimate = estimateService.getById(estimateId).orElse(null);
        if (estimate == null) { event.forwardTo(""); return; }
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        removeAll();
        boolean isAdmin = authContext.hasRole("ADMIN");
        boolean isFinal = Boolean.TRUE.equals(estimate.getIsFinal());

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(false);

        // Заголовок
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT), e ->
                getUI().ifPresent(ui ->
                        ui.navigate("estimates/" + estimate.getOrderId())));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        H3 title = new H3(estimate.getName());
        header.setFlexGrow(1, title);
        header.add(backBtn, title, isFinal
                ? styledBadge("Фінальний",
                    "var(--lumo-success-color-10pct)", "var(--lumo-success-text-color)")
                : styledBadge("Чернетка",
                    "var(--lumo-contrast-5pct)", "var(--lumo-secondary-text-color)"));

        Span desc = new Span(estimate.getDescription() != null ? estimate.getDescription() : "");
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Таби
        Tab itemsTab = new Tab("Позиції кошторису");

        requestsBadge = new Span("0");
        requestsBadge.getStyle()
                .set("background", "var(--lumo-error-color)").set("color", "white")
                .set("border-radius", "50%").set("font-size", "11px")
                .set("padding", "0 5px").set("margin-left", "6px");
        requestsBadge.setVisible(false);

        HorizontalLayout reqTabContent = new HorizontalLayout(
                new Span("Запити на зміну"), requestsBadge);
        reqTabContent.setAlignItems(FlexComponent.Alignment.CENTER);
        reqTabContent.setSpacing(false);
        Tab requestsTab = new Tab(reqTabContent);

        Tabs tabs = new Tabs(itemsTab, requestsTab);
        Div itemsPanel = buildItemsPanel(isAdmin, isFinal);
        Div reqPanel   = buildRequestsPanel(isAdmin);
        reqPanel.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            itemsPanel.setVisible(tabs.getSelectedTab() == itemsTab);
            reqPanel.setVisible(tabs.getSelectedTab() == requestsTab);
        });

        layout.add(header, desc, tabs, itemsPanel, reqPanel);
        add(layout);

        // FAB Додати позицію (ADMIN, не фінальний)
        if (isAdmin && !isFinal) {
            Button fab = fabButton(VaadinIcon.PLUS, "Додати позицію");
            fab.addClickListener(e ->
                    new AddItemDialog(estimateService, estimateId, this::refreshAll).open());
            add(fab);
        }

        // FAB Подати запит (CLIENT, тільки якщо НЕ фінальний)
        if (!isAdmin && !isFinal) {
            Button fab = fabButton(VaadinIcon.ENVELOPE, "Подати запит на зміну");
            fab.getStyle().set("background-color", "var(--lumo-primary-color)");
            fab.addClickListener(e -> openRequestDialog());
            add(fab);
        }
    }

    private Div buildItemsPanel(boolean isAdmin, boolean isFinal) {
        itemsGrid = new Grid<>(EstimateLineItemDto.class, false);
        itemsGrid.addComponentColumn(item -> {
            Span b = new Span(typeName(item.getType()));
            b.getStyle().set("font-size", "var(--lumo-font-size-xs)")
                    .set("padding", "1px 6px")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("background", "var(--lumo-contrast-5pct)");
            return b;
        }).setHeader("Тип").setWidth("110px").setFlexGrow(0);
        itemsGrid.addColumn(EstimateLineItemDto::getItemName)
                .setHeader("Назва").setAutoWidth(true);
        itemsGrid.addColumn(i -> i.getUnitPrice() != null ? i.getUnitPrice() + " грн." : "—")
                .setHeader("Ціна/од.").setWidth("120px").setFlexGrow(0);
        itemsGrid.addColumn(EstimateLineItemDto::getAmount)
                .setHeader("К-сть").setWidth("80px").setFlexGrow(0);
        itemsGrid.addColumn(i -> i.getLineTotal() != null ? i.getLineTotal() + " грн." : "—")
                .setHeader("Сума").setWidth("120px").setFlexGrow(0);
        itemsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        if (isAdmin && !isFinal) {
            GridContextMenu<EstimateLineItemDto> menu = new GridContextMenu<>(itemsGrid);
            menu.addItem("Видалити позицію", e -> e.getItem().ifPresent(item -> {
                ConfirmDialog d = new ConfirmDialog();
                d.setHeader("Видалити позицію?");
                d.setText("«" + item.getItemName() + "» буде видалено.");
                d.setCancelable(true); d.setCancelText("Скасувати");
                d.setConfirmText("Видалити"); d.setConfirmButtonTheme("error primary");
                d.addConfirmListener(ev -> {
                    try {
                        estimateService.removeItem(estimateId, item.getItemId(), item.getType());
                        refreshAll();
                    } catch (Exception ex) { showError(ex.getMessage()); }
                });
                d.open();
            }));
        }

        totalSpan = new Span();
        totalSpan.getStyle().set("font-weight", "600").set("font-size", "1.1em");
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.add(totalSpan);

        Div panel = new Div(itemsGrid, footer);
        panel.setSizeFull();
        return panel;
    }

    private Div buildRequestsPanel(boolean isAdmin) {
        requestsGrid = new Grid<>(ChangeRequestDto.class, false);
        requestsGrid.addColumn(ChangeRequestDto::getRequestDate)
                .setHeader("Дата").setWidth("110px").setFlexGrow(0).setSortable(true);

        // Колонка "Клієнт" — тільки для адміна
        if (isAdmin) {
            requestsGrid.addColumn(ChangeRequestDto::getClientEmail)
                    .setHeader("Клієнт").setWidth("200px").setFlexGrow(0);
        }

        requestsGrid.addColumn(ChangeRequestDto::getRequestText)
                .setHeader("Запит").setAutoWidth(true);
        requestsGrid.addComponentColumn(r -> statusRequestBadge(r.getStatus()))
                .setHeader("Статус").setWidth("130px").setFlexGrow(0);
        requestsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        if (isAdmin) {
            GridContextMenu<ChangeRequestDto> menu = new GridContextMenu<>(requestsGrid);
            menu.addItem("Позначити «розглянутий»",
                    e -> e.getItem().ifPresent(r -> updateRequestStatus(r, "розглянутий")));
            menu.addItem("Позначити «відхилений»",
                    e -> e.getItem().ifPresent(r -> updateRequestStatus(r, "відхилений")));
        }

        Div panel = new Div(requestsGrid);
        panel.setSizeFull();
        return panel;
    }

    private void openRequestDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Запит на зміну кошторису");
        dialog.setWidth("480px");

        TextArea text = new TextArea("Опишіть що потрібно змінити");
        text.setWidthFull();
        text.setMinHeight("120px");
        text.setPlaceholder("Наприклад: замінити матеріал X на Y...");

        Button send = new Button("Надіслати", e -> {
            if (text.isEmpty()) {
                text.setInvalid(true);
                text.setErrorMessage("Текст запиту не може бути порожнім");
                return;
            }
            try {
                ChangeRequestDto dto = new ChangeRequestDto();
                dto.setEstimateId(estimateId);
                dto.setClientEmail(authContext.getPrincipalName().orElse("unknown"));
                dto.setRequestText(text.getValue());
                changeRequestService.create(dto);
                dialog.close();
                refreshAll();
                Notification n = Notification.show(
                        "Запит надіслано. Адміністратор розгляне його найближчим часом.",
                        4000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Скасувати", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout btns = new HorizontalLayout(send, cancel);
        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dialog.add(new VerticalLayout(text, btns));
        dialog.open();
    }

    private void updateRequestStatus(ChangeRequestDto r, String status) {
        try { changeRequestService.updateStatus(r.getId(), status); refreshAll(); }
        catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void refreshAll() { refreshItems(); refreshRequests(); }

    private void refreshItems() {
        if (itemsGrid == null) return;
        List<EstimateLineItemDto> items = estimateService.getItems(estimateId);
        itemsGrid.setItems(items);
        BigDecimal total = items.stream().map(EstimateLineItemDto::getLineTotal)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalSpan.setText("Загальна сума: " + total + " грн.");
    }

    private void refreshRequests() {
        if (requestsGrid == null) return;
        List<ChangeRequestDto> requests = changeRequestService.getByEstimateId(estimateId);
        requestsGrid.setItems(requests);
        long newCount = requests.stream()
                .filter(r -> "новий".equals(r.getStatus())).count();
        requestsBadge.setText(String.valueOf(newCount));
        requestsBadge.setVisible(newCount > 0);
    }

    private Button fabButton(VaadinIcon icon, String tooltip) {
        Button btn = new Button(new Icon(icon));
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_LARGE);
        btn.setTooltipText(tooltip);
        btn.getStyle().set("position", "fixed")
                .set("bottom", "var(--lumo-space-xl)")
                .set("right", "var(--lumo-space-l)")
                .set("border-radius", "50%")
                .set("width", "56px").set("height", "56px").set("z-index", "100");
        return btn;
    }

    private Span styledBadge(String text, String bg, String color) {
        Span s = new Span(text);
        s.getStyle().set("background", bg).set("color", color)
                .set("padding", "2px 10px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");
        return s;
    }

    private Span statusRequestBadge(String status) {
        return switch (status != null ? status : "") {
            case "розглянутий" -> styledBadge("розглянутий",
                    "var(--lumo-success-color-10pct)", "var(--lumo-success-text-color)");
            case "відхилений"  -> styledBadge("відхилений",
                    "var(--lumo-error-color-10pct)", "var(--lumo-error-text-color)");
            default            -> styledBadge("новий",
                    "var(--lumo-primary-color-10pct)", "var(--lumo-primary-text-color)");
        };
    }

    private String typeName(com.john.webapp.dto.CatalogItemDto.Type type) {
        if (type == null) return "—";
        return switch (type) {
            case MATERIAL  -> "Матеріал";
            case ACCESSORY -> "Фурнітура";
            case PROCESS   -> "Процес";
        };
    }

    private void showError(String msg) {
        Notification n = Notification.show("Помилка: " + msg,
                5000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}