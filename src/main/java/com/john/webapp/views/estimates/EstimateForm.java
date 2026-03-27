package com.john.webapp.views.estimates;

import com.john.webapp.dto.EstimateResponseDto;
import com.john.webapp.service.EstimateServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class EstimateForm extends Div {

    private final EstimateServiceClient estimateService;
    private final Long orderId;

    private EstimateResponseDto current;
    private Runnable onClose;

    private final TextField nameField    = new TextField("Назва");
    private final TextArea  descField    = new TextArea("Опис");
    private final DatePicker dateField   = new DatePicker("Дата");

    private final Button saveBtn   = new Button("Зберегти");
    private final Button cancelBtn = new Button("Скасувати");

    public EstimateForm(EstimateServiceClient estimateService, Long orderId) {
        this.estimateService = estimateService;
        this.orderId = orderId;

        setVisible(false);
        getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("box-sizing", "border-box");

        nameField.setWidthFull();
        nameField.setRequired(true);
        nameField.setPlaceholder("Наприклад: Попередній кошторис шафи");

        descField.setWidthFull();
        descField.setMinHeight("80px");

        dateField.setWidthFull();
        dateField.setValue(LocalDate.now());

        FormLayout formLayout = new FormLayout(nameField, dateField, descField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));
        formLayout.setColspan(descField, 2);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> save());

        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> { if (onClose != null) onClose.run(); });

        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);

        add(new H4("Кошторис"), formLayout, buttons);
    }

    public void editEstimate(EstimateResponseDto dto) {
        this.current = dto;
        setVisible(true);

        if (dto == null) {
            nameField.clear();
            descField.clear();
            dateField.setValue(LocalDate.now());
        } else {
            nameField.setValue(dto.getName() != null ? dto.getName() : "");
            descField.setValue(dto.getDescription() != null ? dto.getDescription() : "");
            dateField.setValue(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        }
    }

    private void save() {
        if (nameField.isEmpty()) {
            nameField.setInvalid(true);
            nameField.setErrorMessage("Назва обов'язкова");
            return;
        }
        nameField.setInvalid(false);

        try {
            EstimateResponseDto dto = current != null ? current : new EstimateResponseDto();
            dto.setName(nameField.getValue());
            dto.setDescription(descField.getValue());
            dto.setDate(dateField.getValue());
            dto.setOrderId(orderId);

            if (current == null) {
                estimateService.create(dto);
                Notification n = Notification.show("Кошторис створено.", 3000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                estimateService.update(dto);
                Notification n = Notification.show("Кошторис оновлено.", 3000,
                        Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            if (onClose != null) onClose.run();

        } catch (Exception ex) {
            Notification n = Notification.show("Помилка: " + ex.getMessage(),
                    5000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}